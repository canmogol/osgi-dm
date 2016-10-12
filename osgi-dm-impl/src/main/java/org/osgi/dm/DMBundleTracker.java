package org.osgi.dm;


import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class DMBundleTracker extends BundleTracker<Bundle> {

    private final BundleTrackerListener listener;
    private Set<String> bundleNames;

    public DMBundleTracker(BundleTrackerListener listener, BundleContext context, int stateMask, BundleTrackerCustomizer<Bundle> customizer) {
        super(context, stateMask, customizer);
        this.bundleNames = Arrays.asList(context.getBundles()).stream().map(Bundle::getSymbolicName)
                .filter(name -> !(name == null || name.startsWith("org.eclipse") || name.startsWith("org.osgi.dm-")))
                .collect(Collectors.toSet());
        this.listener = listener;
    }

    @Override
    public synchronized Bundle addingBundle(Bundle bundle, BundleEvent event) {
        bundleNames.add(bundle.getSymbolicName());
        return bundle;
    }

    @Override
    public synchronized void removedBundle(Bundle bundle, BundleEvent event, Bundle object) {
        if (bundle.getState() == Bundle.ACTIVE || event.getType() == BundleEvent.STARTED) {
            bundleNames.remove(bundle.getSymbolicName());
            if (bundleNames.size() == 0) {
                listener.allBundlesStarted();
            }
        }
    }

    public Set<String> getBundleNames() {
        return Collections.unmodifiableSet(bundleNames);
    }

}