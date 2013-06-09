package netfondsjanitor.model.mybatis;

import maunakea.financial.beans.StockBean;
import oahu.financial.Stock;
import oahu.financial.StockPrice;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/18/13
 * Time: 8:52 PM
 */

public interface StockMapper {
    void insertStockPrice(Stock bean);

    List<StockPrice> selectStockprices(@Param("tickerId") int tickerId,
                                 @Param("fromDx") Date fromDx);

    List<Map<Integer,Date>> selectMaxDate();

    List<Stock> selectStocks();

    List<Stock> selectStocksWithPrices(@Param("tickerIds") List<Integer> tickerIds,
                                      @Param("fromDx") Date fromDx);

}
