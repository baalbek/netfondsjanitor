package netfondsjanitor.aspects;

public aspect DemoAspect  {

    int counter = 0;

    pointcut traced() : !within(DemoAspect);

    before() : traced() {
        System.out.println("Before " + thisJoinPoint + ", " + counter++);
    }
}

