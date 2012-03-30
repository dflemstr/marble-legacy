package org.marble.level;

import java.net.URL;

import com.google.common.base.Optional;

public class MetaLevel {
    private final String name;
    private final URL uri;
    private final Optional<URL> previewURI;

    public MetaLevel(final String name, final URL uri,
            final Optional<URL> previewURI) {
        this.name = name;
        this.uri = uri;
        this.previewURI = previewURI;
    }

    public String getName() {
        return name;
    }

    public URL getUri() {
        return uri;
    }

    public Optional<URL> getPreviewURI() {
        return previewURI;
    }

}
