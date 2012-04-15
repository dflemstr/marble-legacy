package org.marble.input;

public enum PlayerInput {
    MoveForward("move-forward"), MoveBackward("move-backward"), MoveRight(
            "move-right"), MoveLeft("move-left");
    private final String name;

    private PlayerInput(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
