package org.marble.level;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

public class StatisticalMetaLevel {

    public StatisticalMetaLevel(final ImmutableMap<String, Integer> highscores) {
        this.highscores = highscores;
    }

    public ImmutableMap<String, Integer> getHighscores() {
        return highscores;
    }

    public JSONObject toJSON() throws JSONException {
        final JSONObject result = new JSONObject();
        final JSONObject highscoresObject = new JSONObject();
        for (final Map.Entry<String, Integer> highscore : highscores.entrySet()) {
            highscoresObject.put(highscore.getKey(), highscore.getValue());
        }
        result.put("highscores", highscoresObject);
        return result;
    }

    private final ImmutableMap<String, Integer> highscores;
}
