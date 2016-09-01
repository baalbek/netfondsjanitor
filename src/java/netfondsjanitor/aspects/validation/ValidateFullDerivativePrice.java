package netfondsjanitor.aspects.validation;

import oahu.exceptions.BinarySearchException;
import oahu.financial.DerivativePrice;
import org.apache.log4j.Logger;

import java.util.function.Function;

/**
 * Created by rcs on 01.09.16.
 *
 */
public class ValidateFullDerivativePrice implements Function<DerivativePrice,Boolean> {

    Logger log = Logger.getLogger(getClass().getPackage().getName());

    private int daysLimit;
    private Double spreadLimit;

    @Override
    public Boolean apply(DerivativePrice price) {
        if (price.getDerivative() == null) {
            return false;
        }

        String ticker = price.getDerivative().getTicker();
        if (price.getDays() < daysLimit) {
            log.info(String.format("%s has expired within %d days",ticker,daysLimit));
            return false;
        }

        if (price.getBuy() <= 0) {
            log.warn(String.format("%s: buy <= 0.0",ticker));
            return false;
        }

        if (price.getSell() <= 0) {
            log.warn(String.format("%s: sell <= 0.0",ticker));
            return false;
        }

        if (spreadLimit != null) {
            double spread = price.getSell() - price.getBuy();
            if (spread > spreadLimit) {
                log.info(String.format("%s: spread (%.2f) larger than allowed (%.2f)",ticker,spread,spreadLimit));
                return false;
            }
        }

        try {
            if (price.getIvSell() <= 0) {
                log.info(String.format("%s: ivSell <= 0.0",ticker));
                return false;
            }

            if (price.getIvBuy() <= 0) {
                log.info(String.format("%s: ivBuy <= 0.0",ticker));
                return false;
            }
        }
        catch (BinarySearchException ex) {
            log.warn(String.format("%s: %s",ticker,ex.getMessage()));
            return false;
        }
        return true;
    }

    public void setSpreadLimit(Double spreadLimit) {
        this.spreadLimit = spreadLimit;
    }

    public void setDaysLimit(int daysLimit) {
        this.daysLimit = daysLimit;
    }
}
