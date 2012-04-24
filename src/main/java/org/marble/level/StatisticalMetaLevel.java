package org.marble.level;

import java.net.URL;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

public class StatisticalMetaLevel extends MetaLevel {

    public StatisticalMetaLevel(final MetaLevel level,
            final ImmutableMap<String, Integer> highscores) {
        this(level.getName(), level.getUri(), level.getPreviewURI(), level
                .getUUID(), highscores);
    }

    public StatisticalMetaLevel(final String name, final URL uri,
            final Optional<URL> previewURI, final UUID uuid,
            final ImmutableMap<String, Integer> highscores) {
        super(name, uri, previewURI, uuid);
        this.highscores = highscores;
    }

    public ImmutableMap<String, Integer> getHighscores() {
        return highscores;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        final JSONObject result = super.toJSON();
        final JSONObject highscoresObject = new JSONObject();
        for (final Map.Entry<String, Integer> highscore : highscores.entrySet()) {
            highscoresObject.append(highscore.getKey(), highscore.getValue());
        }
        result.append("highscores", highscoresObject);
        return result;
    }

    private final ImmutableMap<String, Integer> highscores;
}
