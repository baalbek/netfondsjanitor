(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [oahu.financial Etrade StockTicker]
    [java.io FileNotFoundException])
  (:require
      [netfondsjanitor.service.logservice :as LOG]
      [netfondsjanitor.service.db :as DB]
      [netfondsjanitor.service.feed :as FEED])
  (:use
    [netfondsjanitor.cli :only (cli)]))


(defmacro map-java-fn [map-fn java-obj lst]
  `(map #(~map-fn ~java-obj %) ~lst))


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
        (println parsed-args))
    (do
      (let [
            f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))
            stockticker ^StockTicker (.getBean f "stockticker")
            tix (.getTickers stockticker)
            ]

        (if (check-arg :ivharvest)
            (println (.findId  stockticker "YAR")))
        (if (check-arg :spot)
          (let [etrade ^Etrade (.getBean f "etrade")
                tix-list (.getBean f "ticker-list")
                stocks (map-java-fn .getSpot etrade tix-list)]
            (doseq [s stocks]
              (println (.getTicker s)))))
        (if (check-arg :feed)
          (doseq [t tix]
            (try
              (let [cur-lines  (FEED/get-lines t)
                    num-beans (count cur-lines)]
                (LOG/info (str "Will insert " num-beans " for " t))
                (if (> num-beans 0)
                  (DB/update-stockprices cur-lines)))
              (catch FileNotFoundException fe
                (LOG/warn (str "No feed for ticker " t)))
              (catch Exception e
                (LOG/fatal (str "Unexpected error: " (.getMessage e) " aborting"))
                (System/exit 0)))))

        )))))


(main *command-line-args*)
