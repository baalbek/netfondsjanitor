(ns netfondsjanitor.janitors.dbharvester
  (:import
    [oahu.financial.janitors JanitorContext]
    [oahu.financial.repository StockMarketRepository]
    [oahu.financial OptionCalculator])
  (:use
    [netfondsjanitor.service.common :only (*user-tix* *test-run* *repos*)]))


(defn do-harvest [^JanitorContext ctx
                  ^OptionCalculator calculator]
  (let [from-date (.harvestFrom ctx)
        to-date (.harvestTo ctx)
        options (.findOptionPricesStockTix *repos* *user-tix* from-date to-date)]
    (with-session DerivativeMapper
      (doseq [x options]
        (try
          (.insertBlackScholes it (.getPriceId x) (.ivBuy x calculator) (.ivSell x calculator))
          (catch Exception ex))))))

;(println (.ivCall calculator (.getSpot x) (.getStrike x) (.getYears x) (.getBuy x)))
