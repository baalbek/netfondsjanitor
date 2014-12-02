(ns netfondsjanitor.janitors.DefaultJanitor
  (:gen-class
    :init init
    :state state
    :implements [oahu.financial.janitors.Janitor]
    :methods [
               [setFeedStoreDir [String] void]
               [setStockMarketRepos [oahu.financial.repository.StockMarketRepository] void]
               [setDownloader [oahu.financial.html.EtradeDownloader] void]
               [setEtrade [oahu.financial.repository.EtradeDerivatives] void]
               [setDownloadManager [oahu.financial.html.DownloadManager] void]
               [setOptionsHtmlParser [oahu.financial.html.OptionsHtmlParser] void]
               ]
    )
  (:use
    [clojure.algo.monads :only [domonad maybe-m]]
    [clojure.string :only [split join]]
    [netfondsjanitor.service.common :only (*user-tix* *feed* *repos*)])
  (:import
    [oahu.financial.html OptionsHtmlParser]
    [java.io File FileNotFoundException]
    [java.time LocalTime]
    [com.gargoylesoftware.htmlunit.html HtmlPage]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockBean StockPriceBean DerivativeBean]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial Stock StockPrice]
    [oahu.financial.repository EtradeDerivatives]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.html EtradeDownloader]
    [oahu.financial.html DownloadManager])
  (:require
    [maunakea.financial.htmlutil :as hu]
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]
    [netfondsjanitor.service.feed :as FEED]))

;;;------------------------------------------------------------------------
;;;-------------------------- gen-class methods ---------------------------
;;;------------------------------------------------------------------------
(defn -init []
  [[] (atom {})])

(defn set-property [this k v]
  (let [s (.state this)]
    (swap! s assoc k v)))

(defn -setStockMarketRepos [this, ^StockMarketRepository value]
  (set-property this :repos value))

(defn -setFeedStoreDir [this, ^StockMarketRepository value]
  (set-property this :feed value))

(defn -setEtrade [this, ^StockMarketRepository value]
  (set-property this :etrade value))

(defn -setDownloader [this, ^StockMarketRepository value]
  (set-property this :downloader value))

(defn -setDownloadManager [this, ^StockMarketRepository value]
  (set-property this :manager value))

(defn -setOptionsHtmlParser [this, ^StockMarketRepository value]
  (set-property this :opxhtmlparser value))

(comment
  (defn -setStockMarketRepos [this, ^StockMarketRepository value]
    (let [s (.state this)]
      (swap! s assoc :repos value)))

  (defn -setFeedStoreDir [this, ^String value]
    (let [s (.state this)]
      (swap! s assoc :feed value)))

  (defn -setEtrade [this, ^EtradeDerivatives value]
    (let [s (.state this)]
      (swap! s assoc :etrade value)))

  (defn -setDownloader [this, ^EtradeDownloader value]
    (let [s (.state this)]
      (swap! s assoc :downloader value)))

  (defn -setDownloadManager [this, ^DownloadManager value]
    (let [s (.state this)]
      (swap! s assoc :manager value)))

  (defn -setOptionsHtmlParser [this, ^OptionsHtmlParser value]
    (let [s (.state this)]
      (swap! s assoc :opxhtmlparser value)))
  )

;;;------------------------------------------------------------------------
;;;-------------------------- Cloure methods ---------------------------
;;;------------------------------------------------------------------------

(def db-tix (memoize 
  (fn [f]
    (println (str "db-tix first time " f))
    (let [stocks (.getStocks *repos*)
          tix (if (nil? f)
                stocks
                (filter f stocks))
          tix-s (map #(.getTicker %) tix)]
      tix-s))))
      ;[tix tix-s]))))

(defn do-paper-history [^EtradeDownloader downloader]
  ;(let [[_ tix-s] (db-tix nil)]
  (let [tix-s (or *user-tix* (db-tix nil))]
    (doseq [t tix-s]
      (LOG/info (str "Will download paper history for " t))
      (.downloadPaperHistory downloader t))))

(defn do-feed []
  ;(let [[_ tix-s] (db-tix nil)]
  (let [tix-s (or *user-tix* (db-tix nil))]
    (doseq [t tix-s]
      (try
        (let [cur-lines  (FEED/get-lines t)
              num-beans (count cur-lines)]
          (if (> num-beans 0)
            (do
              (LOG/info (str "Will insert " num-beans " for " t))
              (DB/update-stockprices cur-lines))
            (LOG/info (str "No beans for " t))))
        (catch FileNotFoundException fe
          (LOG/warn (str "No feed for ticker " t ": " (.getMessage fe))))
        (catch Exception e
          (LOG/fatal (str "Unexpected error: " (.getMessage e) " aborting"))
          (System/exit 0))))))

(defn tcat-in [in-vals v]
  (let [category (.getTickerCategory v)]
    (some #{category} in-vals)))

(def tcat-in-1-3 (partial tcat-in [1 3]))

(defn do-spot [^EtradeDerivatives etrade]
  (let [tix-s (db-tix tcat-in-1-3) ;(db-tix #(= 1 (.getTickerCategory %)))
        stocks (COM/map-java-fn .getSpot etrade tix-s)]
    (DB/with-session StockMapper
      (doseq [^StockPriceBean s stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            (.insertStockPrice it s)
            ))))))

(defn do-spots-from-downloaded-options [^DownloadManager manager, ^OptionsHtmlParser parser]
  (let [;tix-s (or *user-tix* (db-tix #(= 1 (.getTickerCategory %))))
        tix-s (or *user-tix* (db-tix (partial tcat-in-1-3)))
        pages (COM/map-tuple-java-fn .getLastDownloaded manager tix-s)]
    (DB/with-session StockMapper
      (doseq [[^String ticker, ^HtmlPage page] pages]
        (let [^Stock stock (.findStock *repos* ticker)
              ^StockPriceBean s (.parseSpot parser page)]
          (.setStock s stock)
          (.insertStockPrice it s)
          )))))

(defn do-upd-derivatives [^EtradeDerivatives etrade]
  (let [tix-s (or *user-tix* (db-tix tcat-in-1-3))] ;#(= 1 (.getTickerCategory %))))]
    (doseq [t tix-s]
      (LOG/info (str "Will update derivatives for " t))
      (let [derivx (.getCallPutDefs etrade t)]
        (DB/insert-derivatives derivx)))))

(comment
  (let [calls (.getCalls etrade t)
        puts (.getPuts etrade t)]
    (DB/insert-derivatives calls)
    (DB/insert-derivatives puts)))

;(defn do-onetime-download-options [^EtradeDownloader downloader])

(defn block-task [test wait]
  (while (test)
    (LOG/info "Market not open yet...")
    (Thread/sleep wait)))

(defn while-task [test wait f]
  (while (test) (do (f) (Thread/sleep wait))))

(defn time-less-than [cur-time]
  (fn []
    (< (.compareTo (LocalTime/now) cur-time) 0)))

(defmacro doif [java-prop ctx & body]
  `(if (= (~java-prop  ~ctx) true)
    ~@body))

(defmacro in? [v items]
  `(some #(= ~v %) ~items))

(defn flatten-1
  [x]
  (filter #(and (sequential? %) (not-any? sequential? %))
    (rest (tree-seq #(and (sequential? %) (some sequential? %)) seq x))))

(defn do-harvest-files-with [on-process-file
                       ^EtradeDerivatives etrade
                       tix
                       from-date
                       & [to-date]]
  (let [to-datex (if (nil? to-date) from-date to-date)
        tix-re (re-pattern "(\\S*)\\.html$")
        short-months [4 6 9 11]
        correct-date?
          (fn [d m]
            (cond
              (in? m short-months) (<= d 30)
              (= m 2) (<= d 28)
              :else true))
        all-days
          (fn [y]
            (fn [m]
              (for [dx (range 1 32) :when (correct-date? dx m)]
                [y m dx])))
        full-year
          (fn [y]
            (for [mx (range 1 13) dx (range 1 32) :when (correct-date? dx mx)] [y mx dx]))
        pm_
          (fn [range-fn rr y m d]
            (for [dx (range-fn d rr) :when (correct-date? dx m)]
              [y m dx]))
        month-end (partial pm_ drop (range 32))
        month-begin (partial pm_ take (range 1 32))
        year-end
          (fn [y m d]
            (let [a (month-end y m d)
                  b (for [mx (range (+ m 1) 13)
                          dx (range 1 32) :when (correct-date? dx mx)]
                      [y mx dx])]
              (concat a b)))
        year-begin
          (fn [y m d]
            (let [a (for [mx (range 1 m)
                          dx (range 1 32) :when (correct-date? dx mx)]
                      [y mx dx])
                  b (month-begin y m d)]
              (concat a b)))
        process-file
          (fn [^File f]
            (LOG/info (str "(Harvest) Trying file: " (.getPath f)))
            (domonad maybe-m
              [
                cur-tix (re-matches tix-re (.getName f))
                hit (in? (second cur-tix) tix)
                scp (.getSpotCallsPuts2 etrade ^File f)
                spot (.first scp)
                calls (.second scp)
                puts (.third scp)
              ]
              (on-process-file f spot calls puts)
              ))

        process-dir
          (fn [[y m d]]
            (let [cur-dir (clojure.java.io/file (join "/" ["/home/rcs/opt/java/netfondsjanitor/feed" y m d]))
                  files (filter #(.isFile %) (file-seq cur-dir))]
              (doseq [cur-file files] (process-file cur-file))))
        pfn
          (fn [v]
            (map read-string (split v #"-")))
        [y1 m1 d1] (pfn from-date)
        [y2 m2 d2] (pfn to-datex)]
    (let [items (cond
                  (and (= y1 y2) (= m1 m2) (= d1 d2))
                    [[y1 m1 d1]]
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
      (LOG/info (str "(Harvest) Processing files from: " from-date " to: " to-datex))
      (doseq [cur-dir items] (process-dir cur-dir)))))


(defn iv-harvest [^File f,
                  ^StockPrice spot,
                  calls,
                  puts]

  (try
    (LOG/info (str "(IvHarvest) Hit on file: " (.getPath f)
                ", date: " (.getDx spot)
                ", time: " (.getSqlTime spot)))
    (DB/with-session DerivativeMapper
      (do
        ;(.insertSpot it spot)
        (doseq [c calls]
          (println (str "Option id: " (.getDerivativeId c))))))
    ;(.insertDerivativePrice it c))))
    (catch Exception e (LOG/error (str "[" (.getPath f) "] "(.getMessage e))))))
;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *repos* (@s :repos)
              *user-tix* (.getTickers ctx)]
      (doif .isQuery ctx (let [tix-s (db-tix nil)] (doseq [t tix-s] (println t))))
      (doif .isPaperHistory ctx (do-paper-history (@s :downloader)))
      (doif .isFeed ctx (do-feed))
      (doif .isSpot ctx (do-spot (@s :etrade)))
      (doif .isIvHarvest ctx
        (let [opx-tix (or *user-tix* (db-tix tcat-in-1-3))]
          (do-harvest-files-with iv-harvest (@s :etrade) opx-tix (.ivHarvestFrom ctx) (.ivHarvestTo ctx))))
      (doif .isUpdateDbOptions ctx (do-upd-derivatives (@s :etrade)))
      (doif .isOneTimeDownloadOptions ctx
        (let [dl (@s :downloader)
              opx-tix (or *user-tix* (db-tix tcat-in-1-3))]
              ;opx-tix (or *user-tix* (db-tix #(= 1 (.getTickerCategory %))))]
          (doseq [t opx-tix]
            (LOG/info (str "One-time download of " t))
            (.downloadDerivatives dl t))))
      (doif .isSpotFromDownloadedOptions ctx (do-spots-from-downloaded-options (@s :manager) (@s :opxhtmlparser)))
      (doif .isRollingOptions ctx
        (let [opening-time (COM/str->date (.getOpen ctx))
              closing-time (COM/str->date (.getClose ctx))
              dl (@s :downloader)
              opx-tix (db-tix tcat-in-1-3)
              ;opx-tix (db-tix #(= 1 (.getTickerCategory %)))
              rollopt-run (fn []
                            (doseq [t opx-tix]
                              (.downloadDerivatives dl t)))]
            (block-task (time-less-than opening-time) (* 10 60 1000))
            (while-task (time-less-than closing-time) (* 60000 (.getRollingInterval ctx)) rollopt-run)
            (System/exit 0)))
        )))

;(use '[clojure.contrib.monads :only [defmonad domonad]])

;(defmonad error-m
;    [m-result identity
;     m-bind   (fn [m f] (if (has-failed? m)
;                         m
;                         (f m)))])
;
;
;
;http://brehaut.net/blog/2011/error_monads
