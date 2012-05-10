package org.marble.level;

import java.net.URL;
import java.util.UUID;

import com.google.common.base.Optional;

import org.json.JSONException;
import org.json.JSONObject;

public class MetaLevel {
    private final String name;
    private final Optional<URL> previewURI;
    private final URL uri;
    private final UUID uuid;

    public MetaLevel(final String name, final URL uri,
            final Optional<URL> previewURI, final UUID uuid) {
        this.name = name;
        this.uri = uri;
        this.previewURI = previewURI;
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public Optional<URL> getPreviewURI() {
        return previewURI;
    }

    public URL getUri() {
        return uri;
    }

    public UUID getUUID() {
        return uuid;
    }

    public JSONObject toJSON() throws JSONException {
        final JSONObject result = new JSONObject();
        result.put("name", name);
        result.put("uri", uri.toExternalForm());
        if (previewURI.isPresent()) {
            result.put("previewURI", previewURI.get().toExternalForm());
        }
        result.put("uuid", uuid.toString());
        return result;
    }
}
