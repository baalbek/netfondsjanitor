package netfondsjanitor.model.mybatis;

import oahu.financial.beans.StockBean;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/18/13
 * Time: 8:52 PM
 */

public interface StockMapper {
    void insertStockPrice(StockBean bean);

    List<StockBean> selectTicker(@Param("tickerId") int tickerId,
                                 @Param("fromDx") Date fromDx);

}
