package netfondsjanitor.aspects;

import oahu.annotations.Cache;
import java.util.UUID;

import netfondsjanitor.App;
import oahu.aspects.cache.Cacheable;
import netfondsrepos.repos.DefaultEtradeRepository;

public privileged aspect DemoAspect implements  Cacheable {

    public UUID getUUID() {
        return null;
    }

    int counter = 0;

    pointcut traced() : within(App);

    before() : traced() {
        System.out.println("Before " + thisJoinPoint + ", " + counter++);
    }

    /*
    pointcut htmlParse() : execution(* DefaultEtradeRepository.parseHtmlFor(..));

    around() : htmlParse() {
        System.out.println("PARSING HTML...");
        return proceed();
    }
    */
}

