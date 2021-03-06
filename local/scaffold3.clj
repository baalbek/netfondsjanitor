(ns scaffold3
  (:import
    [java.sql Date]
    [java.time LocalDate LocalTime]
    [java.io File]
    [java.util ArrayList]
    [org.apache.ibatis.exceptions PersistenceException]
    [ranoraraku.beans StockBean StockPriceBean]
    [ranoraraku.models.mybatis DerivativeMapper]
    [vega.financial.calculator BlackScholes]
    [org.springframework.context.support ClassPathXmlApplicationContext])
  (:use
    [clojure.string :only (join split)]
    [netfondsjanitor.service.common :only (*user-tix* *feed* *repos* *test-run* *calculator* *cache*)])
  (:require
    [netfondsjanitor.service.feed :as FEED]
    [netfondsjanitor.janitors.harvester :as harv]
    [clojure.string :as cs]
    [netfondsjanitor.service.db :as DB]))

(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "netfondsjanitor.xml"))))
      ;(ClassPathXmlApplicationContext. "harvest.xml"))))

(defn get-bean [n]
  (.getBean (factory) n))

(defn etrade [] (get-bean  "etrade"))

(defn repos [] (get-bean  "repos"))

(defn calc [] (get-bean  "calculator"))

(defn html [ticker]
  (clojure.java.io/file (str "../feed/2015/2/9/" ticker ".html")))

(defn create-spot []
  (let [dx (LocalDate/of 2015 11 30)
        tx (LocalTime/of 17 59 0)
        result (StockPriceBean. dx tx 10 12 9 10 1999)
        stock-bean (StockBean.)]
    (.setOid stock-bean 3)
    (.setStock result stock-bean)
    result))


(defn find-spot [s]
  (DB/with-session DerivativeMapper
    (.findSpotId ^DerivativeMapper it ^StockPrice s)))

;(def f (File. "/home/rcs/opt/java/netfondsjanitor/feed/2015/12/17/YAR.html"))
(def f (File. "/home/rcs/opt/java/netfondsjanitor/feed/2015/9/30/YAR.html"))

(def redo harv/redo-harvest-spots-and-optionprices)

(defn exec-redo []
  (redo f (etrade)))

(defn str->list [line]
  (split line #","))


(defn stox []
  (.getStocks (repos)))

(defn feed []
  (binding [*feed* "../feed"
            *repos* (repos)]
    (let [max-dx-dict (DB/get-max-dx)
          sx (stox)
          s (first sx)]
      (let [cur-lines (FEED/get-linesx s max-dx-dict)]
        (println cur-lines)))))
      ;(doseq [s sx]
      ;  (println (.getTicker s))
      ;  (let [cur-lines (FEED/get-linesx s max-dx)]
      ;    (println cur-lines)])))


    ;(FEED/parse-file
    ;(let [lx (FEED/get-lines "YAR")]
    ;  (println lx))))
