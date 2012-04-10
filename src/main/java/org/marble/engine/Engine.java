package org.marble.engine;

import com.ardor3d.util.ReadOnlyTimer;

import org.marble.entity.Entity;

/**
 * An engine that handles a particular trait of entities.
 * 
 * @param <E>
 *            The entity trait to handle.
 */
public abstract class Engine<E extends Entity> {
    private final Class<E> entityType;

    protected Engine(final Class<E> entityType) {
        this.entityType = entityType;
    }

    /**
     * Lets this engine handle a specified entity. The entity must be accepted
     * by the engine via {@link #shouldHandle(Entity)}.
     * 
     * @param entity
     *            The entity to start handling.
     */
    public final void addEntity(final Entity entity) {
        if (shouldHandle(entity)) {
            entityAdded(this.entityType.cast(entity));
        } else
            throw new IllegalArgumentException(
                    "Cannot handle this type of entity");
    }

    /**
     * Destruction routine called at the end of the simulation.
     */
    public abstract void destroy();

    /**
     * Notifies the core of the engine that an entity has been added.
     * 
     * @param entity
     *            The entity that was added.
     */
    protected abstract void entityAdded(E entity);

    /**
     * Notifies the core of the engine that an entity has been removed.
     * 
     * @param entity
     *            The entity that has been removed.
     */
    protected abstract void entityRemoved(E entity);

    /**
     * Initialization routine called before any entities are added.
     */
    public abstract void initialize();

    /**
     * Makes this engine stop handling a specified entity. The entity must be
     * accepted by the engine via {@link #shouldHandle(Entity)}, but doesn't
     * neccessarily have to have been added to the engine before.
     * 
     * @param entity
     *            The entity to stop handling.
     */
    public final void removeEntity(final Entity entity) {
        if (shouldHandle(entity)) {
            entityRemoved(this.entityType.cast(entity));
        } else
            throw new IllegalArgumentException(
                    "Cannot handle this type of entity");
    }

    /**
     * Specifies whether this engine should handle the specified entity.
     * 
     * @param entity
     *            The entity to test for inclusion.
     * @return Whether the entity should be handled by this engine.
     */
    public boolean shouldHandle(final Entity entity) {
        return this.entityType.isInstance(entity);
    }

    /**
     * Advances the state of the engine by one step.
     * 
     * @param timer
     *            The timer specifying how much time that has elapsed.
     * @return Whether the engine is still usable.
     */
    public abstract boolean update(ReadOnlyTimer timer);
}
