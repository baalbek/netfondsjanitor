package netfondsjanitor.validation;

import oahu.financial.DerivativePrice;

import java.util.Collection;
import java.util.function.Function;

public class ValidateDerivativePrices1
    implements Function<Collection<DerivativePrice>,Collection<DerivativePrice>> {

    @Override
    public Collection<DerivativePrice> 
    apply(Collection<DerivativePrice> items) {
        items.removeIf(this::isNotOk);
        return items;
    }

    private boolean isNotOk(DerivativePrice p) {
        if (p.getBuy() <= 0) {
            return true;
        }
        if (p.getSell() <= 0) {
            return true;
        }
        if (p.getDerivative() == null) {
            return true;
        }
        if (p.getStockPrice() == null) {
            return true;
        }
        return false;
    }
}
