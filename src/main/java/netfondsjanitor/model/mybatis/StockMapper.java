package netfondsjanitor.model.mybatis;

import oahu.financial.beans.StockBean;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/18/13
 * Time: 8:52 PM
 */

public interface StockMapper {
    void insertStockPrice(StockBean bean);
}
