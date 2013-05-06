package netfondsjanitor.model.mybatis;

import oahu.financial.beans.StockTickerBean;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/6/13
 * Time: 6:20 PM
 */
public interface StockTickerMapper {
    List<StockTickerBean> selectTickers();
}
