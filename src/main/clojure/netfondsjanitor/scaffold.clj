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
    [clojure.java.io :as io]
    [net.cgrand.enlive-html :as html]
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]
    [netfondsjanitor.service.feed :as FEED])
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

(def cache (atom {}))

(defn invalidate [ticker]
  (reset! cache (dissoc @cache ticker)))

(defn snip-ticker [ticker]
  (if-let [result (@cache ticker)]
    result
    (let [page (.downloadDerivatives (dl) ticker)
          new-result (html/html-snippet (-> page .getWebResponse .getContentAsString))]
      (reset! cache (assoc @cache ticker new-result))
      new-result)))

(def hsel html/select)

(def a= html/attr=)

(def snip html/html-snippet)

(defn stock-snip [ticker]
  (let [mysnip (snip-ticker ticker)]
    (remove #(= % "\n")
      (:content (last (html/select mysnip [[:table#updatetable1] [:tr]]))))))

(defn stock-vol [x]
  (let [vol-str (first (:content (nth x 9)))]
    (read-string (join (split vol-str #"\D")))))

(defn spot [x]
  (read-string (first (:content (nth x 0)))))

(defn stock-opn [x]
  (read-string (first (:content (nth x 5)))))

(defn stock-hi [x]
  (read-string (first (:content (nth x 6)))))

(defn stock-lo [x]
  (read-string (first (:content (nth x 7)))))

(defn stock-buy [x]
  (read-string (first (:content (first (:content (nth x 3)))))))

(defn stock-sell [x]
  (read-string (first (:content (first (:content (nth x 3)))))))

(defn opx-snips [ticker]
  (let [mysnip (snip-ticker ticker)
        rows (:content (first (html/select mysnip [:table [:.com :.topmargin]])))
        content-rows (map :content rows)
        rows-ex-nil (remove #(nil? %) content-rows)
        rm-eol (fn [v] (remove #(= % "\n") v))
        rows-ex-eol (map rm-eol rows-ex-nil)]
    (filter #(= (count %) 9) rows-ex-eol)))


(defn titem []
  (nth (opx "YAR") 18))

(defn output [fname ticker]
  (with-open [w (io/writer fname)]
    (let [content (opx ticker)]
      (doseq [line content]
        (.write w (str line))))))
    
(defn opx-name [o]
  (first (:content (first (:content (nth o 0))))))

(defn opx-type [o]
  (first (:content (nth o 1))))

(defn opx-x [o]
  (read-string (first (:content (nth o 2)))))

(defn opx-exp [o]
  (first (:content (nth o 3))))

(defn opx-buy [o]
  (read-string (first (:content (nth o 4)))))

(defn opx-sell [o]
  (read-string (first (:content (nth o 5)))))

