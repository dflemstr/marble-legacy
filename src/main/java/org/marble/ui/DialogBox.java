package org.marble.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.Element;
import de.lessvoid.nifty.elements.render.TextRenderer;

public final class DialogBox {
    private DialogBox() {
    }

    public static void showDialog(final Nifty nifty, final String message) {
        final Element popup = nifty.createPopup("dialog");
        popup.findElementByName("dialog-text").getRenderer(TextRenderer.class)
                .setText(message);
        popup.layoutElements();

        nifty.showPopup(nifty.getCurrentScreen(), "dialog", null);
    }

    public enum Button {
        OK(16, "OK"), Cancel(8, "Cancel"), Close(12, "Close"), Yes(10, "Yes"),
        No(9, "No"), Abort(13, "Abort"), Retry(14, "Retry"), Ignore(15,
                "Ignore");
        private final int weight;
        private final String label;

        private Button(final int weight, final String label) {
            this.weight = weight;
            this.label = label;
        }

        public int getWeight() {
            return weight;
        }

        public String getLabel() {
            return label;
        }
    }

    public enum Icon {
        Information("dialog-information"), Warning("dialog-warning"), Critical(
                "dialog-error"), Question("dialog-information");
        private final String icon;

        private Icon(final String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }
    }
}
