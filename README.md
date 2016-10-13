# osgi-dm
dependency management bundle for OSGI

osgi-dm-interface has an annotation for inject methods and a listener interface to get notified about injections.

Below code shows a sample of ```@Inject``` annotation usage, it injects the ```UserService``` instance,
```
    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
```

Below code shows a sample of ```InjectionAware``` interface usage, ```public void complete()``` method is the start point,
```
public class AnyPojo implements InjectionAware {

    @Override
    public void complete() {
       log.info("injection complete, all dependencies injected");
       // here the business logic code
    }

    @Override
    public void incomplete(List<Class> dependencies) {
        log.info("injection INCOMPLETE, NOT all dependencies injected!");
    }

    @Override
    public void lost(Class dependency) {
        log.info("this dependency lost: " + dependency);
    }

}

```

