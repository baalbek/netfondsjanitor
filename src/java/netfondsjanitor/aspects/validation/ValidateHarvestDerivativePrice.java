package netfondsjanitor.aspects.validation;

import oahu.exceptions.BinarySearchException;
import oahu.financial.DerivativePrice;
import org.apache.log4j.Logger;

import java.util.function.Function;

/**
 * Created by rcs on 01.09.16.
 *
 */
public class ValidateHarvestDerivativePrice implements Function<DerivativePrice,Boolean> {

    Logger log = Logger.getLogger(getClass().getPackage().getName());

    @Override
    public Boolean apply(DerivativePrice p) {
        String ticker = p.getDerivative().getTicker();
        try {
            if (p.getBuy() <= 0) {
                log.warn(String.format("%s: buy <= 0.0",ticker));
                return false;
            }

            if (p.getSell() <= 0) {
                log.warn(String.format("%s: sell <= 0.0",ticker));
                return false;
            }
        }
        catch (BinarySearchException ex) {
            log.warn(String.format("%s: %s",ticker,ex.getMessage()));
            return false;
        }
        return true;
    }
}
