package org.osgi.dm.api;

import java.util.List;

public interface InjectionAware {

    void complete();

    void incomplete(List<Class> dependencies);

    void lost(Class dependency);

}
