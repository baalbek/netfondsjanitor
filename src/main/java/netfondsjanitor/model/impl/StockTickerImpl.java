package netfondsjanitor.model.impl;

import oahu.exceptions.NotImplementedException;
import oahu.financial.StockTicker;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/6/13
 * Time: 6:17 PM
 */
public class StockTickerImpl implements StockTicker {
    @Override
    public String findTicker(int tickerId) {
        throw new NotImplementedException();
    }

    @Override
    public int findId(String ticker) {
        throw new NotImplementedException();
    }
}
