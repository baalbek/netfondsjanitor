package netfondsjanitor.service;

import maunakea.financial.beans.CalculatedDerivativeBean;
import oahu.exceptions.NotImplementedException;
import oahu.financial.OptionCalculator;
import oahu.financial.beans.DerivativeBean;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 1/6/13
 * Time: 3:32 PM
 */
public class BlackScholesCalculator implements OptionCalculator {

    private  kalihiwai.financial.OptionCalculator helper;

    public BlackScholesCalculator() {}

    private double yearsToExpiry(CalculatedDerivativeBean d) {
        return d.daysProperty().get()/365.0;
    }

    @Override
    public double delta(DerivativeBean d) {
        throw new NotImplementedException();
    }

    @Override
    public double spread(DerivativeBean d) {
        throw new NotImplementedException();
    }

    @Override
    public double breakEven(DerivativeBean d) {
        throw new NotImplementedException();
    }

    @Override
    public double stockPriceFor(double optionPrice, DerivativeBean d, int priceType) {
        throw new NotImplementedException();
    }

    @Override
    public double iv(DerivativeBean d, int priceType) {
        CalculatedDerivativeBean cd = (CalculatedDerivativeBean)d;

        double price = priceType == DerivativeBean.BUY ? cd.getBuy() : cd.getSell();
        return cd.getOpType() == DerivativeBean.CALL ?
                helper.ivCall(price, cd.getParent().getValue(), cd.getX(), 0.05, yearsToExpiry(cd)) :
                helper.ivPut(price, cd.getParent().getValue(), cd.getX(), 0.05, yearsToExpiry(cd));
    }

    public kalihiwai.financial.OptionCalculator getHelper() {
        return helper;
    }

    public void setHelper(kalihiwai.financial.OptionCalculator helper) {
        this.helper = helper;
    }
}
