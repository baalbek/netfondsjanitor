(ns netfondsjanitor.statistics.stox
  (:import
    ;[oahu.financial StockPrice]
    [ranoraraku.beans StockPriceBean])
  (:require
    [clojure.java.io :as IO]
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.feed :as FEED]))

(defn marketval->volume [^StockPriceBean s]
  (let [avg (* 0.25 (+ (.getOpn s) (.getHi s) (.getLo s) (.getCls s)))
        result (/ (.getMarketValue s) avg)]
    result))

(defn vol-diff-pct [s]
  (let [market-vol (marketval->volume s)
        real-vol (.getVolume s)
        diff (- market-vol real-vol)]
    (* 100.0 (/ diff real-vol))))

(defn to-r [ticker]
  (let [feed (.getFeedStoreDir (.getBean COM/*spring* "downloadMaintenanceAspect"))
        beans (remove #(= (.getVolume %) 0) (FEED/get-all-lines ticker))
        diffs (map vol-diff-pct beans)
        orig-vols (map #(.getVolume %) beans)]
    (with-open [wrt (IO/writer (str feed "/" ticker "_R"))]
      (.write wrt "D\tV\n")
      (doseq [[x1 x2] (map vector diffs orig-vols)] 
        (.write wrt (str x1 "\t" x2 "\n"))))))

