(ns netfondsjanitor.janitors.DefaultJanitor
  (:gen-class
    :init init
    :state state
    :implements [oahu.financial.janitors.Janitor]
    :methods [
               [setFeedStoreDir [String] void]
               [setStockLocator [oahu.financial.StockLocator] void]
               [setDownloader [oahu.financial.html.EtradeDownloader] void]
               [setEtrade [oahu.financial.Etrade] void]
               ]
    )
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *feed* *locator*)])
  (:import
    [java.io FileNotFoundException]
    [java.time LocalTime]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockBean StockPriceBean DerivativeBean]
    [oahu.financial StockLocator Etrade]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.html EtradeDownloader])
  (:require
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]
    [netfondsjanitor.service.feed :as FEED]))

;;;------------------------------------------------------------------------
;;;-------------------------- gen-class methods ---------------------------
;;;------------------------------------------------------------------------
(defn -init []
  [[] (atom {})])

(defn -setStockLocator [this, ^StockLocator value]
  (let [s (.state this)]
    (swap! s assoc :locator value)))

(defn -setFeedStoreDir [this, ^String value]
  (let [s (.state this)]
    (swap! s assoc :feed value)))

(defn -setEtrade [this, ^Etrade value]
  (let [s (.state this)]
    (swap! s assoc :etrade value)))

(defn -setDownloader [this, ^EtradeDownloader value]
  (let [s (.state this)]
    (swap! s assoc :downloader value)))

;;;------------------------------------------------------------------------
;;;-------------------------- Cloure methods ---------------------------
;;;------------------------------------------------------------------------

(def db-tix (memoize 
  (fn [f]
    (println (str "db-tix first time " f))
    (let [tix (if (nil? f)
                (.getTickers *locator*)
                (filter f (.getTickers *locator*)))
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

(defn do-spot [^Etrade etrade]
  (let [tix-s (db-tix #(= 1 (.getTickerCategory %)))
        stocks (COM/map-java-fn .getSpot etrade tix-s)]
    (DB/with-session StockMapper
      (doseq [s ^StockPriceBean stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            (.insertStockPrice it s)
            ))))))

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
;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *locator* (@s :locator)
              *user-tix* (.getTickers ctx)]
      (doif .isFeed ctx (do-feed))
      (doif .isQuery ctx (let [tix-s (db-tix nil)] (doseq [t tix-s] (println t))))
      (doif .isPaperHistory ctx (do-paper-history (@s :downloader)))
      (doif .isSpot ctx (do-spot (@s :etrade)))
      (doif .isRollingOptions ctx 
        (let [opening-time (COM/str->date (.getOpen ctx))
              closing-time (COM/str->date (.getClose ctx))
              dl (@s :downloader)
              opx-tix (db-tix #(= 1 (.getTickerCategory %)))
              rollopt-run (fn [] 
                            (doseq [t opx-tix]
                              (.downloadDerivatives dl t)))]
            (block-task (time-less-than opening-time) (* 10 60 1000))
            (while-task (time-less-than closing-time) (* 60000 (.getRollingInterval ctx)) rollopt-run)
            (System/exit 0)))
        )))
