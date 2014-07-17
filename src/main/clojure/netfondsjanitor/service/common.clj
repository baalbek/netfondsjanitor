(ns netfondsjanitor.service.common
  (:import
    [java.time LocalTime]))

;(def ^:dynamic *spring*)
;(def ^:dynamic *janitor-context*)

(def ^:dynamic *feed*)

(def ^:dynamic *locator*)

(def ^:dynamic *user-tix*)


(defn str->date [arg]
  (if-let [v (re-find #"(\d+):(\d+)" arg)]
    (let [hours (nth v 1)
          minutes (nth v 2)] 
      (println (str hours " " minutes))
      (LocalTime/of (read-string hours) (read-string minutes)))))

(defmacro map-java-fn [map-fn java-obj lst]
  `(map #(~map-fn ~java-obj %) ~lst))


(defmacro map-tuple-java-fn [map-fn java-obj lst]
  `(let [tupled# (fn [arg#]
                  (let [result# (~map-fn ~java-obj arg#)]
                    [arg# result#]))]
     (map tupled# ~lst)))

