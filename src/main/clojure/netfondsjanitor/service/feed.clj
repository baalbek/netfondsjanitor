(ns netfondsjanitor.service.feed
  (:use
    [clojure.string :only (join split)]
    [netfondsjanitor.service.common :only (*spring*)])
  (:require
    [netfondsjanitor.service.db :as DB]
    [clojure.java.io :as IO])
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Stock StockLocator]
    [ranoraraku.beans StockPriceBean]
    [org.joda.time DateMidnight]))

;  (with-open [wrtr (writer "/tmp/test.txt")]
;    (.write wrtr "Line to be written"))

(defn parse-file [ticker line-fn filter-fn]
  (with-open [rdr (IO/reader (str "/home/rcs/opt/java/netfondsjanitor/html/" ticker ".txt"))]
    (doall (take-while filter-fn (rest (map line-fn (line-seq rdr)))))))


(defn str->list [line]
  (split line #"\s+"))

(defn str->int [s]
  (.intValue (Integer/parseInt s)))

(defn str->double [s]
  (.doubleValue (Double/parseDouble s)))

(defn parse-date [s]
  (let [[a y m d] (first (re-seq #"(\d\d\d\d)(\d\d)(\d\d)" s))]
    (DateMidnight. (str->int y) (str->int m) (str->int d))))

(defn line-filter [dx lx]
  (let [cur-dx (parse-date (first lx))]
    (if (<= (.compareTo cur-dx dx) 0)
      false
      true)))

; 0date	       1paper 2exch	3      4open	  5high	  6low	   7close	   8volume	9value
; ["20130102" "YAR" "Oslo" "Bors" "277.70" "279.00" "275.70" "276.80" "839375" "232598528"]

(defn line->stockpricebean [^Stock stock l]
  (let [[dx ticker _ _ opn hi lo cls vol _] l
        bean ^StockPriceBean (StockPriceBean.)]
    (doto bean
      (.setDxJoda (parse-date dx))
      (.setOpn (str->double opn))
      (.setHi (str->double hi))
      (.setLo (str->double lo))
      (.setCls (str->double cls))
      (.setVolume (str->int vol))
      (.setStock stock)
      )
    bean))


(defn get-lines [ticker]
  (let [
        locator ^StockLocator (.getBean *spring* "stocklocator")
        max-dx (DB/get-max-dx)
        cur-dx (max-dx (.findId locator ticker))
        cur-filter (if (nil? cur-dx)
                     (fn [_] true)
                     (partial line-filter cur-dx))
        lx (parse-file
             ticker
             str->list
             cur-filter)]
    (map (partial line->stockpricebean (.locateStock locator ticker)) lx)))

