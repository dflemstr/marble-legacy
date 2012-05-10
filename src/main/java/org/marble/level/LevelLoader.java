package org.marble.level;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Matrix4f;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.codehaus.jparsec.error.ParserException;

import org.marble.entity.Entity;
import org.marble.entity.connected.Connected;
import org.marble.entity.connected.Connector;

/**
 * A class for loading level data from level files.
 */
public final class LevelLoader {
    private final LevelParser parser = new LevelParser();

    /**
     * Loads a level from the specified URL.
     * 
     * @param url
     *            The URL to load the level from.
     * @return The entities that the loaded level consists of.
     * @throws ParserException
     *             if the level file contained syntax errors.
     * @throws LevelLoadException
     *             if the level could not be loaded for logical reasons.
     * @throws IOException
     *             if there was an error when fetching the level file from the
     *             URL.
     */
    public ImmutableSet<Entity> loadLevel(final URL url)
            throws ParserException, LevelLoadException, IOException {
        return runStatements(loadLevelStatements(url));
    }

    /**
     * Fetches a series of level statements from the specified URL.
     * 
     * @param url
     *            The URL to load level statements from.
     * @return The loaded series of level statements, describing how a level
     *         should be loaded.
     * @throws IOException
     *             if there was an error when fetching the level file from the
     *             URL.
     * @throws ParserException
     *             if the level file contained syntax errors.
     */
    public ImmutableList<LevelStatement> loadLevelStatements(final URL url)
            throws IOException, ParserException {

        InputStream input = null;
        ImmutableList<LevelStatement> statements;
        try {
            input = url.openStream();
            statements =
                    parser.getParser().parse(new InputStreamReader(input),
                            url.toExternalForm());
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return statements;
    }

    public MetaLevelPack loadMetaLevelPack(final URL url) throws IOException,
            JSONException, LevelLoadException {
        final String input = Resources.toString(url, Charsets.UTF_8);
        final JSONObject packObject = new JSONObject(input);
        return loadMetaLevelPack(packObject, url);
    }

    public StatisticalMetaLevel loadStatisticalMetaLevel(
            final JSONObject levelObject) throws JSONException {
        final ImmutableMap<String, Integer> highscores =
                loadHighscores(levelObject.getJSONObject("highscores"));
        return new StatisticalMetaLevel(highscores);
    }

    /**
     * Runs (executes) a series of level statements, producing a set of
     * initialized world entities.
     * 
     * @param statements
     *            The statements to execute.
     * @return The produced set of world entities.
     * @throws LevelLoadException
     *             If the series of statements contained a logical error.
     */
    public ImmutableSet<Entity> runStatements(
            final ImmutableList<LevelStatement> statements)
            throws LevelLoadException {
        final HashMap<String, Class<? extends Entity>> classAliases =
                new HashMap<String, Class<? extends Entity>>();
        final HashMap<String, Entity> entityNames =
                new HashMap<String, Entity>();
        final ImmutableSet.Builder<Entity> entityBuilder =
                ImmutableSet.builder();
        // Temporary variables:
        final Matrix4f baseMatrix = new Matrix4f();
        final Matrix4f transformMatrix = new Matrix4f();
        final Matrix3f rotationMatrix = new Matrix3f();
        final Quaternion rotationQuaternion = new Quaternion();
        final Vector3f translationVector = new Vector3f();
        final Transform movedTransform = new Transform();

        for (final LevelStatement statement : statements) {
            final int loc = statement.getLocation();
            if (statement instanceof LevelStatement.Alias) {
                final LevelStatement.Alias alias =
                        (LevelStatement.Alias) statement;
                classAliases.put(alias.getAlias(),
                        loadEntityClass(alias.getAliasedClass(), loc));
            } else if (statement instanceof LevelStatement.Connection) {
                final LevelStatement.Connection connection =
                        (LevelStatement.Connection) statement;
                final String movedEntityName = connection.getNameLeft();
                final String baseEntityName = connection.getNameRight();
                final String movedConnectorName = connection.getConnectorLeft();
                final String baseConnectorName = connection.getConnectorRight();

                // The entity that should be moved
                final Entity movedEntity = entityNames.get(movedEntityName);
                validateEntity(movedEntity, movedEntityName, loc);

                // The entity to base the transformation of the moved entity on
                final Entity baseEntity = entityNames.get(baseEntityName);
                validateEntity(baseEntity, baseEntityName, loc);

                // The connector relative to which we should align the other
                // connector
                final Connector baseConnector =
                        withConnectivity(baseEntity, baseEntityName, loc)
                                .getConnectors().get(baseConnectorName);
                validateConnector(baseConnector, baseConnectorName,
                        baseEntityName, loc);

                // The connector to align to the first connector
                final Connector movedConnector =
                        withConnectivity(movedEntity, movedEntityName, loc)
                                .getConnectors().get(movedConnectorName);
                validateConnector(movedConnector, movedConnectorName,
                        movedEntityName, loc);

                // Calculate the transformation that aligns the moved connector
                // to the base connector
                baseConnector.transformInto(movedConnector, transformMatrix);

                // Create the offset transform relative to the base entity
                final Transform baseTransform = baseEntity.getTransform();
                baseMatrix.loadIdentity();
                baseMatrix.scale(baseTransform.getScale());
                baseMatrix.multLocal(baseTransform.getRotation());
                baseMatrix.setTranslation(baseTransform.getTranslation());

                baseMatrix.multLocal(transformMatrix);

                final float scaleX =
                        FastMath.sqrt(FastMath.sqr(baseMatrix.get(0, 0))
                                + FastMath.sqr(baseMatrix.get(1, 0))
                                + FastMath.sqr(baseMatrix.get(2, 0)));
                final float scaleY =
                        FastMath.sqrt(FastMath.sqr(baseMatrix.get(0, 1))
                                + FastMath.sqr(baseMatrix.get(1, 1))
                                + FastMath.sqr(baseMatrix.get(2, 1)));
                final float scaleZ =
                        FastMath.sqrt(FastMath.sqr(baseMatrix.get(0, 2))
                                + FastMath.sqr(baseMatrix.get(1, 2))
                                + FastMath.sqr(baseMatrix.get(2, 2)));

                for (int x = 0; x < 3; x++) {
                    rotationMatrix.set(x, 0, baseMatrix.get(x, 0) / scaleX);
                    rotationMatrix.set(x, 1, baseMatrix.get(x, 1) / scaleY);
                    rotationMatrix.set(x, 2, baseMatrix.get(x, 2) / scaleZ);
                }

                movedTransform.loadIdentity();
                rotationQuaternion.fromRotationMatrix(rotationMatrix);
                movedTransform.setRotation(rotationQuaternion);
                movedTransform.setTranslation(baseMatrix.get(0, 3),
                        baseMatrix.get(1, 3), baseMatrix.get(2, 3));
                movedTransform.setScale(scaleX, scaleY, scaleZ);

                // Use the calculated transform for the moved entity
                movedEntity.setTransform(movedTransform);
            } else if (statement instanceof LevelStatement.Declaration) {
                final LevelStatement.Declaration declaration =
                        (LevelStatement.Declaration) statement;
                final String className = declaration.getClassName();
                Class<? extends Entity> entityClass =
                        classAliases.get(className);

                // We aren't using a class alias; load the class directly
                if (entityClass == null) {
                    entityClass = loadEntityClass(className, loc);
                }

                final Entity entity =
                        createEntity(entityClass, declaration.getInitArgs(),
                                className, loc);
                entity.setName(declaration.getName());
                entityNames.put(declaration.getName(), entity);
                entityBuilder.add(entity);
            } else if (statement instanceof LevelStatement.Position) {
                final LevelStatement.Position position =
                        (LevelStatement.Position) statement;
                final Entity movedEntity = entityNames.get(position.getName());
                validateEntity(movedEntity, position.getName(), loc);

                movedTransform.loadIdentity();
                // Should we position the entity relative to some other entity?
                if (position.getRelativeTo().isPresent()) {
                    final String relativeToName =
                            position.getRelativeTo().get();
                    final Entity baseEntity = entityNames.get(relativeToName);
                    validateEntity(baseEntity, relativeToName, loc);

                    // TODO instead of placing the entity at an offset from the
                    // other entity, one might want to make the offset relative
                    // (so that if the base entity is rotated, the moved
                    // entity's offset is relative to that rotation)
                    baseEntity.getTransform().getTranslation(translationVector);
                    translationVector.addLocal(position.getPosition());

                    if (position.getConnector().isPresent()) {
                        final Connected connected = (Connected) baseEntity;
                        final String relativeToConnector =
                                position.getConnector().get();
                        final Connector connector =
                                connected.getConnectors().get(
                                        relativeToConnector);
                        final Vector3f translation =
                                connector.getTranslation().clone();
                        baseEntity.getTransform().getRotation()
                                .mult(translation, translation);
                        translationVector.addLocal(translation);
                    }

                    movedTransform.setTranslation(translationVector);
                } else {
                    movedTransform.setTranslation(position.getPosition());
                }
                movedEntity.setTransform(movedTransform);
            }
        }

        // Calculate the bounding box.

        final ImmutableSet<Entity> set = entityBuilder.build();

        return set;
    }

    private Optional<String> discardEmpty(final String string) {
        if (string.isEmpty())
            return Optional.absent();
        else
            return Optional.fromNullable(string);
    }

    /**
     * Creates a new instance of the specified class of entity.
     * 
     * @param entityClass
     *            The class of entity to instantiate.
     * @param className
     *            The name in the level data for the entity class.
     * @param args
     *            The construction arguments for the class.
     * @param loc
     *            The current source code location.
     * @return A constructed entity instance of the specified class.
     * @throws LevelLoadException
     *             if it was impossible to instantiate the specified class.
     */
    Entity createEntity(final Class<? extends Entity> entityClass,
            final ImmutableList<Object> args, final String className,
            final int loc) throws LevelLoadException {

        try {
            return ConstructorUtils.invokeConstructor(entityClass,
                    args.toArray());
        } catch (final InstantiationException e) {
            throw new LevelLoadException("Could not create a new `" + className
                    + "': Class doesn't have an exposed constructor",
                    LevelLoadException.Kind.INCOMPATIBLE_CLASS, loc, e);
        } catch (final IllegalAccessException e) {
            throw new LevelLoadException("Could not create a new `" + className
                    + "': We are not allowed to load that class",
                    LevelLoadException.Kind.INVALID_CLASS, loc, e);
        } catch (final NoSuchMethodException e) {
            throw new LevelLoadException("Could not create a new `" + className
                    + "': Class doesn't have a constructor with " + args.size()
                    + " arguments of the correct type(s)",
                    LevelLoadException.Kind.INCOMPATIBLE_INITIALIZER, loc, e);
        } catch (final InvocationTargetException e) {
            throw new LevelLoadException("Error while initializing a `"
                    + className + "': " + e.getCause().getMessage(),
                    LevelLoadException.Kind.INITIALIZATION_ERROR, loc,
                    e.getCause());
        }
    }

    /**
     * Locates the class file for a specified entity class.
     * 
     * @param className
     *            The name of the entity class to locate.
     * @param loc
     *            The current source code location.
     * @return The found class.
     * @throws LevelLoadException
     *             if the class could not be found or if it does not implement
     *             the {@link Entity} interface.
     */
    Class<? extends Entity> loadEntityClass(final String className,
            final int loc) throws LevelLoadException {

        try {
            return Class.forName(className).asSubclass(Entity.class);
        } catch (final ClassNotFoundException e) {
            throw new LevelLoadException("Could not load entity class `"
                    + className + "': Class not found",
                    LevelLoadException.Kind.INVALID_CLASS, loc, e);
        } catch (final ClassCastException e) {
            throw new LevelLoadException("Could not load entity class `"
                    + className + "': Class is not an Entity",
                    LevelLoadException.Kind.INCOMPATIBLE_CLASS, loc, e);
        }
    }

    ImmutableMap<String, Integer> loadHighscores(
            final JSONObject highscoreObject) throws JSONException {
        final ImmutableMap.Builder<String, Integer> resultBuilder =
                ImmutableMap.builder();

        final Iterator<?> keysIterator = highscoreObject.keys();
        while (keysIterator.hasNext()) {
            final String key = (String) keysIterator.next();
            final Integer value = highscoreObject.getInt(key);
            resultBuilder.put(key, value);
        }
        return resultBuilder.build();
    }

    MetaLevel loadMetaLevel(final JSONObject levelObject,
            final Optional<URL> packURL) throws JSONException,
            LevelLoadException {
        try {
            final String previewURIString = levelObject.optString("previewUri");
            final Optional<URL> previewURI =
                    previewURIString == null ? Optional.<URL> absent()
                            : Optional.of(makeRelativeURL(packURL,
                                    previewURIString));
            final URL url =
                    makeRelativeURL(packURL, levelObject.getString("uri"));
            return new MetaLevel(levelObject.getString("name"), url,
                    previewURI, UUID.fromString(levelObject.getString("uuid")));
        } catch (final MalformedURLException e) {
            throw new LevelLoadException("Invalid URI",
                    LevelLoadException.Kind.INVALID_URI, -1, e);
        }
    }

    MetaLevelPack loadMetaLevelPack(final JSONObject object, final URL packURL)
            throws JSONException, LevelLoadException {
        return new MetaLevelPack(object.getString("name"),
                discardEmpty(object.optString("version")),
                discardEmpty(object.optString("description")),
                discardEmpty(object.optString("author")), loadMetaLevels(
                        object.getJSONArray("levels"), packURL));
    }

    ImmutableList<MetaLevel> loadMetaLevels(final JSONArray array,
            final URL packURL) throws JSONException, LevelLoadException {
        final ImmutableList.Builder<MetaLevel> resultBuilder =
                ImmutableList.builder();
        final Optional<URL> definitelyPackURL = Optional.of(packURL);
        for (int i = 0; i < array.length(); i++) {
            resultBuilder.add(loadMetaLevel(array.getJSONObject(i),
                    definitelyPackURL));
        }
        return resultBuilder.build();
    }

    URL makeRelativeURL(final Optional<URL> baseURL,
            final String relativeURLString) throws MalformedURLException {
        if (baseURL.isPresent())
            return new URL(baseURL.get(), relativeURLString);
        else
            return new URL(relativeURLString);
    }

    /**
     * Checks to see that a connector is valid; throws an exception otherwise.
     */
    void validateConnector(final Connector connector,
            final String connectorName, final String entityName, final int loc)
            throws LevelLoadException {

        if (connector == null)
            throw new LevelLoadException("No connector named `" + connectorName
                    + "' on entity `" + entityName + "'",
                    LevelLoadException.Kind.INCOMPATIBLE_ENTITY, loc);
    }

    /**
     * Checks to see that an entity is valid; throws an exception otherwise.
     */
    void validateEntity(final Entity entity, final String entityName,
            final int loc) throws LevelLoadException {

        if (entity == null)
            throw new LevelLoadException("No entity called `" + entityName
                    + "'", LevelLoadException.Kind.INVALID_ENTITY, loc);
    }

    /**
     * Checks to see that an entity supports connectivity and casts it
     * accordingly; throws an exception otherwise.
     */
    Connected withConnectivity(final Entity entity, final String entityName,
            final int loc) throws LevelLoadException {

        if (entity instanceof Connected)
            return (Connected) entity;
        else
            throw new LevelLoadException("Entity `" + entityName
                    + "' does not support connectivity",
                    LevelLoadException.Kind.INCOMPATIBLE_ENTITY, loc);
    }
}
