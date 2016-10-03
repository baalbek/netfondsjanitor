package netfondsjanitor.aspects.validation;

import java.util.Collection;

import java.util.function.Function;

import oahu.financial.DerivativePrice;
import oahu.financial.repository.EtradeRepository;

public aspect ValidateDerivativePrices {

    pointcut validatePrices() : execution(public Collection<DerivativePrice> EtradeRepository.*(..));


    Collection<DerivativePrice> around() : validatePrices() {
        System.out.println("around() : validatePrices() aspect...");

        Collection<DerivativePrice> result = proceed();

        if (validatePrices != null) {
            result = validatePrices.apply(result);
        }
        return result;
    }

    Function<Collection<DerivativePrice>,Collection<DerivativePrice>> validatePrices;

    public void
    setValidatePrices
        (Function<Collection<DerivativePrice>,Collection<DerivativePrice>> validatePrices) {
            this.validatePrices = validatePrices;
    }
}
