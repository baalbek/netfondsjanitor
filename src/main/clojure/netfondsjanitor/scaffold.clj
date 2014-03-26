(ns netfondsjanitor.scaffold
  (:import
    [org.joda.time LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Etrade]
    [oahu.financial.html EtradeDownloader]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockPriceBean DerivativeBean]
    [java.io FileNotFoundException])
  (:require
      [net.cgrand.enlive-html :as html]
      [netfondsjanitor.service.common :as COM]
      [netfondsjanitor.service.logservice :as LOG]
      [netfondsjanitor.service.db :as DB]
      [netfondsjanitor.service.feed :as FEED]))


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

(def html-content
  (memoize
    (fn [ticker]
      (let [page (.downloadDerivatives (dl) ticker)]
        (-> page .getWebResponse .getContentAsString)))))


(defn my-fetch []
  (html/select (html-content "NHY") [:td]))

(def hsel html/select)

(def a= html/attr=)

(def snip html/html-snippet)

(defn opx [ticker]
  (let [mysnip (html/html-snippet (html-content ticker))]
    (:content (first (html/select mysnip [:table [:.com :.topmargin]])))))
