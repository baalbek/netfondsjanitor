(ns netfondsjanitor.janitors.dbharvester
  (:import [oahu.financial.janitors JanitorContext]))


(defn do-harvest [^JanitorContext ctx]
  (println (.harvestFrom ctx) " - " (.harvestTo ctx) " - " (.getTickers ctx)))
