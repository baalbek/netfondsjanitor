(ns netfondsjanitor.service.db
  (:import
    [maunakea.util MyBatisUtils]
    [org.joda.time DateMidnight]
    [netfondsjanitor.model.mybatis StockMapper]
    [org.apache.ibatis.session SqlSession]))


(defn maxdx->map [mx]
  (loop [x mx result {}]
    (if (not (seq x))
      result
      (let [m (first x)
            tix (.get m "ticker_id")
            dx (DateMidnight. (.getTime (.get m "max_dx")))]
        (recur (rest x) (assoc result tix dx))))))

(defn get-max-dx []
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)
        result (.selectMaxDate mapper)]
    (doto session .commit .close)
    (maxdx->map result)))


(defn update-stockprices [stock-beans]
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)]
    (doseq [^StockBean x stock-beans]
      (.insertStockPrice mapper x))
    (doto session .commit .close)))