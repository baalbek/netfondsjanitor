(ns scaffold
  (:import
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
    [maunakea.financial.NetfondsDerivatives :as DR]
    [maunakea.financial.htmlutil :as HU])
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
      (ClassPathXmlApplicationContext. "demorun.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn dl []
  (.getBean (factory) "downloader"))

(defn calc []
  (.getBean (factory) "calculator"))

(defn loc []
  (.getBean (factory) "locator"))

(defn dlm []
  (.getBean (factory) "downloadMaintenanceAspect"))

(defn opx []
  (let [f (clojure.java.io/file "../feed/2014/9/10/OBX.html")]
    (.getSpotCallsPuts2 (etrade) f)))

(comment
  (def get-derx (partial DR/get-derivatives (loc) (calc) (dl)))

  (def tix JAN/db-tix)

  (defn my-tix [f] (binding [*locator* (loc)] (JAN/db-tix f)))

  (def redami HU/read-date-time)

  (defn spot []
    (let [f (clojure.java.io/file "../feed/2014/9/10/OBX.html")
          snip (HU/snip-ticker f)
          ;result (HU/spot-from-snip-ticker snip)]
          ;result (:content (html/select snip [:#toptime]))]
          ;result (first (:content (first (html/select snip [:#toptime]))))
          result (HU/spot-from-snip-ticker snip)]
      result))
  )
