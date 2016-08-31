(ns scaffold
  (:import
    [oahu.dto Tuple2]
    [oahu.financial Derivative$LifeCycle]
    [java.time LocalDate LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [ranoraraku.models.mybatis DerivativeMapper]
    [ranoraraku.beans StockPriceBean])
  (:require
    [netfondsjanitor.janitors.harvester :as HARV]
    [netfondsjanitor.janitors.DefaultJanitor :as JAN]
    [netfondsjanitor.service.db :as DB])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *repos* *feed* *test-run*)]
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

(defn calls [ticker]
  (let [e (etrade)]
    (.calls e ticker)))

(defn dlx [ticker]
  (let [d (dl)]
    (.downloadDerivatives d ticker)))

(defn fx []
  (let [a (LocalDate/of 2016 1 1)
        b (LocalDate/of 2016 8 1)]
    (HARV/items-between-dates a b)))

(def fdx (LocalDate/of 2016 1 1))

(def tdx (LocalDate/of 2016 3 1))

(def feed-path "/home/rcs/opt/java/netfondsjanitor/feed")

(defn dhfw[]
  (binding [*feed* feed-path] 
    (HARV/do-harvest-files-with HARV/harvest-list-file (etrade) ["YAR"] fdx tdx)))

(def my-file (java.io.File. (str feed-path "/2016/8/16/YAR.html")))

(defn my-harv []
  (binding [*test-run* true]
    (HARV/harvest-derivatives my-file (etrade))))

(defn my-cp-defs-2 []
  (.callPutDefs (etrade) "YAR" my-file))

(defn my-cp-defs []
  (.callPutDefs (etrade) "YAR"))

(defn fromLifeCycle [defs lifeCycle]
  (filter #(= (.getLifeCycle %) lifeCycle) defs))

(defn fromHtml [defs]
  (fromLifeCycle defs Derivative$LifeCycle/FROM_HTML))

(defn fromDb [defs]
  (fromLifeCycle defs Derivative$LifeCycle/FROM_DATABASE))



