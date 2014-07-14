(ns scaffold
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
    [netfondsjanitor.janitors.DefaultJanitor :as JAN]
    [maunakea.financial.Netfonds2 :as N2])
  (:use
    [netfondsjanitor.service.common :only (*feed* *locator*)]
    [clojure.string :only [split join]]))


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

(def ^:dynamic *base-url* "http://www.vg.no") ;"https://news.ycombinator.com/")

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn hn-headlines []
  (map html/text (html/select (fetch-url *base-url*) [:td.title :a])))

(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "dlstockoptions.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn dl []
  (.getBean (factory) "downloader"))

(defn loc []
  (.getBean (factory) "locator"))

(defn dlm []
  (.getBean (factory) "downloadMaintenanceAspect"))

;(def tix (binding [*locator* (loc)] (JAN/db-tix)))

(def tix JAN/db-tix)

(defn my-tix [f] (binding [*locator* (loc)] (JAN/db-tix f)))


