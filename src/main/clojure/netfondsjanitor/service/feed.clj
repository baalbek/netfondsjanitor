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
  (with-open [rdr (IO/reader (str *feed* "/" ticker ".txt"))]
    (doall (take-while filter-fn (rest (map line-fn (line-seq rdr)))))))


(defn str->list [line]
  (split line #"\s+"))

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

; 0date	       1paper 2exch	3      4open	  5high	  6low	   7close	   8volume	9value
; ["20130102" "YAR" "Oslo" "Bors" "277.70" "279.00" "275.70" "276.80" "839375" "232598528"]

(defn line->stockpricebean [^Stock stock l]
  (let [[dx ticker _ _ opn hi lo cls vol market-val] l
        bean ^StockPriceBean (StockPriceBean.)]
    (doto bean
      (.setLocalDx (parse-date dx))
      (.setOpn (str->double opn))
      (.setHi (str->double hi))
      (.setLo (str->double lo))
      (.setCls (str->double cls))
      (.setVolume (str->int vol))
      (.setMarketValue (str->double market-val))
      (.setStock stock)
      )
    bean))


(defn get-lines [ticker]
  (let [stock (.findStock *repos* ticker)
        ;max-dx (DB/get-max-dx)
        ;cur-dx (max-dx (.findId *locator* ticker))
        cur-filter (if (nil? stock)
                     (fn [_] true)
                     (let [cur-dx (DB/get-max-dx (.getOid stock))]
                        (partial line-filter cur-dx)))
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
