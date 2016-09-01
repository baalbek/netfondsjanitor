package netfondsjanitor.aspects;

import netfondsjanitor.aspects.validation.ValidateHarvestDerivativePrice;
import oahu.financial.DerivativePrice;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: rcs
 * Date: 5/2/13
 * Time: 1:34 PM
 */

@Aspect
public class ValidateBeansAspect {
    Logger log = Logger.getLogger(getClass().getPackage().getName());

    private Double spreadLimit = null;
    private Integer daysLimit = 0;

    @Pointcut("execution(Collection<DerivativePrice> oahu.financial.repository.EtradeRepository.*(String))")
    public void getCallsPutsPointcut() {
    }

    @SuppressWarnings("unchecked")
    @Around("getCallsPutsPointcut()")
    public Collection<DerivativePrice> 
    getCallsPutsPointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        Collection<DerivativePrice> items = (Collection<DerivativePrice>)jp.proceed();
        /*
        Collection<DerivativePrice> result =
                items.stream().filter(
                        p -> derivativePriceValidation.apply(p)
                ).collect(Collectors.toCollection(ArrayList::new));
        //*/
        Collection<DerivativePrice> result = new ArrayList<>();

        if (derivativePriceValidation == null) {
            derivativePriceValidation = new ValidateHarvestDerivativePrice();
        }

        for (DerivativePrice p : items) {
            if (derivativePriceValidation.apply(p)) {
                result.add(p);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Option passed ok: %s", p.getDerivative().getTicker()));
                }
            }
            else if (log.isDebugEnabled()) {
                log.debug(String.format("Option fail: %s", p.getDerivative().getTicker()));
            }
        }
        return result;
    }

    private Function<DerivativePrice,Boolean> derivativePriceValidation;

    public void setDerivativePriceValidation(Function<DerivativePrice, Boolean> derivativePriceValidation) {
        this.derivativePriceValidation = derivativePriceValidation;
    }
}
