(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [org.apache.log4j PropertyConfigurator]
    [org.apache.commons.logging LogFactory]
    [java.util Properties]
    [maunakea.util MyBatisUtils])
  (:use [waimea.cli :only (cli)]))

(defn initLog4j []
  (let [lf (LogFactory/getFactory)
        props (Properties.)
        clazz (.getClass props)
        resource (.getResourceAsStream clazz "/log4j.properties")]
    (.setAttribute lf "org.apache.commons.logging.Log" "org.apache.commons.logging.impl.NoOpLog")
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
      (println (MyBatisUtils/getSession))
      (doseq [t tix-list] (println t))
      )))

(main *command-line-args*)