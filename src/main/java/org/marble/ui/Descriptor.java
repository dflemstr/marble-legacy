package org.marble.ui;

import com.google.common.base.Optional;

public interface Descriptor<A> {
    public String getName(A a);

    public Optional<String> getDescription(A a);
}
