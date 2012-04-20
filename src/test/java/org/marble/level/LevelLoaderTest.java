package org.marble.level;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import com.jme3.math.Vector3f;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;

import org.junit.Before;
import org.junit.Test;

import org.marble.Game;
import org.marble.entity.AbstractEntity;
import org.marble.entity.Entity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;
import org.marble.level.LevelStatement.Alias;
import org.marble.level.LevelStatement.Connection;
import org.marble.level.LevelStatement.Declaration;
import org.marble.level.LevelStatement.Position;
import org.marble.util.Connectors;

public class LevelLoaderTest {

    private LevelLoader loader;

    @Test
    public void createEntityClass() throws LevelLoadException {
        final Entity entity =
                loader.createEntity(MockEntity.class, ImmutableList.of(),
                        "MockEntity", 0);
        assertTrue(entity instanceof MockEntity);
    }

    @Test(expected = LevelLoadException.class)
    public void createEntityFailure1() throws LevelLoadException {
        loader.createEntity(MockEntity.class,
                ImmutableList.of((Object) "foo", "bar"), "MockEntity", 0);
    }

    @Test(expected = LevelLoadException.class)
    public void createEntityFailure2() throws LevelLoadException {
        loader.createEntity(MockEntity.class,
                ImmutableList.of((Object) "foo", "bar", "baz", "diz"),
                "MockEntity", 0);
    }

    @Test
    public void createEntityInit() throws LevelLoadException {
        MockEntity entity =
                (MockEntity) loader.createEntity(MockEntity.class,
                        ImmutableList.of(), "MockEntity", 0);
        assertEquals(0, entity.initializerIndex);
        entity =
                (MockEntity) loader.createEntity(MockEntity.class,
                        ImmutableList.of((Object) 0.0f, "bar", new Vector3f()),
                        "MockEntity", 0);
        assertEquals(1, entity.initializerIndex);
    }

    @Test
    public void loadEntityClass() throws LevelLoadException {
        final Class<?> c =
                loader.loadEntityClass(MockEntity.class.getCanonicalName(), 0);
        assertEquals(MockEntity.class, c);
    }

    @Test(expected = LevelLoadException.class)
    public void loadEntityClassFailure() throws LevelLoadException {
        loader.loadEntityClass("java.lang.String", 0);
    }

    @Test
    public void runStatements() throws LevelLoadException {
        final String mockEntityClass = MockEntity.class.getCanonicalName();

        final ImmutableSet<Entity> entities1 =
                loader.runStatements(ImmutableList.of(new Alias(0, "Entity",
                        mockEntityClass), new Declaration(0, "e1", "Entity",
                        ImmutableList.of())));
        assertEquals(1, entities1.size());

        final ImmutableSet<Entity> entities2 =
                loader.runStatements(ImmutableList
                        .of((LevelStatement) new Declaration(0, "e1",
                                mockEntityClass, ImmutableList.of())));
        assertEquals(1, entities2.size());
    }

    @Test
    public void runStatementsConnectivity() throws LevelLoadException {
        final String mockEntityClass = MockEntity.class.getCanonicalName();
        final Optional<String> absent = Optional.absent();

        final ImmutableSet<Entity> entities2 =
                loader.runStatements(ImmutableList.of(
                        new Declaration(0, "e1", mockEntityClass, ImmutableList
                                .of()),
                        new Declaration(0, "e2", mockEntityClass, ImmutableList
                                .of()),
                        new Declaration(0, "e3", mockEntityClass, ImmutableList
                                .of()), new Position(0, "e1", new Vector3f(0,
                                1, 2), absent), new Connection(0, "e2", "c1",
                                "e1", "c2"), new Connection(0, "e3", "c1",
                                "e1", "c3")));
        assertEquals(3, entities2.size());
        final UnmodifiableIterator<Entity> iter = entities2.iterator();
        final Vector3f pos = new Vector3f();
        // Yes, ImmutableSet is guaranteed to iterate entries in insertion order
        iter.next().getTransform().getTranslation(pos);
        roundVectorToInts(pos);
        assertEquals(new Vector3f(0, 1, 2), pos);
        iter.next().getTransform().getTranslation(pos);
        roundVectorToInts(pos);
        assertEquals(new Vector3f(2, 1, 2), pos);
        iter.next().getTransform().getTranslation(pos);
        roundVectorToInts(pos);
        assertEquals(new Vector3f(3, 1, 3), pos);
    }

    private void roundVectorToInts(final Vector3f vector) {
        vector.x = Math.round(vector.x);
        vector.y = Math.round(vector.y);
        vector.z = Math.round(vector.z);
    }

    @Test
    public void runStatementsPositioning() throws LevelLoadException {
        final String mockEntityClass = MockEntity.class.getCanonicalName();
        final Optional<String> absent = Optional.absent();

        final ImmutableSet<Entity> entities1 =
                loader.runStatements(ImmutableList.of(new Declaration(0, "e1",
                        mockEntityClass, ImmutableList.of()), new Position(0,
                        "e1", new Vector3f(1, 2, 3), absent)));
        assertEquals(1, entities1.size());
        final Vector3f pos = new Vector3f();
        entities1.iterator().next().getTransform().getTranslation(pos);
        assertEquals(new Vector3f(1, 2, 3), pos);

        final ImmutableSet<Entity> entities2 =
                loader.runStatements(ImmutableList.of(
                        new Declaration(0, "e1", mockEntityClass, ImmutableList
                                .of()),
                        new Declaration(0, "e2", mockEntityClass, ImmutableList
                                .of()),
                        new Position(0, "e1", new Vector3f(0, 1, 2), absent),
                        new Position(0, "e2", new Vector3f(3, 4, 5), Optional
                                .of("e1"))));
        assertEquals(2, entities2.size());
        final UnmodifiableIterator<Entity> iter = entities2.iterator();
        // Yes, ImmutableSet is guaranteed to iterate entries in insertion order
        iter.next().getTransform().getTranslation(pos);
        assertEquals(new Vector3f(0, 1, 2), pos);
        iter.next().getTransform().getTranslation(pos);
        assertEquals(new Vector3f(3, 5, 7), pos);
    }

    @Before
    public void setUp() {
        loader = new LevelLoader();
    }
}

class MockEntity extends AbstractEntity implements Connected {
    int initializerIndex = -1;

    public MockEntity() {
        initializerIndex = 0;
    }

    public MockEntity(final Float foo, final String bar, final Vector3f baz) {
        initializerIndex = 1;
    }

    public MockEntity(final Object foo, final Object bar, final Object baz) {
        initializerIndex = 2;
    }

    public MockEntity(final String a, final String b, final String c,
            final String d) {
        initializerIndex = 4;
        throw new IllegalArgumentException();
    }

    @Override
    public Map<String, Connector> getConnectors() {
        return ImmutableMap.of("c1", Connectors.offsetBy(-1, 0, 0, 0, 0, 0),
                "c2", Connectors.offsetBy(1, 0, 0, 0, 0, 0), "c3",
                Connectors.offsetBy(3, 0, 0, (float) Math.PI, 0, 0));
    }

    @Override
    public void initialize(final Game game) {
    }

}
