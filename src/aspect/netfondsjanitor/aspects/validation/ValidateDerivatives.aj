package netfondsjanitor.aspects.validation;

import java.util.Collection;

import java.util.function.Function;

import oahu.financial.Derivative;
import oahu.financial.repository.EtradeRepository;

public aspect ValidateDerivatives {

    pointcut validateDerivatives () : execution(public Collection<Derivative> EtradeRepository.*(..));


    Collection<Derivative> around() : validateDerivatives() {
        System.out.println("around() : validateDerivatives() aspect...");

        Collection<Derivative> result = proceed();

        if (validateDerivatives != null) {
            result = validateDerivatives.apply(result);
        }
        return result;
    }

    Function<Collection<Derivative>,Collection<Derivative>> validateDerivatives;

    public void
    setValidateDerivatives
        (Function<Collection<Derivative>,Collection<Derivative>> validateDerivatives) {
            this.validateDerivatives = validateDerivatives;
    }
}
