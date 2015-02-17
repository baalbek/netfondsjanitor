(ns netfondsjanitor.janitors.DefaultJanitor
  (:gen-class
    :init init
    :state state
    :implements [oahu.financial.janitors.Janitor]
    :methods [
               [setFeedStoreDir [String] void]
               [setStockMarketRepos [oahu.financial.repository.StockMarketRepository] void]
               [setDownloader [oahu.financial.html.EtradeDownloader] void]
               [setDownloadManager [oahu.financial.html.DownloadManager] void]
               [setEtrade [oahu.financial.repository.EtradeDerivatives] void]
               ]
    )
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *feed* *repos* *test-run*)])
  (:import
    [oahu.financial.html OptionsHtmlParser]
    [java.io File FileNotFoundException]
    [java.time LocalTime]
    [com.gargoylesoftware.htmlunit.html HtmlPage]
    [ranoraraku.models.mybatis StockMapper]
    [ranoraraku.beans StockPriceBean]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial Stock StockPrice]
    [oahu.financial.repository EtradeDerivatives]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.html EtradeDownloader]
    [oahu.financial.html DownloadManager])
  (:require
    [maunakea.financial.htmlutil :as hu]
    [netfondsjanitor.janitors.harvester :as HARV]
    [netfondsjanitor.service.common :as COM]
    [netfondsjanitor.service.logservice :as LOG]
    [netfondsjanitor.service.db :as DB]
    [netfondsjanitor.service.feed :as FEED]))

;;;------------------------------------------------------------------------
;;;-------------------------- gen-class methods ---------------------------
;;;------------------------------------------------------------------------
(defn -init []
  [[] (atom {})])

(defn set-property [this k v]
  (let [s (.state this)]
    (swap! s assoc k v)))

(defn -setStockMarketRepos [this value]
  (set-property this :repos value))

(defn -setFeedStoreDir [this value]
  (set-property this :feed value))

(defn -setEtrade [this value]
  (set-property this :etrade value))

(defn -setDownloader [this value]
  (set-property this :downloader value))

(defn -setDownloadManager [this value]
  (set-property this :manager value))

(comment

  (defn -setOptionsHtmlParser [this value]
    (set-property this :opxhtmlparser value))
  )

;;;------------------------------------------------------------------------
;;;-------------------------- Cloure methods ---------------------------
;;;------------------------------------------------------------------------


(defn do-paper-history [^EtradeDownloader downloader]
  (let [tix-s (or *user-tix* (COM/db-tix nil))]
    (doseq [t tix-s]
      (LOG/info (str "Will download paper history for " t))
      (.downloadPaperHistory downloader t))))

(defn do-feed []
  (let [tix-s (or *user-tix* (COM/db-tix nil))]
    (doseq [t tix-s]
      (try
        (let [cur-lines  (FEED/get-lines t)
              num-beans (count cur-lines)]
          (if (> num-beans 0)
            (do
              (LOG/info (str "Will insert " num-beans " for " t))
              (DB/update-stockprices cur-lines))
            (LOG/info (str "No beans for " t))))
        (catch FileNotFoundException fe
          (LOG/warn (str "No feed for ticker " t ": " (.getMessage fe))))
        (catch Exception e
          (LOG/fatal (str "Unexpected error: " (.getMessage e) " aborting"))
          (System/exit 0))))))


(defn do-spot [^EtradeDerivatives etrade]
  (let [tix-s (COM/db-tix COM/tcat-in-1-3)
        stocks (COM/map-java-fn .getSpot etrade tix-s)]
    (DB/with-session StockMapper
      (doseq [^StockPriceBean s stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            (.insertStockPrice ^StockMapper it s)
            ))))))

(defn do-spots-from-downloaded-options [^DownloadManager manager, ^EtradeDerivatives etrade]
  (let [
        tix-s (or *user-tix* (COM/db-tix (partial COM/tcat-in-1-3)))
        pages (COM/map-tuple-java-fn .getLastDownloadedFile manager tix-s)
        ]
    (if (= *test-run* true)
      (doseq [[^String ticker, ^File page] pages]
        (let [^StockPrice s (.getSpot2 etrade page ticker)]
          (println "Test run for: " ticker ", page: " page ", ticker from html: " (-> s .getStock .getTicker))))
    (DB/with-session StockMapper
      (doseq [[^String ticker, ^File page] pages]
        (let [^StockPrice s (.getSpot2 etrade page ticker)]
          (LOG/info (str "Inserting stock price: " s ))
          (.insertStockPrice it s)
          ;(println "Here we are: " (-> s .getStock .getTicker) ",opn: " (.getOpn s) ", hi: " (.getHi s) ", lo: " (.getLo s) ", cls: " (.getCls s))
          ))))))

(defn block-task [test wait]
  (while (test)
    (LOG/info "Market not open yet...")
    (Thread/sleep wait)))

(defn while-task [test wait f]
  (while (test) (do (f) (Thread/sleep wait))))

(defn time-less-than [cur-time]
  (fn []
    (< (.compareTo (LocalTime/now) cur-time) 0)))

(defmacro doif [java-prop ctx & body]
  `(if (= (~java-prop  ~ctx) true)
    ~@body))


;;;------------------------------------------------------------------------
;;;-------------------------- Interface methods ---------------------------
;;;------------------------------------------------------------------------
(defn -run [this, ^JanitorContext ctx]
  (LOG/info (.toString ctx))
  (let [s (.state this)]
    (binding [*feed* (@s :feed)
              *repos* (@s :repos)
              *user-tix* (.getTickers ctx)
              *test-run* (.isTestRun ctx)]
      (doif .isQuery ctx (let [tix-s (COM/db-tix nil)] (doseq [t tix-s] (println t))))
      (doif .isPaperHistory ctx (do-paper-history (@s :downloader)))
      (doif .isFeed ctx (do-feed))
      (doif .isSpot ctx (do-spot (@s :etrade)))
      (doif .isIvHarvest ctx
        (HARV/do-harvest-files-with HARV/iv-harvest (@s :etrade) ctx))
        ;(if (= *test-run* true)
        ;  (HARV/do-harvest-files-with HARV/harvest-test-run (@s :etrade) ctx)
        ;  (HARV/do-harvest-files-with HARV/iv-harvest (@s :etrade) ctx)))
      (doif .isUpdateDbOptions ctx
        (HARV/do-harvest-files-with HARV/harvest-derivatives (@s :etrade) ctx))
      (doif .isOneTimeDownloadOptions ctx
        (let [^EtradeDownloader dl (@s :downloader)
              opx-tix (or *user-tix* (COM/db-tix COM/tcat-in-1-3))]
          (doseq [t opx-tix]
            (LOG/info (str "One-time download of " t))
            (.downloadDerivatives dl t))))
      (doif .isSpotFromDownloadedOptions ctx (do-spots-from-downloaded-options (@s :manager) (@s :etrade)))
      (doif .isRollingOptions ctx
        (let [opening-time (COM/str->date (.getOpen ctx))
              closing-time (COM/str->date (.getClose ctx))
              ^EtradeDownloader dl (@s :downloader)
              opx-tix (COM/db-tix COM/tcat-in-1-3)
              rollopt-run (fn []
                            (doseq [t opx-tix]
                              (.downloadDerivatives dl t)))]
            (block-task (time-less-than opening-time) (* 10 60 1000))
            (while-task (time-less-than closing-time) (* 60000 (.getRollingInterval ctx)) rollopt-run)
            (System/exit 0)))
        )))

;(use '[clojure.contrib.monads :only [defmonad domonad]])

;(defmonad error-m
;    [m-result identity
;     m-bind   (fn [m f] (if (has-failed? m)
;                         m
;                         (f m)))])
;
;
;
;http://brehaut.net/blog/2011/error_monads
