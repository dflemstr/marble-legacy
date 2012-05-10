package org.marble.ui;

public enum UIScreen {
    Game("game"), Highscores("highscores"), LevelPacks("level-packs"), Levels(
            "levels"), Loss("loss"), Pause("pause"), Settings("settings"),
    Start("start"), Win("win");
    private String name;

    private UIScreen(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
