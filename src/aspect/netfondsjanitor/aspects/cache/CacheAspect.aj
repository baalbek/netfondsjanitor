package netfondsjanitor.aspects.cache;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.Method;

import oahu.annotations.Cache;
import java.util.function.Function;

public privileged aspect CacheAspect  {
    Map<Integer,Map<String,Object>> cachedThisObj = new HashMap<>();

    pointcut cached() : execution(@Cache * *(..));


    Object around() : cached() {
        if (cacheKeyFactory == null) {
            return proceed();
        }

        Map<String,Object> cachedMethods = null;

        int hc =  thisJoinPoint.getThis().hashCode();
        if (cachedThisObj.containsKey(hc)) {
            cachedMethods = cachedThisObj.get(hc);
        }
        else {
            cachedMethods = new HashMap<>();
            cachedThisObj.put(hc,cachedMethods);
        }

        String key = cacheKeyFactory.apply(thisJoinPoint);
        if (cachedMethods.containsKey(key)){
            System.out.println("Return cached result");
            return cachedMethods.get(key);
        }
        else {
            System.out.println("Caching result");
            Object result = proceed();

            cachedMethods.put(key,result);

            return result;
        }
    }

    public void invalidate(Object thisObj) {
        cachedThisObj.remove(thisObj);
    }

    Function<JoinPoint,String> cacheKeyFactory;

    public void setCacheKeyFactory(Function<JoinPoint,String> cacheKeyFactory) {
        this.cacheKeyFactory  = cacheKeyFactory;
    }
}

