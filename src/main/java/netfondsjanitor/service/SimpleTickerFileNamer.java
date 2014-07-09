package netfondsjanitor.service;

import java.util.function.Function;

/**
 * Created by rcs on 7/7/14.
 */
public class SimpleTickerFileNamer implements Function<Object,String> {
    @Override
    public String apply(Object o) {
        return String.format("%s.html", o);
    }
}
