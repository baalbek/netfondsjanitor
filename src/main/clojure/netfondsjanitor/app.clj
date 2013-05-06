(ns netfondsjanitor.app
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]
    [org.apache.log4j PropertyConfigurator]
    [org.apache.commons.logging LogFactory]
    [java.util Date Properties]
    [org.apache.ibatis.session SqlSession]

    [oahu.financial.beans StockBean]
    [oahu.financial Etrade]
    [maunakea.financial.beans CalculatedDerivativeBean]
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

(defn new-date [y m d]
  (Date. (- y 1900) (- m 1) d))

(defmacro map-java-fn [map-fn java-obj lst]
  `(map #(~map-fn ~java-obj %) ~lst))

(defn update-stockprices [stock-beans]
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)]
    (println mapper)
    (doseq [^StockBean x stock-beans]
      (println x)
      (.insertStockPrice mapper x))
    (doto session .commit .close)))

(defn select-ticker-id [id]
  (let [session ^SqlSession (MyBatisUtils/getSession)
        mapper ^StockMapper (.getMapper session StockMapper)
        result (.selectTicker mapper id (new-date 2013 1 1))]
    result))

(defn main [args]
  (initLog4j)
  (let [parsed-args-vec (cli args
                          ["-h" "--[no-]help" "Print cmd line options and quit" :default false]
                          ["-x" "--xml" "Spring xml filename" :default "netfondsjanitor.xml"]
                          ["-i" "--[no-]ivharvest" "Harvesting implied volatility" :default false]
                          ["-s" "--[no-]spot" "Update stockprices" :default false]
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
            ]

        (if (check-arg :ivharvest)
            (do
              (println "Iv harvestin'" f)))

        (if (check-arg :spot)
          (let [etrade ^Etrade (.getBean f "etrade")
                tix-list (.getBean f "ticker-list")
                stocks (map-java-fn .getSpot etrade tix-list)]
            (println stocks))))))))



    (comment
    (if (= (:help parsed-args) true)
      (do
        (println parsed-args-vec)
        (println parsed-args))
      (do
        (let [f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. (:xml parsed-args))
              etrade ^Etrade (.getBean f "etrade")
              tix-list (.getBean f "ticker-list")]
              (update-stockprices (map-java-fn .getSpot etrade tix-list)))))
    )

;(defn scaffold []
;  (let [f ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. "netfondsjanitor.xml")
;        etrade (.getBean f "etrade")]
;    etrade))

(main *command-line-args*)
