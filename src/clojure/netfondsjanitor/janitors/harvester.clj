(ns netfondsjanitor.janitors.harvester
  (:import
    [java.io File]
    [java.time LocalDate]
    [org.apache.ibatis.exceptions PersistenceException]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial DerivativePrice StockPrice]
    [oahu.financial.repository EtradeDerivatives]
    [ranoraraku.models.mybatis DerivativeMapper]
    [oahu.exceptions HtmlConversionException]
    [ranoraraku.beans.options DerivativePriceBean])
  (:use
    [clojure.string :only [split join]]
    [clojure.algo.monads :only [domonad maybe-m]]
    [netfondsjanitor.service.common :only (*user-tix* *test-run* *feed*)])
  (:require
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]))

(defn flatten-1
  [x]
  (filter #(and (sequential? %) (not-any? sequential? %))
    (rest (tree-seq #(and (sequential? %) (some sequential? %)) seq x))))


(defn correct-date? [d m]
  (let [short-months [4 6 9 11]]
    (cond
      (COM/in? m short-months) (<= d 30)
      (= m 2) (<= d 28)
      :else true)))

(defn all-days [y]
  (fn [m]
    (for [dx (range 1 32) :when (correct-date? dx m)]
      [y m dx])))

(defn full-year [y]
  (for [mx (range 1 13) dx (range 1 32) :when (correct-date? dx mx)] [y mx dx]))

(defn pm_ [range-fn rr y m d]
  (for [dx (range-fn d rr) :when (correct-date? dx m)]
    [y m dx]))

(def month-end (partial pm_ drop (range 32)))

(def month-begin (partial pm_ take (range 1 32)))

(defn year-end  [y m d]
  (let [a (month-end y m d)
        b (for [mx (range (+ m 1) 13)
                dx (range 1 32) :when (correct-date? dx mx)]
            [y mx dx])]
    (concat a b)))

(defn year-begin [y m d]
  (let [a (for [mx (range 1 m)
                dx (range 1 32) :when (correct-date? dx mx)]
            [y mx dx])
        b (month-begin y m d)]
    (concat a b)))

(defn process-file [tix etrade on-process-file]
  (fn [^File f]
    (let [tix-re (re-pattern "(\\S*)\\.html$")]
      (LOG/info (str "(Harvest) Trying file: " (.getPath f)))
      (domonad maybe-m
        [
          cur-tix (re-matches tix-re (.getName f))
          hit (COM/in? (second cur-tix) tix)
          ]
        (on-process-file f etrade)))))

(def ^:dynamic *process-file*)

(defn process-dir [[y m d]]
  ;(let [cur-dir (clojure.java.io/file (join "/" ["/home/rcs/opt/java/netfondsjanitor/feed" y m d]))
  (let [cur-dir (clojure.java.io/file (join "/" [*feed* y m d]))
        files (filter #(.isFile ^File %) (file-seq cur-dir))]
    (doseq [cur-file files] (*process-file* cur-file))))

(defn items-between-dates [^LocalDate from-date
                           ^LocalDate to-date]
  (let [pfn
          (fn [v]
            ;(map read-string (split v #"-")))
            [(.getYear v)
            (.getValue (.getMonth v))
            (.getDayOfMonth v)])
        [y1 m1 d1] (pfn from-date)
        [y2 m2 d2] (pfn to-date)
        items (cond
                (and (= y1 y2) (= m1 m2) (= d1 d2))
                  [[y1 m1 d1]]
                (and (= y1 y2) (= m1 m2))
                  (let [days (range d1 (+ d2 1))]
                    (for [d days]
                      [y1 m1 d]))
                (= y1 y2)
                  (let [months (drop 1 (range m1 m2))
                        a (month-end y1 m1 d1)
                        b (flatten-1 (map (all-days y1) months))
                        c (month-begin y2 m2 d2)]
                    (concat a b c))
                :else
                  (let [years (drop 1 (range y1 y2))
                        a (year-end y1 m1 d1)
                        b (flatten-1 (map full-year years))
                        c (year-begin y2 m2 d2)]
                    (concat a b c)))]
    items))

(defn do-harvest-files-with
  ([on-process-file
    ^EtradeDerivatives etrade
    ^JanitorContext ctx]
    (let [tix (or *user-tix* (COM/db-tix COM/tcat-in-1-3))
          from-date (.harvestFrom ctx)
          to-date (.harvestTo ctx)]
      (do-harvest-files-with on-process-file etrade tix from-date to-date)))
  ([on-process-file
    ^EtradeDerivatives etrade
    tix
    from-date
    & [to-date]]
    ;(try
    (let [to-datex (if (nil? to-date) from-date to-date)
          items (items-between-dates from-date to-datex)]
        (LOG/info (str "(Harvest) Processing files from: " from-date " to: " to-datex))
        (binding [*process-file* (process-file tix etrade on-process-file)]
          (doseq [cur-dir items] (process-dir cur-dir))))))
      ;(catch HtmlConversionException hex (LOG/error (str "(Harvest) " (.getMessage hex)))))))


(defn insert [calls puts ctx]
  (let [calls-puts (concat calls puts)]
    (doseq [^DerivativePriceBean c calls-puts]
      (LOG/info (str "New Option id: " (.getDerivativeId c) ", option type: " (-> c .getDerivative .getOpType)))
      (.insertDerivativePrice ^DerivativeMapper ctx c))))

(defn insert-existing-spot [spot calls puts ctx]
  (let [oid (.findSpotId ^DerivativeMapper ctx ^StockPrice spot)]
    (.setOid ^StockPrice spot oid)
    (let [num-prices (.countOpxPricesForSpot ^DerivativeMapper ctx ^StockPrice spot)]
      (if (= num-prices 0)
        (do
          (LOG/info (str "Inserting new option price for existing spot [oid " (.getOid ^StockPrice spot) "]"))
          (insert calls puts ^DerivativeMapper ctx))
        (LOG/info (str "Option prices  already inserted (" num-prices  ") for existing spot [oid " (.getOid ^StockPrice spot) "]"))))))

(defn redo-harvest-spots-and-optionprices [^File f,
                                   ^EtradeDerivatives etrade])

(defn harvest-spots-and-optionprices [^File f,
                  ^EtradeDerivatives etrade]
  (try
    (let [scp (.getSpotCallsPuts2 etrade ^File f)
          ^StockPrice spot (.first scp)
          calls (.second scp)
          puts (.third scp)]
      (if (= *test-run* true)
        (LOG/info (str "[Test Run] Ticker: " (-> spot .getStock .getTicker) ", number of calls: " (count calls) ", puts: " (count puts)))
        (try
          (LOG/info (str "(harvest) Hit on file: " (.getPath f)
                      ", date: " (.getDx spot)
                      ", time: " (.getSqlTime spot)))
          (DB/with-session DerivativeMapper
            (do
              (.insertSpot it spot)
              (LOG/info (str "Inserted new spot [oid " (.getOid spot) "]: " (-> spot .getStock .getTicker)))
              (insert calls puts it)))
          (catch PersistenceException e
            (let [err-code (.getSQLState (.getCause e))]
              (if (.equals err-code "23505")
                (DB/with-session DerivativeMapper
                  (insert-existing-spot spot calls puts it))
                (LOG/error (str (.getMessage e))))))
          (catch Exception e (LOG/error (str "[" (.getPath f) "] "(.getMessage e)))))))
    (catch HtmlConversionException hex (LOG/warn (str "[" (.getPath f) "] "(.getMessage hex))))))

(defn harvest-derivatives [^File f,
                           ^EtradeDerivatives etrade]
  (try
    (LOG/info (str "(Harvest new derivatives) Hit on file: " (.getPath f)))
    (let [call-put-defs (.getCallPutDefs2 etrade f)]
      (if (= *test-run* true)
        (doseq [d call-put-defs]
          (LOG/info (str "(Test run) Would insert  " (.getOpTypeStr d) ": " (.getTicker d))))
        (DB/insert-derivatives call-put-defs)))
  (catch HtmlConversionException hex (LOG/warn (str "[" (.getPath f) "] "(.getMessage hex))))))

(defn harvest-list-derivatives [^File f,
                           ^EtradeDerivatives etrade])

(defn harvest-list-file [^File f,
                        ^EtradeDerivatives etrade]
  (LOG/info (str "(Harvest new derivatives) Hit on file: " (.getPath f)))
  )

