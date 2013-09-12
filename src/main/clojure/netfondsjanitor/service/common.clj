(ns netfondsjanitor.service.common
  (:import
    [org.joda.time LocalTime]))

(def ^:dynamic *spring*)

(defn str->date [arg]
  (if-let [v (re-find #"(\d+):(\d+)" arg)]
    (let [hours (nth v 1)
          minutes (nth v 2)] 
      (println (str hours " " minutes))
      (LocalTime. (read-string hours) (read-string minutes)))))
