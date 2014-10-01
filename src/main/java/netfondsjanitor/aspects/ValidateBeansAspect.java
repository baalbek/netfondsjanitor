package netfondsjanitor.aspects;

import oahu.domain.Tuple3;
import oahu.exceptions.BinarySearchException;
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

    @Pointcut("execution(* oahu.financial.EtradeDerivatives.getSpotCallsPuts2(java.io.File))")
    public void getSpotCallsPuts2Pointcut() {
    }

    @Around("getSpotCallsPuts2Pointcut()")
    public Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
    getSpotCallsPuts2PointcutMethod(ProceedingJoinPoint jp) throws Throwable {

        Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
                tmp = (Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>)jp.proceed();


        Collection<DerivativePrice> calls = tmp.second();
        Collection<DerivativePrice> validatedCalls = new ArrayList<>();
        Collection<DerivativePrice> puts = tmp.third();
        Collection<DerivativePrice> validatedPuts = new ArrayList<>();

        //log.info(String.format("%s\nNumber of puts: %d",jp.toString(),tmp.size()));

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

        /*
        if (cb.getParent() == null) {
            log.warn(String.format("%s: parent is null",ticker));
            return false;
        }
        */

        if (cb.getDays() < daysLimit) {
            log.info(String.format("%s has expired within %d days",ticker,daysLimit));
            return false;
        }

        if (cb.getBuy() <= 0) {
            log.info(String.format("%s: buy <= 0.0",ticker));
            return false;
        }

        if (cb.getSell() <= 0) {
            log.info(String.format("%s: sell <= 0.0",ticker));
            return false;
        }

        if (spreadLimit != null) {
            double spread = cb.getSell() - cb.getBuy();
            if (spread > spreadLimit.doubleValue()) {
                log.info(String.format("%s: spread (%.2f) larger than allowed (%.2f)",ticker,spread,spreadLimit));
                return false;
            }
        }

        try {
            if (cb.getIvSell() <= 0) {
                log.info(String.format("%s: ivSell <= 0.0",ticker));
                return false;
            }

            if (cb.getIvBuy() <= 0) {
                log.info(String.format("%s: ivBuy <= 0.0",ticker));
                return false;
            }
        }
        catch (BinarySearchException ex) {
            log.warn(String.format("%s: %s",ticker,ex.getMessage()));
            return false;
        }
        return true;
    }

    public Double getSpreadLimit() {
        return spreadLimit;
    }

    public void setSpreadLimit(Double spreadLimit) {
        this.spreadLimit = spreadLimit;
    }

    public Integer getDaysLimit() {
        return daysLimit;
    }

    public void setDaysLimit(Integer daysLimit) {
        this.daysLimit = daysLimit;
    }

}
