(ns netfondsjanitor.app
  (:import
    [org.joda.time LocalTime]
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Etrade]
    [oahu.financial.html EtradeDownloader]
    [ranoraraku.models.mybatis StockMapper DerivativeMapper]
    [ranoraraku.beans StockPriceBean DerivativeBean]
    [java.io FileNotFoundException])
  (:require
      [clojure.string :as CSTR]
      [netfondsjanitor.service.common :as COM]
      [netfondsjanitor.service.logservice :as LOG]
      [netfondsjanitor.service.db :as DB]
      [netfondsjanitor.service.feed :as FEED]
      [netfondsjanitor.statistics.stox :as STOX])
  (:use
    [netfondsjanitor.service.common :only (*spring*)]
    [netfondsjanitor.cli :only (cli)]))


(defn do-spot [tix]
  (let [etrade ^Etrade (.getBean *spring* "etrade")
        stocks (COM/map-java-fn .getSpot etrade tix)]
    (DB/with-session StockMapper
      (doseq [s ^StockPriceBean stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            (.insertStockPrice it s)
            ))))))

(defn do-paper-history [tix]
  (let [downloader ^EtradeDownloader (.getBean *spring* "downloader")]
    (doseq [t tix]
      (LOG/info (str "Will download paper history for " t))
      (.downloadPaperHistory downloader t))))

(defn do-feed [tix]
  (doseq [t tix]
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
        (System/exit 0)))))

(defn do-ivharvest [tix])


(defn do-derivatives [tix]
  (let [etrade ^Etrade (.getBean *spring* "etrade")]
    (doseq [t tix]
      (LOG/info (str "Will update derivatives for " t))
      (let [calls (.getCalls etrade t)
            puts (.getPuts etrade t)]
        (DB/insert-derivatives calls)
        (DB/insert-derivatives puts)))))


(defn do-stats [tix]
  (doseq [t tix]
    (LOG/info (str "Calculating stats for " t))
    (STOX/to-r t)))

(defn block-task [test wait]
  (while (test)
    (LOG/info "Market not open yet...")
    (Thread/sleep wait)))

(defn while-task [test wait f]
  (while (test) (do (f) (Thread/sleep wait))))

(defn time-less-than [cur-time]
  (fn []
    (< (.compareTo (LocalTime.) cur-time) 0)))

(defn main [args]
  (LOG/initLog4j)
  (let [parsed-args-vec (cli args
                          ["-h" "--[no-]help" "Print cmd line options and quit" :default false]
                          ["-x" "--xml" "Spring xml filename" :default "netfondsjanitor.xml"]
                          ["-i" "--[no-]ivharvest" "Harvesting implied volatility" :default false]
                          ["-d" "--[no-]derivatives" "Update database with new options" :default false]
                          ["-s" "--[no-]spot" "Update todays stockprices" :default false]
                          ["-p" "--[no-]paper" "Download paper history" :default false]
                          ["-f" "--[no-]feed" "Update stockprices from feed" :default false]
                          ["-q" "--[no-]query" "Show active tickers" :default false]
                          ["-O" "--[no-]options" "Rolling download options for tix" :default false]
                          ["-I" "--[no-]stock-index" "Rolling download stock index OBX" :default false]
                          ["-T" "--time-interval" "Rolling download time interval in minutes" :default "30"]
                          ["-U" "--open" "Opening time for the market (hh:mm)" :default "9:30"]
                          ["-C" "--close" "Closing time for the market (hh:mm)" :default "17:20"]
                          ["-S" "--[no-]stats" "Calculate statistics." :default false]
                          ["-t" "--tickers" "If -S, select which tickers. Default: from database" :default nil]
                          )
        parsed-args (first parsed-args-vec)
        check-arg (fn [& args]
                    (some #(= (% parsed-args) true) args))
       ]

    (if (check-arg :help)
      (do
        (println parsed-args-vec)
        (System/exit 0)))

    (binding [*spring* ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))]
      (let [locator (.getBean *spring* "locator")
            tix (if (check-arg :options :stock-index) (.getTickers locator) (map #(.getTicker %) (.getTickers locator)))]

        (if (check-arg :query)
          (doseq [t tix]
            (println "Ticker: " t)))

        (if (check-arg :options :stock-index) 
          (let [opening-time (COM/str->date (:open parsed-args))
                closing-time (COM/str->date (:close parsed-args))
                dl (.getBean *spring* "downloader")
                run (if (check-arg :options)
                      ;---------------- :option -------------------
                      (let [opx-tix (map #(.getTicker %) (filter #(= (.getTickerCategory %) 1) tix))]
                        (fn [] 
                          (doseq [t opx-tix]
                            (.downloadDerivatives dl t))))
                      ;---------------- :stock-index ------------------------
                      (fn [] (doseq [t tix] (println t))))]
              (block-task (time-less-than opening-time) (* 10 60 1000))
              (while-task (time-less-than closing-time) (* 60000 (read-string (:time-interval parsed-args))) run)
              (System/exit 0)))

        (if (check-arg :ivharvest)
            (do-ivharvest tix))

        (if (check-arg :spot)
          (do-spot tix))

        (if (check-arg :paper)
          (do-paper-history tix))

        (if (check-arg :feed)
          (do-feed tix))

        (if (check-arg :derivatives)
          (do-derivatives tix))

        (if (check-arg :stats)
          (let [my-tix (if-let [parsed-tix (:tickers parsed-args)]
                          (CSTR/split parsed-tix #",")
                          (map #(.getTicker %) (.getTickers locator)))]
            (do-stats my-tix)))
        ))))



(main *command-line-args*)

(comment
(defn factory []
  (ClassPathXmlApplicationContext. "netfondsjanitor.xml"))

(defn etrade []
  (.getBean (factory) "etrade"))
  )
