package org.osgi.dm;

import org.osgi.dm.api.InjectionAware;
import org.osgi.framework.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DMActivator implements BundleActivator, BundleTrackerListener {

    private final Map<Class<?>, List<Map<Method, Object>>> methods = new HashMap<>();
    private Logger log = Logger.getLogger(getClass().getName());
    private ExecutorService pool = Executors.newCachedThreadPool();
    private Map<Object, List<Method>> injectedServiceMethods = new HashMap<>();

    public void start(BundleContext bundleContext) throws Exception {
        log.info("dependency manager activator started");

        // bundle tracker will track for the starting bundles
        DMBundleTracker bundleTracker = new DMBundleTracker(this, bundleContext, Bundle.STARTING, null);
        bundleTracker.open();

        // service listener to get the services to inject
        ServiceListener serviceListener = new DMServiceListener(bundleContext, methods, injectedServiceMethods);
        try {
            // "object class" filter used to filter all the services
            String filter = "(objectclass=*)";
            bundleContext.addServiceListener(serviceListener, filter);
        } catch (InvalidSyntaxException e) {
            String error = "could not add the service listener to do the injection, filter: " + e.getFilter() + " error: " + e.getMessage();
            log.log(Level.SEVERE, error, e);
        }

    }

    public void stop(BundleContext bundleContext) throws Exception {
        log.info("dependency manager activator stopped");
    }

    @Override
    public void allBundlesStarted() {
        injectedServiceMethods.forEach((service, methods) -> {
            if (service instanceof InjectionAware) {
                InjectionAware injectionAware = (InjectionAware) service;
                if (methods.size() > 0) {
                    List<Class> dependencies = methods.stream().map(metot -> {
                        Class type = null;
                        if (metot.getParameterTypes().length == 1) {
                            type = metot.getParameterTypes()[0];
                        }
                        return type;
                    }).filter(type -> {
                        if (type != null) {
                            return true;
                        } else {
                            return false;
                        }
                    }).collect(Collectors.toList());
                    pool.execute(() -> {
                        injectionAware.incomplete(dependencies);
                    });
                } else {
                    pool.execute(injectionAware::complete);
                }
            }
        });
    }

}
