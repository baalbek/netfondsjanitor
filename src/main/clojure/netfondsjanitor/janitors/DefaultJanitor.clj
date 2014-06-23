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
    [netfondsjanitor.service.common :only (*feed* *locator*)])
  (:import
    [java.io FileNotFoundException]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockPriceBean DerivativeBean]
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
(defn do-paper-history [tix, ^EtradeDownloader downloader]
  (doseq [t tix]
    (LOG/info (str "Will download paper history for " t))
    (.downloadPaperHistory downloader t)))

(defn do-feed [tix]
  (doseq [t tix]
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
        (System/exit 0)))))

(defn do-spot [tix, ^Etrade etrade]
  (let [stocks (COM/map-java-fn .getSpot etrade tix)]
    (doseq [^StockPriceBean s stocks]
      (println (.getTicker s)))))

    (comment (DB/with-session StockMapper
      (doseq [s ^StockPriceBean stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            ;(.insertStockPrice it s)
            )))))

(defmacro doif [java-prop ctx & body]
  `(if (= (~java-prop  ~ctx) true)
    ~@body))
;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *locator* (@s :locator)]
      (let [tix (or (.getTickers ctx) (map #(.getTicker %) (.getTickers *locator*)))]
        (doif .isPaperHistory ctx (do-paper-history tix (@s :downloader)))
        (doif .isFeed ctx (do-feed tix))
        (doif .isSpot ctx (do-spot tix (@s :etrade)))
        (doif .isQuery ctx (doseq [t tix] (println t)))
        ))))
