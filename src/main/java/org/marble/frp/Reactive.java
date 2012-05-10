package org.marble.frp;

/**
 * A reactive value represents a discrete Signal from the theory of functional
 * reactive programming. It is a reference to a value that varies over time.
 * When the value is updated, active reactive listeners are notified.
 * 
 * @param <A>
 *            The type of value to make this reactive reference point to.
 */
public interface Reactive<A> {
    /**
     * Add a listener to this reactive signal. When the value of this signal is
     * updated, the listener will be notified.
     */
    public void addReactiveListener(ReactiveListener<A> listener);

    /**
     * Sample the current value of the reactive signal.
     */
    public A getValue();

    /**
     * Remove a listener from this signal. The listener will no longer be
     * notified of updates.
     * 
     * @param listener
     *            The listener to remove.
     */
    public void removeReactiveListener(ReactiveListener<A> listener);
}
