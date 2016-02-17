(ns netfondsjanitor.service.springservice
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext]))

(def ^:dynamic *factory*)


(defn with-spring [& body]
  `(binding [*factory*
            ^ClassPathXmlApplicationContext (ClassPathXmlApplicationContext. "netfondsjanitor.xml")]
    ~@body))

