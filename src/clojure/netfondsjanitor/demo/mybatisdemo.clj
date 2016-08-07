(ns netfondsjanitor.demo.mybatisdemo
  (:import
    [java.util ArrayList]
    [java.time LocalDate]
    [ranoraraku.models.mybatis StockMapper])
  (:require
    [netfondsjanitor.service.db :as DB]))


(defn prices []
  (DB/with-session StockMapper
    (let [tix (ArrayList.)
     dx (LocalDate/of 2013 3 1)]
      (doto tix
        (.add 3)
        (.add 4))
      (.selectStocksWithPrices it tix (.toDate dx)))))

(defn stocks []
  (DB/with-session StockMapper
    (.selectStocks it)))

(defn xupdate []
  (DB/with-session StockMapper
    (prn "Hi")))
