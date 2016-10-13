# osgi-dm
Dependency management bundle for OSGI

---

"osgi-dm-impl" project contains the implementation of the dependency injection, 
"osgi-dm-interface" contains the annotation and interface classes that will be imported by client bundles.

Group and User bundle projects each has interfaces and they have cyclic dependencies to each other that will be injected,
"example-group-bundle"
"example-group-bundle-interface"
"example-user-bundle"
"example-user-bundle-interface

The runner project is just used to copile other projects and run the project with ```start.sh``` script,
"example-runner"

All projects are maven projects, to compile all projects just run ```mvn clean install``` inside the "example-runner" folder,

---

check ```GroupActivator``` and ```UserActivator``` classes for reallife application,


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

