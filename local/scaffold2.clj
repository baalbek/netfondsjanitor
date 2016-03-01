(ns scaffold2
  (:import
    [java.sql Date]
    [java.time LocalDate LocalTime]
    [java.io File]
    [java.util ArrayList]
    [org.apache.ibatis.exceptions PersistenceException]
    [ranoraraku.models.mybatis DerivativeMapper]
    [vega.financial.calculator BlackScholes]
    [org.springframework.context.support ClassPathXmlApplicationContext])
  (:require
    [clojure.string :as cs]
    [maunakea.financial.htmlutil :as hu]
    [maunakea.financial.repository.NetfondsDerivatives :as hur]
    [netfondsjanitor.service.db :as DB]))

(def factory
  (memoize
    (fn []
      ;(ClassPathXmlApplicationContext. "netfondsjanitor.xml"))))
      (ClassPathXmlApplicationContext. "demorun.xml"))))

(defn get-bean [n]
  (.getBean (factory) n))

(defn etrade [] (get-bean  "etrade"))

(defn repos [] (get-bean  "repos"))

(defn calc [] (get-bean  "calculator"))

(defn html [ticker]
  (clojure.java.io/file (str "../feed/2015/2/9/" ticker ".html")))

(defn spotcp [ticker]
  (.getSpotCallsPuts2 (etrade) (html ticker)))

(def snipd hu/snip-derivatives)

(def snips hu/spot-from-snip-derivatives)

(def g (snipd (html "GJF")))

(def getdp hur/get-dprices)

(defn copx [spot]
  (DB/with-session DerivativeMapper
    (.countOpxPricesForSpot it spot)))


(defn spots [stock-id]
  (let [from-dx (Date/valueOf (LocalDate/of 2014 1 1))
        to-dx (Date/valueOf (LocalDate/of 2014 9 1))]
  (DB/with-session DerivativeMapper
    (.spotsOpricesStockId it stock-id from-dx to-dx))))

(defn opri [stock-id]
  (let [d0 (LocalDate/of 2014 1 1)
        d1 (LocalDate/of 2015 1 1)]
    (.findOptionPricesStockId (repos) stock-id d0 d1)))

(defn opri2 [stock-ids]
  (let [d0 (LocalDate/of 2014 1 1)
        d1 (LocalDate/of 2015 1 1)]
    (.findOptionPricesStockIds (repos) stock-ids d0 d1)))

(defn opri3 [stock-tix]
  (let [d0 (LocalDate/of 2014 1 1)
        d1 (LocalDate/of 2015 1 1)]
    (.findOptionPricesStockTix (repos) stock-tix d0 d1)))

(defn ivCall [spot x t price]
  (let [bs (BlackScholes.)]
    (.ivCall bs spot x t price)))

(defn tix []
  (let [result (ArrayList.)]
    (.add result "NHY")
    result))

(defn create-spot []
  (let [dx (LocalDate/of 2014 1 1)
        tx (LocalTime/of 18 0 0)]
    tx))
