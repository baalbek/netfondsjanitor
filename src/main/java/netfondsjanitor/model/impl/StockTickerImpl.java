package netfondsjanitor.model.impl;

import maunakea.util.MyBatisUtils;
import netfondsjanitor.model.mybatis.StockTickerMapper;
import oahu.financial.StockTicker;
import oahu.financial.beans.StockTickerBean;
import org.apache.ibatis.session.SqlSession;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/6/13
 * Time: 6:17 PM
 */
public class StockTickerImpl implements StockTicker {
    private HashMap<Integer,String> id2ticker;
    private HashMap<String,Integer> ticker2id;

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

    private void populate() {
        id2ticker = new HashMap<>();
        ticker2id = new HashMap<>();

        SqlSession session = MyBatisUtils.getSession();

        StockTickerMapper mapper = session.getMapper(StockTickerMapper.class);

        List<StockTickerBean> tix = mapper.selectTickers();

        for (StockTickerBean b : tix) {
            id2ticker.put(b.getId(), b.getTicker());
            ticker2id.put(b.getTicker(), b.getId());
        }
        session.commit();
        session.close();
    }

}
