(ns netfondsjanitor.janitors.dbharvester
  (:import
    [ranoraraku.models.mybatis DerivativeMapper]
    [ranoraraku.beans.options SpotOptionPriceBean]
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial OptionCalculator])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *test-run* *repos* *calculator*)])
  (:require
    [netfondsjanitor.service.db :as DB]))


(defn insert-blackscholes [options]
  (if (= *test-run* true) 
    (doseq [^SpotOptionPriceBean x options]
      (try
        (println "Test run: insert " 
                (.getOpxName x) " - " 
                (.getPriceId x) " - ivBuy: " 
                (.ivBuy x *calculator*) " - ivSell: " 
                (.ivSell x *calculator*))
        (catch Exception ex (prn "Price id: " (.getPriceId x) ": " ex))))
    (DB/with-session DerivativeMapper
      (doseq [^SpotOptionPriceBean x options]
        (try
          (.insertBlackScholes it (.getPriceId x) (.ivBuy x *calculator*) (.ivSell x *calculator*))
          (catch Exception ex (prn "Price id: " (.getPriceId x) ": " ex)))))))

(defn do-harvest [^JanitorContext ctx]
  (let [from-date (.harvestFrom ctx)
        to-date (.harvestTo ctx)
        options (.findOptionPricesStockTix *repos* *user-tix* from-date to-date)]
    (insert-blackscholes options)))

;(println (.ivCall calculator (.getSpot x) (.getStrike x) (.getYears x) (.getBuy x)))
