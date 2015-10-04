(ns netfondsjanitor.janitors.dbharvester
  (:import
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial OptionCalculator])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *test-run* *repos* *calculator*)]))


(defn insert-blackscholes [options]
  (with-session DerivativeMapper
    (doseq [x options]
      (try
        (.insertBlackScholes it (.getPriceId x) (.ivBuy x *calculator*) (.ivSell x *calculator*))
        (catch Exception ex (prn "Price id: " (.getPriceId x) ": " ex))))))

(defn do-harvest [^JanitorContext ctx]
  (let [from-date (.harvestFrom ctx)
        to-date (.harvestTo ctx)
        options (.findOptionPricesStockTix *repos* *user-tix* from-date to-date)]
    (insert-blackscholes options)))

;(println (.ivCall calculator (.getSpot x) (.getStrike x) (.getYears x) (.getBuy x)))
