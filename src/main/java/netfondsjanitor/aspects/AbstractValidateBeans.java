package netfondsjanitor.aspects;

import oahu.domain.Tuple3;
import oahu.financial.DerivativePrice;
import oahu.financial.StockPrice;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by rcs on 17.02.15.
 */
public abstract class AbstractValidateBeans {

    public Tuple3<StockPrice,Collection<DerivativePrice>,Collection<DerivativePrice>>
    exec(ProceedingJoinPoint jp) throws Throwable {

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


    protected abstract boolean isOk(DerivativePrice cb);

}
