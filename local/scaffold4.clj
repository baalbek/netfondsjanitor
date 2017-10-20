(ns scaffold4
  (:import
    [org.springframework.context.support ClassPathXmlApplicationContext])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *feed* *repos* *test-run* *calculator* *cache*)])
  (:require
    [netfondsjanitor.janitors.DefaultJanitor :as j]))


(def factory
  (memoize
    (fn []
      (ClassPathXmlApplicationContext. "htmlspot.xml"))))

(defn etrade []
  (.getBean (factory) "etrade"))

(defn contenthandler []
  (.getBean (factory) "contenthandler"))

(defn x []
  (binding [*feed* "../feed"
            ;*user-tix* ["YAR" "NHY" "STL"]
            *user-tix* ["YAR"]]
    (j/do-spots-from-downloaded-options (etrade))))
