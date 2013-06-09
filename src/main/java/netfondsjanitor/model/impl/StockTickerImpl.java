package netfondsjanitor.model.impl;

import maunakea.financial.beans.StockBean;
import maunakea.util.MyBatisUtils;
import netfondsjanitor.model.mybatis.StockMapper;
import oahu.financial.Stock;
import oahu.financial.StockTickerIdMap;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/6/13
 * Time: 6:17 PM
 */
public class StockTickerImpl implements StockTickerIdMap {
    private HashMap<Integer,String> id2ticker;
    private HashMap<String,Integer> ticker2id;
    private List<String> tickers;

    @Override
    public String findTicker(int tickerId) {
        if (id2ticker == null) {
            populate();
        }

        return id2ticker.get(tickerId);
    }


    @Override
    public Integer findId(String ticker) {
        if (ticker2id == null) {
            populate();
        }

        return ticker2id.get(ticker);
    }

    @Override
    public List<String> getTickers() {
        if (tickers == null) {
            populate();
        }
        return tickers;
    }

    private void populate() {
        id2ticker = new HashMap<>();
        ticker2id = new HashMap<>();
        tickers = new ArrayList<>();

        SqlSession session = MyBatisUtils.getSession();

        StockMapper mapper = session.getMapper(StockMapper.class);

        List<Stock> tix = mapper.selectStocks();


        for (Stock b : tix) {
            id2ticker.put(b.getId(), b.getTicker());
            ticker2id.put(b.getTicker(), b.getId());
            tickers.add(b.getTicker());
        }
        session.commit();
        session.close();
    }

}
