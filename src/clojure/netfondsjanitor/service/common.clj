(ns netfondsjanitor.service.common
  (:import
    [java.time LocalTime])
  (:require
    [clojure.string :as cs]))

;(def ^:dynamic *spring*)
;(def ^:dynamic *janitor-context*)

(def ^:dynamic *feed*)

(def ^:dynamic *repos*)

(def ^:dynamic *user-tix*)

(def ^:dynamic *test-run*)

(def ^:dynamic *calculator*)

(def ^:dynamic *cache*)

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

(defmacro in? [v items]
  `(some #(= ~v %) ~items))

(def db-tix
  (memoize
    (fn [f]
      (println (str "db-tix first time " f))
      (let [stocks (.getStocks *repos*)
            tix (if (nil? f)
                  stocks
                  (filter f stocks))
            tix-s (map #(.getTicker %) tix)]
        tix-s))))

(defn tcat-in [in-vals v]
  (let [category (.getTickerCategory v)]
    (some #{category} in-vals)))

(def tcat-in-1-3 (partial tcat-in [1 3]))

;------------------------- kilauea --------------------------------
(defn as-get-set [^String p get-or-set prefix]
  (str prefix get-or-set (cs/upper-case (first p)) (.substring p 1)))

(defn p2kw [prop]
  (keyword (cs/lower-case prop)))

(defn getter [prop get-fn default]
  (if (nil? default)
    `(def ~get-fn
       (fn [this#]
         (let [cache# (.state this#)]
           (~prop @cache#))))
    `(def ~get-fn
       (fn [this#]
         (let [cache# (.state this#)
               val# (~prop @cache#)]
           (if (nil? val#)
             (do
               (swap! cache# assoc ~prop ~default)
               ~default)
             val#))))))

(defn setter [prop set-fn]
  `(def ~set-fn
     (fn [this# value#]
       (let [cache# (.state this#)]
         (swap! cache# assoc ~prop value#)))))

(defn getsetter [prop get-fn set-fn default]
  (if (nil? default)
    `(do
      (def ~set-fn
        (fn [this# value#]
          (let [cache# (.state this#)]
            (swap! cache# assoc ~prop value#))))
      (def ~get-fn
          (fn [this#]
            (let [cache# (.state this#)]
              (~prop @cache#)))))
    `(do
      (def ~set-fn
        (fn [this# value#]
          (let [cache# (.state this#)]
            (swap! cache# assoc ~prop value#))))
      (def ~get-fn
          (fn [this#]
            (let [cache# (.state this#)
                  val# (~prop @cache#)]
              (if (nil? val#)
                (do
                  (swap! cache# assoc ~prop ~default)
                  ~default)
                val#)))))))


(defmacro defprop [variants prop &
                   { :keys [prefix default]
                     :or {prefix "-"
                          default nil}}]
  (let [s-prop (p2kw prop)
        get-fn (symbol (as-get-set prop "get" prefix))
        set-fn (symbol (as-get-set prop "set" prefix))]
    (cond
      (= variants :getset)
      (getsetter s-prop get-fn set-fn default)
      (= variants :get)
      (getter s-prop get-fn default)
      (= variants :set)
      (setter s-prop set-fn))))
