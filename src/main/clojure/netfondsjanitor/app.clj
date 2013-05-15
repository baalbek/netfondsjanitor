(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Etrade StockTicker]
    [oahu.financial.beans StockBean]
    [netfondsjanitor.model.mybatis StockMapper]
    [java.io FileNotFoundException])
  (:require
      [netfondsjanitor.service.logservice :as LOG]
      [netfondsjanitor.service.db :as DB]
      [netfondsjanitor.service.feed :as FEED])
  (:use
    [netfondsjanitor.service.common :only (*spring*)]
    [netfondsjanitor.cli :only (cli)]))


(defmacro map-java-fn [map-fn java-obj lst]
  `(map #(~map-fn ~java-obj %) ~lst))


(defn do-spot [tix]
  (let [etrade ^Etrade (.getBean *spring* "etrade")
        stocks (map-java-fn .getSpot etrade tix)]
    (DB/with-session StockMapper
      (doseq [s ^StockBean stocks]
        (if-not (nil? s)
          (do
            (LOG/info (str "Will insert spot for " (.getTicker s)))
            (.insertStockPrice it s)
            ))))))

(comment
(defn do-spot [tix]
  (DB/with-session StockMapper
    (println it)))
  )

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
        (LOG/warn (str "No feed for ticker " t)))
      (catch Exception e
        (LOG/fatal (str "Unexpected error: " (.getMessage e) " aborting"))
        (System/exit 0)))))

(defn main [args]
  (LOG/initLog4j)
  (let [parsed-args-vec (cli args
                          ["-h" "--[no-]help" "Print cmd line options and quit" :default false]
                          ["-x" "--xml" "Spring xml filename" :default "netfondsjanitor.xml"]
                          ["-i" "--[no-]ivharvest" "Harvesting implied volatility" :default false]
                          ["-s" "--[no-]spot" "Update todays stockprices" :default false]
                          ["-f" "--[no-]feed" "Update stockprices from feed" :default false]
                          )
        parsed-args (first parsed-args-vec)
        check-arg (fn [arg]
                   (= (arg parsed-args) true))
       ]

    (if (check-arg :help)
      (do
        (println parsed-args-vec)
        (println parsed-args)
        (System/exit 0)))

    ;(if (check-arg :spot)
    ;  (do
    ;    (do-spot nil)
    ;    (System/exit 0)))

    (binding [*spring* ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))]
      (let [stockticker (.getBean *spring* "stockticker")
            tix (.getTickers stockticker)]

        (if (check-arg :ivharvest)
            (println tix))

        (if (check-arg :spot)
          (do-spot tix))

        (if (check-arg :feed)
          (do-feed tix))

      ))))


(main *command-line-args*)
