(ns netfondsjanitor.janitors.DefaultJanitor
  (:gen-class
    :init init
    :state state
    :implements [oahu.financial.janitors.Janitor]
    :methods [
               [setFeedStoreDir [String] void]
               [setStockLocator [oahu.financial.StockLocator] void]
               ]
    )
  (:use
    [netfondsjanitor.service.common :only (*feed* *locator*)])
  (:import
    [oahu.financial StockLocator]
    [oahu.financial.janitors JanitorContext])
  (:require
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
;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *locator* (@s :locator)]
      (let [my-tix (or (.getTickers ctx) (map #(.getTicker %) (.getTickers *locator*)))]
        ))))
