(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [maunakea.financial.impl NetfondsDownloader])
  (:use [waimea.cli :only (cli)]))

(defn main [args]
  (let [parsed-args-vec (cli args
                          ["-x" "--xml" "Spring xml filename" :default "netfondsjanitor.xml"]
                          ["-d" "--[no-]defaultTickers" "Use tickers from xml file" :default true]
                          ["-t"

                           "--tickers" "Tickers (comma-separated) from cli"])
        parsed-args (first parsed-args-vec)]
    (let [f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))
          etrade (.getBean f "etrade")
          tix-list (.getBean f "ticker-list")]
      (println parsed-args)
      ;(println (.getHi (.getSpot etrade "NHY")))

      (println tix-list)
      )))

(main *command-line-args*)