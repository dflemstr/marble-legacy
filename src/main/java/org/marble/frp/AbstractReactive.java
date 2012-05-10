package org.marble.frp;

import java.util.Set;

import com.google.common.collect.Sets;

public abstract class AbstractReactive<A> implements Reactive<A> {

    protected final Set<ReactiveListener<A>> listeners = Sets
            .newIdentityHashSet();

    @Override
    public void addReactiveListener(final ReactiveListener<A> listener) {
        listeners.add(listener);
    }

    @Override
    public abstract A getValue();

    @Override
    public void removeReactiveListener(final ReactiveListener<A> listener) {
        listeners.remove(listener);
    }
}
