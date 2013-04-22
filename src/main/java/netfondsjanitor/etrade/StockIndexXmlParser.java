package netfondsjanitor.etrade;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import oahu.exceptions.NotImplementedException;
import oahu.financial.EtradeStockParser;
import oahu.financial.beans.StockBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 4/21/13
 * Time: 1:01 PM
 */
class StockIndexXmlParser implements EtradeStockParser {
    @Override
    public StockBean parseSpot(HtmlPage htmlPage) {
        throw new NotImplementedException();
    }

    @Override
    public Map<String, StockBean> parseSpots(HtmlPage htmlPage) {
        Map<String, StockBean> result = new HashMap<>();



        return result;
    }
}
