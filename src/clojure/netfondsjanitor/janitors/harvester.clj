(ns netfondsjanitor.janitors.harvester
  (:import
    [java.util Optional]
    [java.io File]
    [java.time LocalDate]
    [org.apache.ibatis.exceptions PersistenceException]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial DerivativePrice StockPrice]
    [oahu.financial Derivative$LifeCycle]
    [oahu.financial.repository EtradeRepository]
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

(def tix-re (re-pattern "(\\S*)\\.html$"))

(defn ticker-name-from-file [^File f]
  (let [m (re-matches tix-re (.getName f))]
    (if (nil? m)
      nil
      (second m))))

(defn process-file [tix etrade on-process-file]
  (fn [^File f year month day]
    (LOG/info (str "(Harvest) Trying file: " (.getPath f)))
    (domonad maybe-m
      [
        cur-tix (ticker-name-from-file f)
        hit (COM/in? cur-tix tix)
        ]
      (on-process-file {:f f :etrade etrade :year year :month month :day day}))))

(def ^:dynamic *process-file*)

(defn process-dir [[y m d]]
  (let [cur-dir (clojure.java.io/file (join "/" [*feed* y m d]))
        files (filter #(.isFile ^File %) (file-seq cur-dir))]
    (doseq [cur-file files] (*process-file* cur-file y m d))))

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
    ^EtradeRepository etrade
    ^JanitorContext ctx]
    (let [tix (or *user-tix* (COM/db-tix COM/tcat-in-1-3))
          from-date (.harvestFrom ctx)
          to-date (.harvestTo ctx)]
      (do-harvest-files-with on-process-file etrade tix from-date to-date)))
  ([on-process-file
    ^EtradeRepository etrade
    tix
    from-date
    & [to-date]]
    ;(try
    (let [to-datex (if (nil? to-date) from-date to-date)
          items (items-between-dates from-date to-datex)]
        (LOG/info (str "(Harvest) Processing files from: " from-date " to: " to-datex ", num items: " (.count items)))
        (binding [*process-file* (process-file tix etrade on-process-file)]
          (doseq [cur-dir items] 
            (LOG/info (str "Cur dir: " cur-dir))
            (process-dir cur-dir))))))
      ;(catch HtmlConversionException hex (LOG/error (str "(Harvest) " (.getMessage hex)))))))


(defn insert [calls puts ctx]
  (let [calls-puts (concat calls puts)]
    (doseq [^DerivativePriceBean c calls-puts]
      (LOG/info (str "New Option id: " (.getDerivativeId c) ", option type: " (-> c .getDerivative .getOpType)))
      (.insertDerivativePrice ^DerivativeMapper ctx c))))

(defn insert-existing-spot [spot calls puts ctx]
  (if-let [oid (.findSpotId ^DerivativeMapper ctx ^StockPrice spot)]
    (do
      (.setOid ^StockPrice spot oid)
      (let [num-prices (.countOpxPricesForSpot ^DerivativeMapper ctx ^StockPrice spot)]
        (if (= num-prices 0)
          (do
            (LOG/info (str "Inserting new option prices for existing spot [oid " (.getOid ^StockPrice spot) "]"))
            (insert calls puts ^DerivativeMapper ctx))
          (LOG/info (str "Option prices  already inserted (" num-prices  ") for existing spot [oid " (.getOid ^StockPrice spot) "]")))))
  (LOG/info (str "Did not find oid for StockPrice [oid " (.getOid ^StockPrice spot) "]"))))

(defn redo-harvest-spots-and-optionprices 
  [{:keys [^File f ^EtradeRepository etrade year month day]}])

(comment
  (try
    (let [scp (.getSpotCallsPuts2 etrade ^File f)
          ^StockPrice spot (.first scp)]
      (if-let [spot-oid (DB/with-session DerivativeMapper
                          (.findSpotId ^DerivativeMapper it ^StockPrice spot))]
        (let [calls (.second scp)
              puts (.third scp)
              insert-fn (fn [x]
                          (let [opid (.getDerivativeId x)
                                sid (.getStockPriceId x)]
                            (DB/with-session DerivativeMapper
                              (try
                                (do
                                  (LOG/info (str "New option price?: stockprice id: " sid ", option id: " opid ", buy: " (.getBuy x) ", sell: " (.getSell x)))
                                  (.insertDerivativePrice ^DerivativeMapper it x))
                                (catch Exception e
                                  (LOG/warn (.getMessage e)))))))]
          (.setOid spot spot-oid)
          (LOG/info (str "Inserting new option prices for existing spot [oid " spot-oid "]"))
          (doseq [cx calls] (insert-fn cx))
          (doseq [px puts] (insert-fn px)))
        (LOG/info (str "Did not find oid for StockPrice [oid " (.getOid ^StockPrice spot) "]"))))
  (catch Exception e
    (LOG/warn (.getMessage e)))))



(defn try-harvest-spot-calls-puts [^StockPrice spot calls puts ^File f]
  (try
    (do
      (LOG/info (str "(harvest) Hit on file: " (.getPath f)
                  ", date: " (.getDx spot)
                  ", time: " (.getSqlTime spot)))
        ;(doseq [c calls]
        ;  (println (str "Call: " (.getDerivativeId c)))))
      (DB/with-session DerivativeMapper
        (do
          (.insertSpot it spot)
          (LOG/info (str "Inserted new spot [oid " (.getOid spot) "]: " (-> spot .getStock .getTicker)))
          (insert calls puts it))))
  (catch PersistenceException e
    (let [err-code (.getSQLState (.getCause e))]
      (if (.equals err-code "23505")
        (DB/with-session DerivativeMapper
          (insert-existing-spot spot calls puts it))
        (LOG/error (str (.getMessage e))))))
  (catch Exception e (LOG/error (str "[" (.getPath f) "] " (.getMessage e))))))

(defn harvest-spots-and-optionprices 
  [{:keys [^File f ^EtradeRepository etrade year month day]}]
  (do 
    (.setDownloadDate etrade (LocalDate/of year month day))
    (try
      (let [f-name (ticker-name-from-file f)
            ^Optional opt-spot (.stockPrice etrade f-name f)]
        (if (= (.isPresent opt-spot) true)
          (let  [^StockPrice spot (.get opt-spot)
                calls (.calls etrade f-name f)
                puts (.puts etrade f-name f)]
            (if (= *test-run* true)
              (LOG/info (str "[Test Run] Ticker: " (-> spot .getStock .getTicker) ", number of calls: " (count calls) ", puts: " (count puts)))
              (try-harvest-spot-calls-puts spot calls puts f)))))
    (catch HtmlConversionException hex (LOG/warn (str "[" (.getPath f) "] " (.getMessage hex)))))))

(defn harvest-derivatives 
  [{:keys [^File f ^EtradeRepository etrade]}]
  (try
    (LOG/info (str "(Harvest new derivatives) Hit on file: " (.getPath f)))
    (let [ticker (ticker-name-from-file f)
          call-put-defs (filter #(= (.getLifeCycle %) Derivative$LifeCycle/FROM_HTML) (.callPutDefs etrade ticker f))]
      (if (= *test-run* true)
        (doseq [d call-put-defs]
          (LOG/info (str "(Test run) Would insert  " (.getOpTypeStr d) ": " (.getTicker d) ", life cycle: " (.getLifeCycle d))))
        (DB/insert-derivatives call-put-defs)))
  (catch HtmlConversionException hex (LOG/warn (str "[" (.getPath f) "] "(.getMessage hex))))))

(defn harvest-list-derivatives 
  [{:keys [^File f ^EtradeRepository etrade year month day]}])

(defn harvest-list-file 
  [{:keys [^File f ^EtradeRepository etrade year month day]}]
  (LOG/info (str "(Harvest new derivatives) Hit on file: " (.getPath f) ", year: " year ", monht: " month ", day:" day)))

(defn file-name-demo 
  [{:keys [^File f ^EtradeRepository etrade]}]
  (println (ticker-name-from-file f)))
