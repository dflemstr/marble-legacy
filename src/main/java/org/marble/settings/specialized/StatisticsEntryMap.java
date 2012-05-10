package org.marble.settings.specialized;

import java.util.UUID;
import java.util.prefs.Preferences;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;

import org.json.JSONException;
import org.json.JSONObject;

import org.marble.level.LevelLoader;
import org.marble.level.StatisticalMetaLevel;
import org.marble.settings.AbstractEntryMap;
import org.marble.util.StringSerializer;

public class StatisticsEntryMap extends
        AbstractEntryMap<UUID, StatisticalMetaLevel> {

    public StatisticsEntryMap(final Preferences prefs, final String baseNode) {
        super(prefs, baseNode, new StatisticalMetaLevel(
                ImmutableMap.<String, Integer> of()), new UUIDKeyTranslation(),
                new StatisticalValueSerializer());
    }

    private static class StatisticalValueSerializer implements
            StringSerializer<StatisticalMetaLevel> {
        private final LevelLoader levelLoader = new LevelLoader();

        @Override
        public StatisticalMetaLevel fromString(final String string) {
            try {
                return levelLoader.loadStatisticalMetaLevel(new JSONObject(
                        string));
            } catch (final JSONException e) {
                throw new RuntimeException(
                        "Malformed statistical level entry in settings", e);
            }
        }

        @Override
        public String toString(final StatisticalMetaLevel a) {
            try {
                return a.toJSON().toString();
            } catch (final JSONException e) {
                throw new RuntimeException(
                        "Invalid JSON serialization of statistical meta level",
                        e);
            }
        }

    }

    private static class UUIDKeyTranslation implements Function<UUID, String> {

        @Override
        public String apply(final UUID input) {
            return input.toString();
        }

    }
}
