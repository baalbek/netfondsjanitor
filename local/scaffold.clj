(ns scaffold
  (:import
    [java.time LocalDate LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Stock]
    [ranoraraku.models.mybatis DerivativeMapper]
    [ranoraraku.beans StockPriceBean])
  (:require
    [net.cgrand.enlive-html :as html]
    [maunakea.financial.htmlutil :as HU]
    [maunakea.financial.repository.NetfondsDerivatives :as ND]
    [netfondsjanitor.janitors.harvester :as HARV]
    [netfondsjanitor.janitors.DefaultJanitor :as JAN]
    [netfondsjanitor.service.db :as DB])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *repos*)]
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

;(defn fetch-url [url]
;  (html/html-resource (java.net.URL. url)))

;(defn hn-headlines []
;  (map html/text (html/select (fetch-url *base-url*) [:td.title :a])))

;String getCompanyName();
;String getTicker();
;int getTickerCategory();
;jint getOid();
;List<StockPrice> getPrices();
;List<Derivative> getDerivatives();


(defn stock-impl []
  (reify Stock
    (getCompanyName [this] "TEST")
    (getTicker [this] "TEST")
    (getTickerCategory [this] 1)
    (getOid [this] 3)
    (getPrices [this] nil)
    (getDerivatives [this] nil)))

(defn spot-id []
  (let [s (StockPriceBean. (LocalDate/of 2015 2 2) (LocalTime/of 18 00) 0 0 0 0 0)]
    (.setStock s (stock-impl))
    (DB/with-session DerivativeMapper
      ;(.insertSpot it s))))
      (.findSpotId it ^StockPriceBean s))))

(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "netfondsjanitor.xml"))))
;(ClassPathXmlApplicationContext. "demorun.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn dl []
  (.getBean (factory) "downloader"))

(defn calc []
  (.getBean (factory) "calculator"))

(defn repos []
  (.getBean (factory) "repos"))

(defn dlm []
  (.getBean (factory) "downloadMaintenanceAspect"))

(def ddd JAN/do-spots-from-downloaded-options)

(defn test-ddd [] 
  (binding [*user-tix* nil
            *repos* (repos)]
    (ddd (dlm) (etrade))))

(def tix HU/ticker-from-snip-derivatives)

(def sel html/select)

(defn html [ticker]
  (clojure.java.io/file (str "../feed/2015/2/9/" ticker ".html")))

(defn opx [ticker]
  (.getSpotCallsPuts2 (etrade) (html ticker)))

(def harvest HARV/do-harvest-files-with)

(def iv-harvest HARV/iv-harvest)


(def ifd HARV/items-between-dates)
(def yb HARV/year-begin)
(def ye HARV/year-end)
(def mb HARV/month-begin)
(def me HARV/month-end)
(def ad HARV/all-days)

(def harvest-deriv HARV/harvest-derivatives)

(def from-date "2015-1-9")
(def to-date "2015-1-15")

(defn test-run [tix]
  (harvest HARV/harvest-test-run (etrade) tix from-date to-date))

(def opx-exp HU/opx-exp)

(def snip-d HU/snip-derivatives)

(defn snix [ticker]
  (HU/snip-derivatives (html ticker)))

(defn snips [ticker]
  (HU/opx-snips (HU/snip-derivatives (html ticker))))

(def cpd HU/create-callput-def)


(comment opnames [ticker]
  (let [opx (snips ticker)]
    (remove #(nil? %) (map HU/opx-name opx))))

(comment dprices [ticker]
  (ND/get-dprices (repos) (calc) (html ticker)))

(defn callput-defs [ticker]
  (ND/get-callput-defs (repos) (dl) ticker))

(comment
  (def long-months [1 3 5 7 8 10 12])

  (def short-months [4 6 9 11])

  (defmacro in? [v items]
    `(some #(= ~v %) ~items))
  ;`(let [hit# (some #(= ~v %) ~items)]
  ;   (if (nil? hit#) false true)))

  (defn correct-date? [d m]
    (cond
      (in? m short-months) (<= d 30)
      (= m 2) (<= d 28)
      :else true))

  ;(let [a (for ;[mx (drop m (range 13))

  (defn pm_ [range-fn rr y m d]
    (for [dx (range-fn d rr) :when (correct-date? dx m)]
      [y m dx]))

  (def pme (partial pm_ drop (range 32)))
  (def pmb (partial pm_ take (range 1 32)))

  (defn all-days [y]
    (fn [m]
      (for [dx (range 1 32) :when (correct-date? dx m)]
        [y m dx])))

  (defn full-year [y]
    (for [mx (range 1 13) dx (range 1 32) :when (correct-date? dx mx)] [y mx dx]))

  (defn pye [y m d]
    (let [a (pme y m d)
          b (for [mx (range (+ m 1) 13)
                  dx (range 1 32) :when (correct-date? dx mx)]
              [y mx dx])]
      (concat a b)))

  (defn pyb [y m d]
    (let [a (for [mx (range 1 m)
                  dx (range 1 32) :when (correct-date? dx mx)]
              [y mx dx])
          b (pmb y m d)]
      (concat a b)))

  (defn flatten-1
    [x]
    (filter #(and (sequential? %) (not-any? sequential? %))
      (rest (tree-seq #(and (sequential? %) (some sequential? %)) seq x))))

  (defn test-pyx [y1 m1 d1 y2 m2 d2]
    (cond
      (and (= y1 y2) (= m1 m2) (= d1 d2))
      "all same"
      (= y1 y2)
      (let [months (drop 1 (range m1 m2))
            a (pme y1 m1 d1)
            b (flatten-1 (map (all-days y1) months))
            c (pmb y2 m2 d2)]
        (concat a b c))
      :else (let [years (drop 1 (range y1 y2))
                  a (pye y1 m1 d1)
                  b (flatten-1 (map full-year years))
                  c (pyb y2 m2 d2)]
              (concat a b c))))

  )




; Column  |  Type   |                    Modifiers
;---------+---------+--------------------------------------------------
; oid     | integer | not null default nextval('iv_oid_seq'::regclass)
; spot_id | integer | not null
; opx_id  | integer | not null
; buy     | price   | not null
; sell    | price   | not null
; iv_buy  | imp_vol | not null
; iv_sell | imp_vol | not null
;
;   Column  |          Type          |                     Modifiers
;   ----------+------------------------+----------------------------------------------------
;    oid      | integer                | not null default nextval('spot_oid_seq'::regclass)
;    stock_id | integer                | not null
;    dx       | date                   | not null
;    tm       | time without time zone | not null
;    price    | price                  | not null
;

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
