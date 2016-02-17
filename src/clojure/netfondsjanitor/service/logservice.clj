(ns netfondsjanitor.service.logservice
  (:import
    [org.apache.log4j Logger PropertyConfigurator]
    [org.apache.commons.logging LogFactory]
    [java.util Properties]))

;(def ^:dynamic *logger*)

(def logger (Logger/getLogger  "netfondsjanitor"))

(defn initLog4j []
  (let [lf (LogFactory/getFactory)
        props (Properties.)
        clazz (.getClass props)
        resource (.getResourceAsStream clazz "/log4j.xml")]
    (.setAttribute lf "org.apache.commons.logging.Log" "org.apache.commons.logging.impl.NoOpLog")
    (.load props resource)
    (PropertyConfigurator/configure props)))


(defn fatal [msg]
  (.fatal logger msg))

(defn error [msg]
  (.error logger msg))

(defn warn [msg]
  (.warn logger msg))


(defn info [msg]
  (.info logger msg))


(defn debug [msg]
  (.debug logger msg))
