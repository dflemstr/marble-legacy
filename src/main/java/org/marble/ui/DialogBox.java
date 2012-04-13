package org.marble.ui;

import com.ardor3d.extension.ui.UIFrame;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

public class DialogBox extends UIFrame {
    public DialogBox(final String title, final ImmutableSet<Button> buttons,
            final Icon icon) {
        super(title);
        // TODO use better sort
        final ImmutableSortedMap.Builder<Integer, Button> buttonWeightBuilder =
                ImmutableSortedMap.naturalOrder();

        for (final Button button : buttons) {
            buttonWeightBuilder.put(button.getWeight(), button);
        }

        buttonWeightBuilder.build().values();
        setResizeable(false);
    }

    public enum Button {
        OK(16), Cancel(8), Close(12), Yes(10), No(9), Abort(13), Retry(14),
        Ignore(15);
        private final int weight;

        private Button(final int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    public enum Icon {
        NoIcon, Information, Warning, Critical, Question;
    }
}
