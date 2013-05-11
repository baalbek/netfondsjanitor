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


(defmacro with-session [& body]
  `(let [session# ^SqlSession (MyBatisUtils/getSession)
         ~'it ^StockMapper (.getMapper session# StockMapper)]
    ~@body
    (doto session# .commit .close)))

(comment
  (defmacro make [v & body]
    (let [value-sym (gensym)]
      `(let [~value-sym (* 2 v)]
         ~@(replace {:value value-sym} body))))
(defmacro make [v & body]
  `(let [~'the-value ~(some-calc v)]
     ~@body))

  (defmacro nth-c [n coll]
    `(loop [n# ~n coll# ~coll]
       (if (= n# 0)
         (first coll#)
         (recur (dec n#) (rest coll#)))))
)