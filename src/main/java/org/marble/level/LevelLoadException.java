package org.marble.level;

/**
 * An exception describing a failure when loading a level. The error message
 * will always be human readable, and an exception suitable for debugging is set
 * as the cause.
 */
public class LevelLoadException extends Exception {
    /**
     * The kind of failure that occurred (for machine-readability)
     */
    public enum Kind {
        /** The specified class is not an usable class */
        INVALID_CLASS,
        /** The specified class is a class, but of the wrong kind. */
        INCOMPATIBLE_CLASS,
        /** The specified class has an incompatible initializer. */
        INCOMPATIBLE_INITIALIZER,
        /** An error occurred while initializing the class */
        INITIALIZATION_ERROR,
        /** The specified entity is not an entity */
        INVALID_ENTITY,
        /** The specified entity is an entity, but of the wrong kind */
        INCOMPATIBLE_ENTITY
    }

    private static final long serialVersionUID = -8722401716350049364L;

    private final int failureLocation;
    private final Kind kind;

    public LevelLoadException(final String message, final Kind kind,
            final int failureLocation) {
        super(message);
        this.kind = kind;
        this.failureLocation = failureLocation;
    }

    public LevelLoadException(final String message, final Kind kind,
            final int failureLocation, final Throwable cause) {
        super(message, cause);
        this.kind = kind;
        this.failureLocation = failureLocation;
    }

    /**
     * The approximate location (index) in the source code where the error
     * occurred.
     */
    public int getFailureLocation() {
        return failureLocation;
    }

    /**
     * The kind of error that occurred.
     */
    public Kind getKind() {
        return kind;
    }
}