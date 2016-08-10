package netfondsjanitor.aspects;

import oahu.dto.Tuple3;
import oahu.exceptions.BinarySearchException;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Collection;

/**
 * Created by rcs on 17.02.15.
 *
 */

@Aspect
public class ValidateHarvestBeansAspect extends AbstractValidateBeans {
    Logger log = Logger.getLogger(getClass().getPackage().getName());

    @Pointcut("execution(* oahu.financial.repository.EtradeRepository.getSpotCallsPuts2(java.io.File))")
    public void getSpotCallsPuts2Pointcut() {
    }

    @Around("getSpotCallsPuts2Pointcut()")
    public Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
    getSpotCallsPuts2PointcutMethod(ProceedingJoinPoint jp) throws Throwable {
        return exec(jp);
    }

    protected boolean isOk(DerivativePrice cb) {
        try {
            if (cb.getBuy() <= 0) {
                log.warn(String.format("%s: buy <= 0.0",getTickerFor(cb)));
                return false;
            }

            if (cb.getSell() <= 0) {
                log.warn(String.format("%s: sell <= 0.0",getTickerFor(cb)));
                return false;
            }
        }
        catch (BinarySearchException ex) {
            log.warn(String.format("%s: %s",getTickerFor(cb),ex.getMessage()));
            return false;
        }
        return true;
    }
}
