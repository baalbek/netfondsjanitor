(ns netfondsjanitor.service.feed
  (:use
    [clojure.string :only (join split)]
    [netfondsjanitor.service.common :only (*feed* *repos*)])
  (:require
    [netfondsjanitor.service.db :as DB]
    [clojure.java.io :as IO])
  (:import
    [java.time LocalDate]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial Stock]
    [ranoraraku.beans StockPriceBean]))

;  (with-open [wrtr (writer "/tmp/test.txt")]
;    (.write wrtr "Line to be written"))

(defn parse-file [ticker line-fn filter-fn]
  (with-open [rdr (IO/reader (str *feed* "/" ticker ".csv"))]
    (doall (take-while filter-fn (rest (map line-fn (line-seq rdr)))))))


(defn str->list [line]
  (split line #","))

(defn str->int [s]
  (.intValue (Integer/parseInt s)))

(defn str->double [s]
  (.doubleValue (Double/parseDouble s)))

(defn parse-date [s]
  (let [[a y m d] (first (re-seq #"(\d\d\d\d)(\d\d)(\d\d)" s))]
    (LocalDate/of (str->int y) (str->int m) (str->int d))))

(defn line-filter [dx lx]
  (let [cur-dx (parse-date (first lx))]
    (if (<= (.compareTo cur-dx dx) 0)
      false
      true)))

; 0date	       1paper 2exch	     3open	  4high	  5low	   6close	   7volume	8value
; ["20130102" "YAR" "Oslo Bors" "277.70" "279.00" "275.70" "276.80" "839375" "232598528"]

(defn line->stockpricebean [^Stock stock l]
  (let [[dx ticker _ opn hi lo cls vol market-val] l
        bean ^StockPriceBean (StockPriceBean.)]
    (doto bean
      (.setLocalDx (parse-date dx))
      (.setOpn (str->double opn))
      (.setHi (str->double hi))
      (.setLo (str->double lo))
      (.setCls (str->double cls))
      (.setVolume (str->int vol))
      (.setMarketValue (str->double market-val))
      (.setStock stock))
    bean))


(defn get-lines-2 [ticker max-dx]
  (let [stock (.findStock *repos* ticker)
        cur-filter (if (nil? stock)
                     (fn [_] true)
                     (let [cur-dx (max-dx (.getOid stock))]
                        (partial line-filter cur-dx)))
        lx (parse-file
             ticker
             str->list
             cur-filter)]
    (map (partial line->stockpricebean stock) lx)))

(defn get-lines-1 [stock max-dx]
  (let [ticker (.getTicker stock)
        stock-dx (max-dx (.getOid stock))
        cur-filter (if (nil? stock-dx)
                     (fn [_] true)
                     (partial line-filter (max-dx (.getOid stock))))
        lx (parse-file
             ticker
             str->list
             cur-filter)]
    (map (partial line->stockpricebean stock) lx)))

(defn get-all-lines [ticker]
  (let [stock (.findStock *repos* ticker)
        cur-filter (fn [_] true)
        lx (parse-file
             ticker
             str->list
             cur-filter)]
    (map (partial line->stockpricebean stock) lx)))
