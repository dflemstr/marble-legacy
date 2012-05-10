package org.marble.entity;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import org.marble.Game;
import org.marble.engine.Engine;

public class EntityManager {
    private final Game game;

    // Currently loaded entities
    private ImmutableSet<Entity> entities = ImmutableSet.of();

    public EntityManager(final Game game) {
        this.game = game;
    }

    /**
     * Starts managing a set of entities.
     * 
     * @param entities
     *            The set of entities to manage.
     */
    public void addEntities(final Set<Entity> entities) {
        try {
            for (final Entity entity : entities) {
                entity.initialize(game);
                for (final Engine<?> engine : game.getEngines()) {
                    if (engine.shouldHandle(entity)) {
                        engine.addEntity(entity);
                    }
                }
            }
        } catch (final Exception e) {
            game.handleError(e);
        }

        this.entities =
                ImmutableSet.<Entity> builder().addAll(this.entities)
                        .addAll(entities).build();
    }

    /**
     * Stops managing an entity.
     * 
     * @param entity
     *            The entity to stop managing.
     */
    public void removeEntities(final Set<Entity> entities) {
        for (final Entity entity : entities) {
            for (final Engine<?> engine : game.getEngines()) {
                if (engine.shouldHandle(entity)) {
                    engine.removeEntity(entity);
                }
            }
            try {
                entity.destroy();
            } catch (final Exception e) {
                game.handleError(e);
            }
        }

        this.entities =
                ImmutableSet.copyOf(Sets.difference(this.entities, entities));
    }

    public void removeEntity(final Entity entity) {
        removeEntities(ImmutableSet.of(entity));
    }

    public void addEntity(final Entity entity) {
        addEntities(ImmutableSet.of(entity));
    }

    /**
     * Removes all entities safely.
     */
    public void removeAllEntities() {
        removeEntities(entities);
    }

    public void update(final float timePerFrame) {
        for (final Entity entity : entities) {
            try {
                entity.update(timePerFrame);
            } catch (final Exception e) {
                game.handleError(e);
            }
        }
    }
}
