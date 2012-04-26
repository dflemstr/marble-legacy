package org.marble.frp.mutable;

import com.google.common.base.Objects;

import org.marble.frp.AbstractReactive;
import org.marble.frp.ReactiveListener;

public abstract class AbstractMutableReactive<A> extends AbstractReactive<A>
        implements MutableReactive<A> {

    @Override
    public abstract A getValue();

    protected abstract void putValue(final A value);

    @Override
    public void setValue(final A value) {
        final A oldValue = getValue();
        if (!Objects.equal(oldValue, value)) {
            for (final ReactiveListener<A> listener : listeners) {
                listener.valueChanged(value);
            }
        }
        putValue(value);
    }
}
