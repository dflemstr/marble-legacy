package org.marble.level;

import javax.vecmath.Vector3d;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Expresses a statement in the abstract language that describes a level.
 */
public abstract class LevelStatement {
    private final Integer location;

    protected LevelStatement(final Integer location) {
        this.location = location;
    }

    /**
     * The location (index) in the source code that this statement occurs at.
     */
    public Integer getLocation() {
        return location;
    }

    /**
     * A class alias definition.
     */
    static class Alias extends LevelStatement {
        private final String alias;
        private final String aliasedClass;

        public Alias(final Integer location, final String alias,
                final String aliasedClass) {
            super(location);
            this.alias = alias;
            this.aliasedClass = aliasedClass;
        }

        @Override
        public boolean equals(final Object that) {
            return that instanceof Alias
                    && Objects.equal(alias, ((Alias) that).alias)
                    && Objects.equal(aliasedClass, ((Alias) that).aliasedClass);
        }

        /**
         * The alias name.
         */
        public String getAlias() {
            return alias;
        }

        /**
         * The class for which the alias should be.
         */
        public String getAliasedClass() {
            return aliasedClass;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(alias, aliasedClass);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("name", alias)
                    .add("aliasedClass", aliasedClass).toString();
        }
    }

    /**
     * A connection definition between two entities, moving the first entity to
     * line up with the specified connection to the second entity.
     */
    static class Connection extends LevelStatement {
        private final String nameLeft, nameRight;
        private final String connectorLeft, connectorRight;

        public Connection(final Integer location, final String nameLeft,
                final String connectorLeft, final String nameRight,
                final String connectorRight) {
            super(location);
            this.nameLeft = nameLeft;
            this.connectorLeft = connectorLeft;
            this.nameRight = nameRight;
            this.connectorRight = connectorRight;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Connection) {
                final Connection that = (Connection) obj;
                return Objects.equal(nameLeft, that.nameLeft)
                        && Objects.equal(connectorLeft, that.connectorLeft)
                        && Objects.equal(nameRight, that.nameRight)
                        && Objects.equal(connectorRight, that.connectorRight);
            } else
                return false;
        }

        /**
         * The connector for the moved entity.
         */
        public String getConnectorLeft() {
            return connectorLeft;
        }

        /**
         * The connector for the base entity.
         */
        public String getConnectorRight() {
            return connectorRight;
        }

        /**
         * The moved entity.
         */
        public String getNameLeft() {
            return nameLeft;
        }

        /**
         * The base entity.
         */
        public String getNameRight() {
            return nameRight;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(nameLeft, connectorLeft, nameRight,
                    connectorRight);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("nameLeft", nameLeft)
                    .add("connectorLeft", connectorLeft)
                    .add("nameRight", nameRight)
                    .add("connectorRight", connectorRight).toString();
        }
    }

    /**
     * An entity definition for creating new entities.
     */
    static class Declaration extends LevelStatement {
        private final String name;
        private final String className;
        private final ImmutableList<Object> initArgs;

        public Declaration(final Integer location, final String name,
                final String className, final ImmutableList<Object> initArgs) {
            super(location);
            this.name = name;
            this.className = className;
            this.initArgs = initArgs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Declaration) {
                final Declaration that = (Declaration) obj;
                return Objects.equal(name, that.name)
                        && Objects.equal(className, that.className)
                        && Objects.equal(initArgs, that.initArgs);
            } else
                return false;
        }

        /**
         * The name or alias of the class to instantiate.
         */
        public String getClassName() {
            return className;
        }

        /**
         * The initialization arguments that should be passed to the entity's
         * constructor.
         */
        public ImmutableList<Object> getInitArgs() {
            return initArgs;
        }

        /**
         * The variable name of the entity.
         */
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, className, initArgs);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("name", name)
                    .add("className", className).add("initArgs", initArgs)
                    .toString();
        }
    }

    /**
     * A positioning statement that moves an entity to some location.
     */
    static class Position extends LevelStatement {
        private final String name;
        private final Vector3d position;
        private final Optional<String> relativeTo;

        public Position(final Integer location, final String name,
                final Vector3d position, final Optional<String> relativeTo) {
            super(location);
            this.name = name;
            this.position = position;
            this.relativeTo = relativeTo;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Position) {
                final Position that = (Position) obj;
                return Objects.equal(name, that.name)
                        && Objects.equal(position, that.position)
                        && Objects.equal(relativeTo, that.relativeTo);
            } else
                return false;
        }

        /**
         * The name of the entity to position.
         */
        public String getName() {
            return name;
        }

        /**
         * The position to use when positioning the entity.
         */
        public Vector3d getPosition() {
            return position;
        }

        /**
         * An optional parameter specifying an entity relative to which the
         * positioning should happen.
         */
        public Optional<String> getRelativeTo() {
            return relativeTo;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(name, position, relativeTo);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("name", name)
                    .add("position", position).add("relativeTo", relativeTo)
                    .toString();
        }
    }
}
