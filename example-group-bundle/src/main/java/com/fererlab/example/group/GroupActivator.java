package com.fererlab.example.group;

import com.fererlab.example.group.api.GroupService;
import com.fererlab.example.user.api.UserService;
import org.apache.commons.lang.ObjectUtils;
import org.osgi.dm.api.Inject;
import org.osgi.dm.api.InjectionAware;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class GroupActivator implements BundleActivator, InjectionAware {

    private Logger log = Logger.getLogger(getClass().getName());

    // This UserService instance will be injected
    private UserService userService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        log.info("start method called");
        // add this activator as bundle activator
        bundleContext.registerService(BundleActivator.class, this, new Hashtable<>());
        // create an instance of GroupService and register this instance
        GroupService groupService = new GroupServiceImpl();
        bundleContext.registerService(GroupService.class, groupService, new Hashtable<>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        log.info("stop method called");
    }

    @Inject
    public void setUserService(UserService userService) {
        log.info("UserService injected: " + userService);
        this.userService = userService;
    }

    @Override
    public void complete() {
        log.info("injection complete, all dependencies injected");
        // all dependencies completed which means UserService instance is injected via setUserService method
        // so it should be available here and we can call it here
        if (userService != null) {
            log.info("ALL USERS: " + userService.getAllUsers());
            log.info("ADMIN USERS: " + userService.getAllUsersOfGroup("Admin"));
        } else {
            log.severe("UserService did not injected or a null is injected instead");
        }
        final Object clone = ObjectUtils.clone(new Vector<>());
        log.info("*** COMMONS-LANG 2.6 -> ObjectUtils - clone: " + clone);
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
