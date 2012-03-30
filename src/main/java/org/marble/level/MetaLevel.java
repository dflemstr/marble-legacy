package org.marble.level;

import java.net.URL;

public class MetaLevel {
    private final String name;
    private final URL uri;
    private final URL previewURI;

    public MetaLevel(final String name, final URL uri, final URL previewURI) {
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

    public URL getPreviewURI() {
        return previewURI;
    }

}
