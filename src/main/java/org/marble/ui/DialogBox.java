package org.marble.ui;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.border.EmptyBorder;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.BorderLayout;
import com.ardor3d.extension.ui.layout.BorderLayoutData;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.extension.ui.util.SubTex;
import com.ardor3d.image.Texture;
import com.ardor3d.util.TextureManager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;

public class DialogBox extends UIFrame {

    class ButtonActionListener implements ActionListener {
        final Button button;

        public ButtonActionListener(final Button button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(final ActionEvent event) {
            if (listener != null) {
                listener.dialogDone(button);
            }
            DialogBox.super.close();
        }

    }

    private DialogListener listener;

    public void setDialogListener(final DialogListener listener) {
        this.listener = listener;
    }

    public interface DialogListener {
        public void dialogDone(Button pressedButton);

        public void dialogClosed();
    }

    public DialogBox(final String title, final String message) {
        this(title, message, Optional.<Icon> absent());
    }

    public DialogBox(final String title, final String message, final Icon icon) {
        this(title, message, Optional.of(icon), ImmutableSet.of(Button.OK));
    }

    public DialogBox(final String title, final String message,
            final Optional<Icon> icon, final Button... buttons) {
        this(title, message, icon, ImmutableSet.<Button> builder().add(buttons)
                .build());
    }

    public DialogBox(final String title, final String message,
            final Optional<Icon> icon, final ImmutableSet<Button> buttons) {
        super(title);

        final UIPanel buttonPanel = new UIPanel(new RowLayout(true));
        // TODO use better sort
        final ImmutableSortedMap.Builder<Integer, Button> buttonWeightBuilder =
                ImmutableSortedMap.naturalOrder();
        for (final Button button : buttons) {
            buttonWeightBuilder.put(button.getWeight(), button);
        }
        for (final Button button : buttonWeightBuilder.build().values()) {
            final UIButton uiButton = new UIButton(button.getLabel());
            uiButton.addActionListener(new ButtonActionListener(button));
            if (button.getIcon().isPresent()) {
                final Texture iconTexture =
                        TextureManager.load("icons/actions/dialog-"
                                + button.getIcon().get() + ".png",
                                Texture.MinificationFilter.Trilinear, false);
                uiButton.setIcon(new SubTex(iconTexture));
            }
            buttonPanel.add(uiButton);
        }

        final UILabel messageLabel = new UILabel(message);
        messageLabel.setGap(8);
        messageLabel.setBorder(new EmptyBorder(16, 16, 16, 16));
        if (icon.isPresent()) {
            final Texture iconTexture =
                    TextureManager.load("icons/status/dialog-"
                            + icon.get().getIcon() + ".png",
                            Texture.MinificationFilter.Trilinear, false);
            messageLabel.setIcon(new SubTex(iconTexture));
        }

        messageLabel.setMinimumContentSize(400, 300);

        final UIPanel mainPanel = new UIPanel(new BorderLayout());
        mainPanel.setMinimumContentSize(400, 300);
        messageLabel.setLayoutData(BorderLayoutData.CENTER);
        mainPanel.add(messageLabel);
        buttonPanel.setLayoutData(BorderLayoutData.SOUTH);
        mainPanel.add(buttonPanel);

        setContentPanel(mainPanel);
        setResizeable(false);
        pack();
    }

    @Override
    public void close() {
        if (listener != null) {
            listener.dialogClosed();
        }
        super.close();
    }

    private static final Optional<String> absent = Optional.<String> absent();

    public enum Button {
        OK(16, "OK", Optional.of("ok")), Cancel(8, "Cancel", Optional
                .of("cancel")), Close(12, "Close", Optional.of("close")), Yes(
                10, "Yes", absent), No(9, "No", absent), Abort(13, "Abort",
                absent), Retry(14, "Retry", absent), Ignore(15, "Ignore",
                absent);
        private final int weight;
        private final String label;
        private final Optional<String> icon;

        private Button(final int weight, final String label,
                final Optional<String> icon) {
            this.weight = weight;
            this.label = label;
            this.icon = icon;
        }

        public int getWeight() {
            return weight;
        }

        public String getLabel() {
            return label;
        }

        public Optional<String> getIcon() {
            return icon;
        }
    }

    public enum Icon {
        Information("information"), Warning("warning"), Critical("critical"),
        Question("information");
        private final String icon;

        private Icon(final String icon) {
            this.icon = icon;
        }

        public String getIcon() {
            return icon;
        }
    }
}
