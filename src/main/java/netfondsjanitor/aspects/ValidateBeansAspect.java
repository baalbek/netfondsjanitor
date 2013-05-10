package netfondsjanitor.aspects;

import maunakea.financial.beans.CalculatedDerivativeBean;
import oahu.financial.beans.DerivativeBean;
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

    private static String NL = "\t\n";

    @Pointcut("execution(* oahu.financial.Etrade.getCalls(String))")
    public void getCallsPointcut() {
    }


    @Around("getCallsPointcut()")
    public Collection<DerivativeBean> getCallsPointcutMethod(ProceedingJoinPoint jp) throws Throwable {

        StringBuilder sb = new StringBuilder(jp.toString());

        Collection<DerivativeBean> tmp = (Collection<DerivativeBean>)jp.proceed();

        Collection<DerivativeBean> result = new ArrayList<>();

        sb.append("\n\tNumber of options: ").append(tmp.size());

        for (DerivativeBean bean : tmp) {
            CalculatedDerivativeBean cb = (CalculatedDerivativeBean)bean;

            String ticker = cb.getTicker();

            if (cb.getParent() == null) {
                sb.append(NL).append(ticker).append(": parent is null!");
                continue;
            }


            if (cb.daysProperty().get() < 0) {
                sb.append(NL).append(ticker).append(" has expired!");
                continue;
            }


            if (cb.getIvSell() < 0 || cb.getIvBuy() < 0) {
                sb.append(NL).append(ticker).append(": iv not valid!");
                continue;
            }

            result.add(cb);
        }

        log.debug(sb.toString());

        System.out.println(sb.toString());
        return result;
    }


    /*
    @Pointcut("execution(* oahu.financial.Etrade.getSpot(String))")
    public void getSpotPointcut() {
    }
    */
}
