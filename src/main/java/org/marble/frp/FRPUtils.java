package org.marble.frp;

import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

import org.marble.frp.mutable.MutableReactive;

public final class FRPUtils {
    private FRPUtils() {
    }

    public static <A> void addAndCallReactiveListener(final Reactive<A> entry,
            final ReactiveListener<A> listener) {
        entry.addReactiveListener(listener);
        listener.valueChanged(entry.getValue());
    }

    public static <A, B> MutableReactive<B> bimap(
            final MutableReactive<A> reactiveA, final Function<A, B> aToB,
            final Function<B, A> bToA) {
        return new MappedMutableReactive<A, B>(reactiveA, aToB, bToA);
    }

    public static <A, B> Reactive<B> map(final Reactive<A> reactiveA,
            final Function<A, B> aToB) {
        return new MappedReactive<A, B>(reactiveA, aToB);
    }

    private static class MappedMutableReactive<A, B> extends
            MappedReactive<A, B> implements MutableReactive<B> {
        private final Function<B, A> bToA;
        private final MutableReactive<A> reactiveA;

        MappedMutableReactive(final MutableReactive<A> reactiveA,
                final Function<A, B> aToB, final Function<B, A> bToA) {
            super(reactiveA, aToB);
            this.reactiveA = reactiveA;
            this.bToA = bToA;
        }

        @Override
        public void setValue(final B value) {
            reactiveA.setValue(bToA.apply(value));
        }

    }

    private static class MappedReactive<A, B> implements Reactive<B> {
        private final Function<A, B> aToB;
        private final Map<ReactiveListener<B>, ReactiveListener<A>> listeners =
                Maps.newIdentityHashMap();
        private final Reactive<A> reactiveA;

        MappedReactive(final Reactive<A> reactiveA, final Function<A, B> aToB) {
            this.reactiveA = reactiveA;
            this.aToB = aToB;
        }

        @Override
        public void addReactiveListener(final ReactiveListener<B> listener) {
            final ReactiveListener<A> wrappingListener =
                    new ReactiveListener<A>() {
                        @Override
                        public void valueChanged(final A value) {
                            listener.valueChanged(aToB.apply(value));
                        }
                    };
            listeners.put(listener, wrappingListener);
            reactiveA.addReactiveListener(wrappingListener);
        }

        @Override
        public B getValue() {
            return aToB.apply(reactiveA.getValue());
        }

        @Override
        public void removeReactiveListener(final ReactiveListener<B> listener) {
            reactiveA.removeReactiveListener(listeners.get(listener));
        }
    }
}
