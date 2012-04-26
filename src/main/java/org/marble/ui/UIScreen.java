package org.marble.ui;

public enum UIScreen {
    Start("start"), LevelPacks("level-packs"), Levels("levels"), Settings(
            "settings"), Game("game"), Pause("pause"), Win("win"),
    Loss("loss"), Highscores("highscores");
    private String name;

    private UIScreen(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
