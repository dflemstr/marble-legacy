package org.marble.input;

public enum PlayerInput {
    MoveBackward("move-backward"), MoveForward("move-forward"), MoveLeft(
            "move-left"), MoveRight("move-right"), Pause("pause");
    private final String name;

    private PlayerInput(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
