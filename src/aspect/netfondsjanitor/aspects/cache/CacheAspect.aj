package netfondsjanitor.aspects.cache;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;

import oahu.annotations.Cache;
import java.util.function.Function;

public privileged aspect CacheAspect  {
    Map<String,Object> cached = new HashMap<>();

    pointcut cached() : execution(@Cache * *(..));


    Object around() : cached() {
        /*
        JoinPoint jp = thisJoinPoint;
        Object[] args = jp.getArgs();
        Cache annot = getAnnotation(jp);

        System.out.println("CACHED " + jp.getThis() + ", args: " + args + ", annot: " + annot.id());

        System.out.println(cacheKeyFactory.apply(jp));

        return proceed();
        */

        String key = cacheKeyFactory.apply(thisJoinPoint);
        if (cached.containsKey(key)){
            System.out.println("Return cached result");
            return cached.get(key);
        }
        else {
            System.out.println("Caching result");
            Object result = proceed();

            cached.put(key,result);

            return result;
        }
    }

    /*
    Cache getAnnotation(JoinPoint jp) {
        MethodSignature signature = (MethodSignature) jp.getSignature();
        Method method = signature.getMethod();
        return method.getAnnotation(Cache.class);
    }
    //*/


    Function<JoinPoint,String> cacheKeyFactory;

    public void setCacheKeyFactory(Function<JoinPoint,String> cacheKeyFactory) {
        this.cacheKeyFactory  = cacheKeyFactory;
    }
}

