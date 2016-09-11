package netfondsjanitor.cache;

import oahu.annotations.Cache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Created by rcs on 10.09.16.
 *
 */
public class CacheIdKeyFactory implements Function<JoinPoint,String> {

    @Override
    public String apply(JoinPoint joinPoint) {
        int hc = joinPoint.getThis().hashCode();
        Cache cache = getAnnotation(joinPoint);

        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            return String.format("%d-%d-%s", hc, cache.id(), args[0].toString());
        }
        else {
            return String.format("%d-%d", hc, cache.id());
        }
    }

    private Cache getAnnotation(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(Cache.class);
    }
}
