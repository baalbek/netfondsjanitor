(ns netfondsjanitor.service.db
  (:import
    [java.time LocalDate]
    [oahu.financial StockPrice]
    [maunakea.util MyBatisUtils]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans DerivativeBean]
    [org.apache.ibatis.session SqlSession])
  (:require
    [netfondsjanitor.service.logservice :as LOG]))


(defn maxdx->map [mx]
  (loop [x mx result {}]
    (if (not (seq x))
      result
      (let [m (first x)
            tix (.get m "ticker_id")
            dx (.toLocalDate (.get m "max_dx"))]
        (recur (rest x) (assoc result tix dx))))))

(defmacro with-session [mapper & body]
  `(let [session# ^SqlSession (MyBatisUtils/getSession)
         ~'it (.getMapper session# ~mapper)
         result# ~@body]
    (doto session# .commit .close)
      result#))

(defn insert-derivatives [ds]
  (with-session DerivativeMapper
    (let [will-insert
          (fn [^DerivativeBean x]
            (if (= 0 (.countDerivative it (.getTicker x)))
              true
              false))]
      (doseq [d ds]
        (if (= true (will-insert d))
          (do
            (LOG/info (str "Will insert " (.getTicker d)))
            (.insertDerivative it d))
          (LOG/info (str (.getTicker d) " already exists")))))))

(defn update-stockprices [stock-beans]
  (with-session StockMapper
    (doseq [^StockPrice x stock-beans]
      (.insertStockPrice it x))))

(defn get-max-dx []
  (let [result (with-session StockMapper
                 (.selectMaxDate it))]
    (maxdx->map result)))

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
