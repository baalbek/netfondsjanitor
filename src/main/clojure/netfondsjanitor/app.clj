(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [org.apache.log4j PropertyConfigurator]
    [java.util Properties])
  (:use [waimea.cli :only (cli)]))


(defn initLog4j []
  (let [props (Properties.)
       clazz (.getClass props)
       resource (.getResourceAsStream clazz "/log4j.properties")]
    (.load props resource)
    (PropertyConfigurator/configure props)))

(defn main [args]
  (initLog4j)
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

      (doseq [t tix-list] (println t))
      )))

(main *command-line-args*)