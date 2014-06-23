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
    [oahu.financial StockLocator Etrade]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.html EtradeDownloader])
  (:require
    [netfondsjanitor.service.logservice :as LOG]
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

;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *locator* (@s :locator)]
      (let [tix (or (.getTickers ctx) (map #(.getTicker %) (.getTickers *locator*)))]
        (if (= (.isPaperHistory ctx) true)
          (do-paper-history tix (@s :downloader)))
        ))))
