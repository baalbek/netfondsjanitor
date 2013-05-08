(ns netfondsjanitor.scaffold
  (:use
    [clojure.string :only (join split)])
  (:require
    [clojure.java.io :as IO])
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial StockTicker]
    [oahu.financial.beans StockBean StockTickerBean]
    [org.joda.time DateTime]))

;  (with-open [wrtr (writer "/tmp/test.txt")]
;    (.write wrtr "Line to be written"))

(defn parse-file [ticker line-fn filter-fn]
  (with-open [rdr (IO/reader (str "/home/rcs/opt/java/netfondsjanitor/feed/" ticker))]
    (doall (take-while filter-fn (rest (map line-fn (line-seq rdr)))))))


(defn str->list [line]
  (split line #"\s+"))

(defn str->int [s]
  (.intValue (Integer/parseInt s)))

(defn str->double [s]
  (.doubleValue (Double/parseDouble s)))

(defn parse-date [s]
  (let [[a y m d] (first (re-seq #"(\d\d\d\d)(\d\d)(\d\d)" s))]
    (DateTime. (str->int y) (str->int m) (str->int d) 0 0 0)))

(defn line-filter [dx lx]
  (let [cur-dx (parse-date (first lx))]
    (if (<= (.compareTo cur-dx dx) 0)
      false
      true)))

; 0date	       1paper 2exch	3      4open	  5high	  6low	   7close	   8volume	9value
; ["20130102" "YAR" "Oslo" "Bors" "277.70" "279.00" "275.70" "276.80" "839375" "232598528"]

(defn line->stockbean [^StockTicker stock-ticker l]
  (let [[dx ticker _ _ opn hi lo cls vol _] l
        bean ^StockBean (StockBean.)]
    (doto bean
      (.setDx (.toDate (parse-date dx)))
      (.setOpn (str->double opn))
      (.setHi (str->double hi))
      (.setLo (str->double lo))
      (.setCls (str->double cls))
      (.setVolume (str->int vol))
      (.setStockTicker stock-ticker)
      (.setTickerId (.findId stock-ticker ticker)))
    bean))

(defn maxdx->map [mx]
  (loop [x mx result {}]
    (if (not (seq x))
      result
      (let [m (first x)
            tix (.get m "ticker_id")
            dx (DateTime. (.getTime (.get m "max_dx")))]
        (recur (rest x) (assoc result tix dx))))))

(defn get-max-dx []
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)
        result (.selectMaxDate mapper)]
    (doto session .commit .close)
    (maxdx->map result)))

(defn get-lines [ticker]
  (let [f ^ClassPathXmlApplicationContext
        (ClassPathXmlApplicationContext. "netfondsjanitor.xml")
        stock-ticker (.getBean f "stockticker")
        max-dx (get-max-dx)
        cur-dx (max-dx (.findId stock-ticker ticker))
        cur-filter (if (nil? cur-dx)
                     (fn [_] true)
                     (partial line-filter cur-dx))
        lx (parse-file
             ticker
             str->list
             cur-filter)]
    (map (partial line->stockbean stock-ticker) lx)))

