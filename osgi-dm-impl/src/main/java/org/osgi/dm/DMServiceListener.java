package org.osgi.dm;


import org.osgi.dm.api.Inject;
import org.osgi.dm.api.InjectionAware;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DMServiceListener implements ServiceListener {

    private Logger log = Logger.getLogger(getClass().getName());

    private final Map<Method, List<Object>> invokedMethodObjects = new HashMap<>();
    private final Map<Class<?>, List<Object>> services = new HashMap<>();
    private final BundleContext bundleContext;
    private final Map<Class<?>, List<Map<Method, Object>>> methods;
    private final Map<Object, List<Method>> injectServiceMethods;

    public DMServiceListener(BundleContext bundleContext, Map<Class<?>, List<Map<Method, Object>>> methods, Map<Object, List<Method>> injectServiceMethods) {
        this.bundleContext = bundleContext;
        this.methods = methods;
        this.injectServiceMethods = injectServiceMethods;
    }

    @Override
    public void serviceChanged(ServiceEvent event) {
        ServiceReference serviceReference = event.getServiceReference();
        Object service = bundleContext.getService(serviceReference);
        Object objectClassProperty = serviceReference.getProperty("objectClass");
        List<Class> typeList = new ArrayList<>();
        if (objectClassProperty != null && objectClassProperty instanceof String[]) {
            typeList = Arrays.asList((String[]) objectClassProperty).stream().map(className -> {
                Class type = null;
                try {
                    type = Class.forName(className);
                } catch (ClassNotFoundException exception) {
                    // we can safely ignore this exception, since this bundle may not know/get a handle to this "Class"
                    String error = "could not created class for this type, type: " + className + " error: " + exception.getMessage();
                    log.log(Level.FINEST, error, exception);
                }
                return type;
            }).filter(type -> {
                if (type != null) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());
        } else {
            log.info("COULD NOT FOUND 'objectClass' PROPERTY, dependencies for this service will not be injected, service: " + service.getClass());
        }
        switch (event.getType()) {
            case ServiceEvent.UNREGISTERING:

                // remove this service from type list
                typeList.stream().forEach(type -> {
                    if (services.containsKey(type) && services.get(type).contains(service)) {
                        services.get(type).remove(service);
                    }
                });

                typeList.stream().filter(services::containsKey).forEach(type -> {
                    methods.entrySet().stream().filter(entry -> entry.getKey().equals(type))
                            .map(Map.Entry::getValue).forEach(list -> {
                        list.forEach(map -> {
                            // the object is the service that has the inject annotated method
                            map.values().stream().filter(object -> object instanceof InjectionAware)
                                    .forEach(object -> {
                                        // notify others that this service is lost
                                        InjectionAware injectionAware = (InjectionAware) object;
                                        injectionAware.lost(type);
                                    });
                        });
                    });
                });

                // Inject annotasyonlu metotlari bul
                // found methods that are annotated with Inject annotation
                List<Method> injectAnnotatedMethods = getInjectAnnotatedMethods(service);

                // remove this service from invoked methods
                invokedMethodObjects.forEach((method, objects) -> {
                    Iterator<Object> it = objects.iterator();
                    while (it.hasNext()) {
                        Object methodObject = it.next();
                        if (service.equals(methodObject)) {
                            it.remove();
                        } else {
                            for (Method injectAnnotatedMethod : injectAnnotatedMethods) {
                                if (method.equals(injectAnnotatedMethod)) {
                                    it.remove();
                                }
                            }
                        }
                    }
                });

                // remove this service from registered methods
                methods.forEach((methodParameterType, methodServiceList) -> {
                    Iterator<Map<Method, Object>> iterator = methodServiceList.iterator();
                    while (iterator.hasNext()) {
                        Map<Method, Object> methodService = iterator.next();
                        if (methodService.values().contains(service)) {
                            iterator.remove();
                        }
                    }
                });

                // remove this service from inject service methods
                Iterator<Map.Entry<Object, List<Method>>> iterator = injectServiceMethods.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Object, List<Method>> entry = iterator.next();
                    if (entry.getKey().equals(service)) {
                        iterator.remove();
                    }
                }

                // end of UNREGISTERING case
                break;
            case ServiceEvent.REGISTERED:
                typeList.stream().forEach(type -> {
                    // add service to registered services
                    if (!services.containsKey(type)) {
                        services.put(type, new ArrayList<>());
                    }
                    // add the service for this type
                    services.get(type).add(service);
                });

                // handle injection
                handleInjection(service);

                // end of REGISTERED case
                break;
            default:
                break;
        }
    }

    private void handleInjection(Object injectService) {
        // find Inject annotated methods
        List<Method> injectAnnotatedMethods = this.getInjectAnnotatedMethods(injectService);

        // get all Inject annotated methods of inject service
        if (!injectServiceMethods.containsKey(injectService)) {
            injectServiceMethods.put(injectService, new ArrayList<>());
        }
        injectServiceMethods.get(injectService).addAll(injectAnnotatedMethods);

        // add method to inject annotated methods
        injectAnnotatedMethods.stream().forEach(method -> {
            if (method.getParameterTypes().length == 1) {
                Class<?> type = method.getParameterTypes()[0];
                if (!methods.containsKey(type)) {
                    methods.put(type, new ArrayList<>());
                }
                methods.get(type).add(new HashMap<Method, Object>() {
                    {
                        put(method, injectService);
                    }
                });
            }
        });

        // inject services
        services.forEach((type, registeredServices) -> {
            methods.forEach((methodType, methodList) -> {
                if (type.isAssignableFrom(methodType)) {
                    methodList.forEach((methodObject) -> {
                        methodObject.forEach((method, object) -> {
                            registeredServices.stream().forEach(service -> {
                                try {
                                    if (!invokedMethodObjects.containsKey(method)) {
                                        invokedMethodObjects.put(method, new ArrayList<>());
                                    }
                                    // prevent invoking of a method with the same object twice
                                    if (!invokedMethodObjects.get(method).contains(service)) {
                                        // send the service to method (a setter method probably)
                                        method.invoke(object, service);
                                        // add this service to invoked method's list
                                        invokedMethodObjects.get(method).add(service);
                                        // remove this method from this service's inject methods,
                                        // since we have (set) injected an object to this method
                                        injectServiceMethods.get(object).remove(method);
                                    }
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    String error = "could not invoke method: " + method.getName() + " on object: " + object + " for parameter: " + service;
                                    log.log(Level.SEVERE, error, e);
                                }
                            });
                        });
                    });
                }
            });
        });
    }

    private List<Method> getInjectAnnotatedMethods(Object object) {
        return Arrays.asList(object.getClass().getDeclaredMethods()).stream().filter(m -> {
            List<Annotation> annotations = Arrays.asList(m.getDeclaredAnnotations()).stream()
                    .filter(annotation -> annotation.annotationType().equals(Inject.class))
                    .collect(Collectors.toList());
            if (!annotations.isEmpty()) {
                // this method is annotated with Inject annotation
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

}