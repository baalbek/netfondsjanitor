(ns netfondsjanitor.scaffold
  (:import
    [org.joda.time LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Etrade StockPrice]
    [oahu.financial.html EtradeDownloader]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockPriceBean DerivativeBean]
    [java.io FileNotFoundException])
  (:require
    [clojure.java.io :as IO]
    [net.cgrand.enlive-html :as html]
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]
    [netfondsjanitor.service.feed :as FEED]
    [maunakea.financial.Netfonds2 :as N2])
  (:use
    [clojure.string :only [split join]]))



(def ^:dynamic *base-url* "http://www.vg.no") ;"https://news.ycombinator.com/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn hn-headlines []
  (map html/text (html/select (fetch-url *base-url*) [:td.title :a])))

(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "netfondsjanitor.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn dl []
  (.getBean (factory) "downloader"))

(defn loc []
  (.getBean (factory) "locator"))

(defn calc []
  (.getBean (factory) "calculator"))

(defn get-deriv [] (partial N2/get-derivatives (loc) (calc) (dl)))

(defn get-spot [] (partial N2/get-spot (loc) (dl)))

(defn osebx [] (N2/snip-osebx (dl)))

(defn html-sel [] html/select)

(defn mz [f]
  (let [cache (atom {})]
    (with-meta
      (fn [arg0]
        (if-let [result (@cache arg0)]
          result
          (let [
                 new-result (f arg0)]
            (reset! cache (assoc @cache arg0 new-result))
            new-result)))
      {:cache cache})))

(defn all-lines [ticker]  
  (binding [COM/*spring* (factory)]
    (FEED/get-all-lines ticker)))

(defn marketval->volume [^StockPriceBean s]
  (let [avg (* 0.25 (+ (.getOpn s) (.getHi s) (.getLo s) (.getCls s)))
        result (/ (.getMarketValue s) avg)]
    ;(println (str "avg " avg ", result " result)
    result))

(defn vol-diff-pct [s]
  (let [market-vol (marketval->volume s)
        real-vol (.getVolume s)
        diff (- market-vol real-vol)]
    (* 100.0 (/ diff real-vol))))

(defn to-r [ticker]
  (binding [COM/*spring* (factory)]
    (let [feed (.getFeedStoreDir (.getBean COM/*spring* "downloadMaintenanceAspect"))
          beans (FEED/get-all-lines ticker)
          diffs (map vol-diff-pct beans)
          orig-vols (map #(.getVolume %) beans)]
      (with-open [wrt (IO/writer (str feed "/" ticker "_R.txt"))]
        (.write wrt "DIFF\tVOLUME\n") 
        (doseq [[x1 x2] (map vector diffs orig-vols)] 
          ;(println x1 x2))))))
          (.write wrt (str x1 "\t" x2 "\n")))))))

