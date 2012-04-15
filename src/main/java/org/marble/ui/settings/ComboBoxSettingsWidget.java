package org.marble.ui.settings;

import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.model.ComboBoxModel;

import com.google.common.base.Objects;

import org.marble.settings.Entry;
import org.marble.settings.EntryListener;

public class ComboBoxSettingsWidget<E> extends UIComboBox {
    private final class SynchronizingSelectionListener implements
            SelectionListener<UIComboBox> {
        private final Entry<E> entry;

        private SynchronizingSelectionListener(final Entry<E> entry) {
            this.entry = entry;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void selectionChanged(final UIComboBox component,
                final Object newValue) {
            entry.setValue((E) newValue);
        }
    }

    private final class SynchronizingEntryListener implements EntryListener<E> {
        @Override
        public void entryChanged(final E value) {
            setSelectedValue(value);
        }
    }

    private final Entry<E> entry;
    private final EntryListener<E> entryListener;

    public ComboBoxSettingsWidget(final Entry<E> entry,
            final ComboBoxModel model) {
        super(model);

        this.entry = entry;

        entryListener = new SynchronizingEntryListener();

        final SelectionListener<UIComboBox> selectionListener =
                new SynchronizingSelectionListener(entry);
        addSelectionListener(selectionListener);
    }

    private void setSelectedValue(final E value) {
        final ComboBoxModel model = getModel();

        for (int i = 0; i < model.size(); i++) {
            if (Objects.equal(value, model.getValueAt(i))) {
                setSelectedIndex(i);
                return;
            }
        }

        throw new RuntimeException("Value " + value + " not an alternative in "
                + model);
    }

    @Override
    public void attachedToHud() {
        entry.addEntryListener(entryListener);
        setSelectedValue(entry.getValue());
        super.attachedToHud();
    }

    @Override
    public void detachedFromHud() {
        entry.removeEntryListener(entryListener);
        super.attachedToHud();
    }
}
