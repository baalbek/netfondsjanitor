package netfondsjanitor.model.mybatis;

import oahu.financial.Stock;
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

    List<Stock> selectTicker(@Param("tickerId") int tickerId,
                                 @Param("fromDx") Date fromDx);

    List<Map<Integer,Date>> selectMaxDate();

}
