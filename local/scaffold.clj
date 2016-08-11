(ns scaffold
  (:import
    [java.time LocalDate LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [ranoraraku.models.mybatis DerivativeMapper]
    [ranoraraku.beans StockPriceBean])
  (:require
    [netfondsjanitor.janitors.harvester :as HARV]
    [netfondsjanitor.janitors.DefaultJanitor :as JAN]
    [netfondsjanitor.service.db :as DB])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *repos*)]
    [clojure.string :only [split join]]))


(defn mz [f]
  (let [cache (atom {})]
    (with-meta
      (fn [arg0]
        (if-let [result (@cache arg0)]
          result
          (let [
                 new-result (f arg0)]
            (reset! cache (assoc @cache arg0 new-result))
            new-result)))
      {:cache cache})))


(comment stock-impl []
  (reify Stock
    (getCompanyName [this] "TEST")
    (getTicker [this] "TEST")
    (getTickerCategory [this] 1)
    (getOid [this] 3)
    (getPrices [this] nil)
    (getDerivatives [this] nil)))

(comment create-spot []
  (let [s (StockPriceBean. (LocalDate/of 2014 9 30) (LocalTime/of 17 59) 0 0 0 0 0)]
    (.setStock s (stock-impl))
    s))


(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "netfondsjanitor.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn dl []
  (.getBean (factory) "downloader"))

(defn calc []
  (.getBean (factory) "calculator"))

(defn repos []
  (.getBean (factory) "repos"))

(defn dlm []
  (.getBean (factory) "downloadMaintenanceAspect"))

