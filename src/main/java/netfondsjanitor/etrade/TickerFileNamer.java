package netfondsjanitor.etrade;

import java.util.function.Function;

/**
 * Created by rcs on 16.09.14.
 *
 */
public class TickerFileNamer implements Function<String,String> {
    private String index;
    @Override
    public String apply(String ticker) {
        return String.format("%s-%s.html",ticker,index);
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }
}
