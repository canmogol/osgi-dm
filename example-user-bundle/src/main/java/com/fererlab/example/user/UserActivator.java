package com.fererlab.example.user;

import com.fererlab.example.group.api.GroupService;
import com.fererlab.example.user.api.UserService;
import org.apache.commons.lang.ObjectUtils;
import org.osgi.dm.api.Inject;
import org.osgi.dm.api.InjectionAware;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

public class UserActivator implements BundleActivator, InjectionAware {

    private Logger log = Logger.getLogger(getClass().getName());

    // This GroupService instance will be injected
    private GroupService groupService;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        log.info("start method called");
        // add this activator as bundle activator
        bundleContext.registerService(BundleActivator.class, this, new Hashtable<>());
        // create an instance of UserService and register this instance
        UserService userService = new UserServiceImpl();
        bundleContext.registerService(UserService.class, userService, new Hashtable<>());
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        log.info("stop method called");
    }

    @Inject
    public void setGroupService(GroupService groupService) {
        log.info("GroupService injected: " + groupService);
        this.groupService = groupService;
    }

    @Override
    public void complete() {
        log.info("injection complete, all dependencies injected");
        // all dependencies completed which means GroupService instance is injected via setGroupService method
        // so it should be available here and we can call it here
        if (groupService != null) {
            log.info("ALL GROUPS: " + groupService.getAllGroups());
            log.info("JOHN'S GROUPS: " + groupService.getGroupsOfUser("john"));
        } else {
            log.severe("GroupService did not injected or a null is injected instead");
        }
        final boolean equals = ObjectUtils.equals("", "");
        log.info("*** COMMONS-LANG 2.5 -> ObjectUtils - equals: " + equals);
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
