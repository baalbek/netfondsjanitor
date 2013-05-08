(ns netfondsjanitor.service.dbservice
  (:import
    [maunakea.util MyBatisUtils]
    [netfondsjanitor.model.mybatis StockMapper]
    [org.apache.ibatis.session SqlSession]))


(defn update-stockprices [stock-beans]
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)]
    (println mapper)
    (doseq [^StockBean x stock-beans]
      (println x)
      (.insertStockPrice mapper x))
    (doto session .commit .close)))