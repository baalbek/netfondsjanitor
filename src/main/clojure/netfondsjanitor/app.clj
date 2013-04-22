(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [org.apache.log4j PropertyConfigurator]
    [org.apache.commons.logging LogFactory]
    [java.util Properties]
    [maunakea.util MyBatisUtils]
    [netfondsjanitor.model.mybatis StockMapper])
  (:use [netfondsjanitor.cli :only (cli)]))

(defn initLog4j []
  (let [lf (LogFactory/getFactory)
        props (Properties.)
        clazz (.getClass props)
        resource (.getResourceAsStream clazz "/log4j.properties")]
    (.setAttribute lf "org.apache.commons.logging.Log" "org.apache.commons.logging.impl.NoOpLog")
    (.load props resource)
    (PropertyConfigurator/configure props)))


(defmacro map-java-fn [map-fn java-obj lst]
  `(map #(~map-fn ~java-obj %) ~lst))

(defn update-stockprices [stock-beans]
  (let [session (MyBatisUtils/getSession)
        mapper (.getMapper session StockMapper)]
    (println mapper)
    (doseq [x stock-beans]
      (println x)
      (.insertStockPrice mapper x))
    (doto session .commit .close)))

(defn main [args]
  (initLog4j)
  (let [parsed-args-vec (cli args
                          ["-h" "--[no-]help" "Print cmd line options and quit" :default false]
                          ["-x" "--xml" "Spring xml filename" :default "netfondsjanitor.xml"]
                          )
        parsed-args (first parsed-args-vec)]
    (if (= (:help parsed-args) true)
      (do
        (println parsed-args-vec)
        (println parsed-args))
      (do
        (let [f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))
              etrade (.getBean f "etrade")
              tix-list (.getBean f "ticker-list")]
              (update-stockprices (map-java-fn .getSpot etrade tix-list)))))))

;(defn scaffold []
;  (let [f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. "netfondsjanitor.xml")
;        etrade (.getBean f "etrade")]
;    etrade))

(main *command-line-args*)
