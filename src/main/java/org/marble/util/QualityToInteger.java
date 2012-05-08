package org.marble.util;

import com.google.common.base.Function;

public final class QualityToInteger implements Function<Quality, Integer> {
    @Override
    public Integer apply(final Quality input) {
        return input.ordinal() + 4;
    }
}
