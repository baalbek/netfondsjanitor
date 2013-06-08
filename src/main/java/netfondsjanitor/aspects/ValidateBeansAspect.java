package netfondsjanitor.aspects;

import oahu.exceptions.BinarySearchException;
import oahu.financial.Derivative;
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


    @Pointcut("execution(* oahu.financial.Etrade.getCalls(String))")
    public void getCallsPointcut() {
    }


    @Pointcut("execution(* oahu.financial.Etrade.getPuts(String))")
    public void getPutsPointcut() {
    }



    @Around("getPutsPointcut()")
    public Collection<Derivative> getPutsPointcutMethod(ProceedingJoinPoint jp) throws Throwable {

        Collection<Derivative> tmp = (Collection<Derivative>)jp.proceed();

        Collection<Derivative> result = new ArrayList<>();

        log.info(String.format("%s\nNumber of puts: %d",jp.toString(),tmp.size()));

        for (Derivative cb : tmp) {
            //CalculatedDerivativeBean cb = (CalculatedDerivativeBean)bean;

            if (isOk(cb) == false) continue;

            result.add(cb);
        }

        return result;
    }

    @Around("getCallsPointcut()")
    public Collection<Derivative> getCallsPointcutMethod(ProceedingJoinPoint jp) throws Throwable {

        Collection<Derivative> tmp = (Collection<Derivative>)jp.proceed();

        Collection<Derivative> result = new ArrayList<>();

        log.info(String.format("%s\nNumber of calls: %d",jp.toString(),tmp.size()));

        for (Derivative cb : tmp) {

            if (isOk(cb) == false) continue;

            result.add(cb);
        }

        return result;
    }

    private boolean isOk(Derivative cb) {
        String ticker = cb.getTicker();

        if (cb.getParent() == null) {
            log.warn(String.format("%s: parent is null",ticker));
            return false;
        }

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


    /*
    @Pointcut("execution(* oahu.financial.Etrade.getSpot(String))")
    public void getSpotPointcut() {
    }
    */
}
