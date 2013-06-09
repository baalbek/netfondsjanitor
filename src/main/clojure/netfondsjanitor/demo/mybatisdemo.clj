(ns netfondsjanitor.demo.mybatisdemo
  (:import
    [java.util ArrayList]
    [org.joda.time DateMidnight]
    [netfondsjanitor.model.mybatis StockMapper])
  (:require
    [netfondsjanitor.service.db :as DB]))


(defn prices []
  (DB/with-session StockMapper
    (let [tix (ArrayList.)
     dx (DateMidnight. 2013 3 1)]
      (doto tix
        (.add 3)
        (.add 4))
      (.selectStocksWithPrices it tix (.toDate dx)))))

(defn stocks []
  (DB/with-session StockMapper
    (.selectStocks it)))

(defn update []
  (DB/with-session StockMapper
    (prn "Hi")))
