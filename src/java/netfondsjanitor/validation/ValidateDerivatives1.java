package netfondsjanitor.validation;

import oahu.financial.Derivative;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by rcs on 02.10.16.
 *
 */
public class ValidateDerivatives1
    implements Function<Collection<Derivative>,Collection<Derivative>> {

    private Logger log = Logger.getLogger(getClass().getPackage().getName());

    @Override
    public Collection<Derivative> apply(Collection<Derivative> items) {
        items.removeIf(this::isNotOk);
        return items;
    }
    private boolean isNotOk(Derivative p) {
        if (p.getStock() == null) {
            warn(p,1);
            return true;
        }
        if (p.getTicker() == null) {
            warn(p,2);
            return true;
        }
        if (p.getExpiry() == null) {
            warn(p,3);
            return true;
        }
        if (p.getX() <= 0) {
            warn(p,4);
            return true;
        }
        if (p.getOpType() == null) {
            warn(p,5);
            return true;
        }
        if (p.getSeries() == null) {
            warn(p,6);
            return true;
        }
        return false;
    }

    private void warn(Derivative p, int wichCase) {
        String stem = "Derivative %s refused due to %s";
        String msg = null;
        String dn = p.getTicker() == null ? "[undefined]" : p.getTicker();
        switch (wichCase) {
            case 1:
                msg = String.format(stem,dn," getStock == null");
                break;
            case 2:
                msg = String.format(stem,dn," getTicker == null");
                break;
            case 3:
                msg = String.format(stem,dn," getExpiry == null");
                break;
            case 4:
                msg = String.format(stem,dn," getX <= 0");
                break;
            case 5:
                msg = String.format(stem,dn," getOpType == null");
                break;
            case 6:
                msg = String.format(stem,dn," getSeries == null");
                break;
        }
        log.warn(msg);
    }
}
