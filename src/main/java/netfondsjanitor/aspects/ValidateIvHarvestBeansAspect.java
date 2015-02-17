package netfondsjanitor.aspects;

import oahu.domain.Tuple3;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rcs on 17.02.15.
 *
 */

@Aspect
public class ValidateIvHarvestBeansAspect {
    Logger log = Logger.getLogger(getClass().getPackage().getName());

    @Pointcut("execution(* oahu.financial.repository.EtradeDerivatives.getSpotCallsPuts2(java.io.File))")
    public void getSpotCallsPuts2Pointcut() {
    }

    @Around("getSpotCallsPuts2Pointcut()")
    public Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
    getSpotCallsPuts2PointcutMethod(ProceedingJoinPoint jp) throws Throwable {

        Tuple3<StockPrice, Collection<DerivativePrice>, Collection<DerivativePrice>>
                tmp = (Tuple3<StockPrice, Collection<DerivativePrice>, Collection<DerivativePrice>>) jp.proceed();

        Collection<DerivativePrice> calls = tmp.second();
        Collection<DerivativePrice> validatedCalls = new ArrayList<>();
        Collection<DerivativePrice> puts = tmp.third();
        Collection<DerivativePrice> validatedPuts = new ArrayList<>();

        for (DerivativePrice call : calls) {
            if (isOk(call) == false) continue;
            validatedCalls.add(call);
        }
        for (DerivativePrice put : puts) {
            if (isOk(put) == false) continue;
            validatedPuts.add(put);
        }

        Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
                result = new Tuple3<>(tmp.first(),validatedCalls,validatedPuts);
        return result;
    }

    private boolean isOk(DerivativePrice cb) {
        String ticker = cb.getDerivative().getTicker();

        if (cb.getBuy() <= 0) {
            log.info(String.format("%s: buy <= 0.0",ticker));
            return false;
        }

        if (cb.getSell() <= 0) {
            log.info(String.format("%s: sell <= 0.0",ticker));
            return false;
        }
        return true;
    }
}
