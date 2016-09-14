package netfondsjanitor.validation;

import oahu.financial.DerivativePrice;

import java.util.Collection;
import java.util.function.Function;

import org.apache.log4j.Logger;
public class ValidateDerivativePrices1
    implements Function<Collection<DerivativePrice>,Collection<DerivativePrice>> {

    Logger log = Logger.getLogger(getClass().getPackage().getName());

    @Override
    public Collection<DerivativePrice> 
    apply(Collection<DerivativePrice> items) {
        items.removeIf(this::isNotOk);
        return items;
    }

    private boolean isNotOk(DerivativePrice p) {
        if (p.getBuy() <= 0) {
            warn(p,1);
            return true;
        }
        if (p.getSell() <= 0) {
            warn(p,2);
            return true;
        }
        if (p.getDerivative() == null) {
            warn(p,3);
            return true;
        }
        if (p.getStockPrice() == null) {
            warn(p,4);
            return true;
        }
        return false;
    }

    private void warn(DerivativePrice p, int wichCase) {
        String stem = "Derivative %s refused due to %s";   
        String msg = null;
        String dn = p.getDerivative() == null ? "[undefined]" : p.getDerivative().getTicker();
        switch (wichCase) {
            case 1:
                msg = String.format(stem,dn," getBuy < 0"); 
                break;
            case 2:
                msg = String.format(stem,dn," getSell < 0"); 
                break;
            case 3:
                msg = String.format(stem,dn," getDerivative == null"); 
                break;
            case 4:
                msg = String.format(stem,dn," getStockPrice == null"); 
                break;
        }
        log.warn(msg);
    }
}
