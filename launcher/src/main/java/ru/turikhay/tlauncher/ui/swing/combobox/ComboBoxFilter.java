package ru.turikhay.tlauncher.ui.swing.combobox;

import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

public class ComboBoxFilter<T> {
    private final JComboBox<T> comboBox;
    private final Supplier<List<T>> valueProvider;
    private final BiPredicate<T, String> filter;
    private final Function<T, IconText> textFunction;
    private T selectedItem;
    private FilterEditor<T> filterEditor;

    private ComboBoxFilter(JComboBox<T> comboBox,
                           Supplier<List<T>> valueProvider, BiPredicate<T, String> filter,
                           Function<T, IconText> textFunction) {
        this.comboBox = comboBox;
        this.valueProvider = valueProvider;
        this.filter = filter;
        this.textFunction = textFunction;
    }

    private void init() {
        prepareComboFiltering();
        initComboPopupListener();
        initComboKeyListener();
    }

    public void updateState() {
        filterEditor.updateState();
    }

    private void prepareComboFiltering() {
        for (T item : this.valueProvider.get()) {
            this.comboBox.addItem(item);
        }

        filterEditor = new FilterEditor<>(textFunction, editing -> {
            if (editing) {
                selectedItem = (T) comboBox.getSelectedItem();
            } else {
                comboBox.setSelectedItem(selectedItem);
                filterEditor.setItem(selectedItem);
            }
        });

        JTextField filterLabel = filterEditor.getTextField();
        filterLabel.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    comboBox.showPopup();
                    filterEditor.hideCaret();
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
                resetFilterComponent();
            }
        });
        comboBox.setEditor(filterEditor);
        comboBox.setEditable(true);
    }

    private void initComboKeyListener() {
        filterEditor.getTextField().addKeyListener(
                new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        char keyChar = e.getKeyChar();
                        if (!Character.isDefined(keyChar)) {
                            return;
                        }
                        int keyCode = e.getKeyCode();
                        switch (keyCode) {
                            case KeyEvent.VK_ENTER:
                                selectedItem = (T) comboBox.getSelectedItem();
                                resetFilterComponent();
                                return;
                            case KeyEvent.VK_ESCAPE:
                                filterEditor.hideCaret();
                                return;
                            default:
                                filterEditor.addChar();
                        }
                        if (!comboBox.isPopupVisible()) {
                            comboBox.showPopup();
                        }
                        SwingUtilities.invokeLater(() -> {
                            if (filterEditor.isEditing()) {
                                applyFilter();
                            } else {
                                resetFilterComponent();
                            }
                        });
                    }
                }
        );
    }

    private void initComboPopupListener() {
        comboBox.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                T item = (T) comboBox.getSelectedItem();
                resetFilterComponent();
                if (item != null) {
                    comboBox.setSelectedItem(item);
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                resetFilterComponent();
            }
        });
    }

    private final AtomicInteger modelCounter = new AtomicInteger();

    private void resetFilterComponent() {
        if (!filterEditor.isEditing()) {
            return;
        }
        modelCounter.incrementAndGet();
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
        model.removeAllElements();
        for (T item : this.valueProvider.get()) {
            model.addElement(item);
        }
        filterEditor.reset();
    }

    private void applyFilter() {
        final int currentUse = modelCounter.incrementAndGet();
        final String term = filterEditor.getTextField().getText();
        AsyncThread.execute(() -> {
            List<T> matchingItems = new ArrayList<>();
            for (T item : this.valueProvider.get()) {
                if (filter.test(item, term)) {
                    matchingItems.add(item);
                }
            }
            SwingUtilities.invokeLater(() -> {
                if (modelCounter.get() != currentUse) {
                    return; // outdated
                }
                DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) comboBox.getModel();
                model.removeAllElements();
                matchingItems.forEach(model::addElement);
            });
        });
    }

    public static <T> ComboBoxFilter<T> decorate(JComboBox<T> comboBox,
                                                 Supplier<List<T>> valueProvider,
                                                 Function<T, IconText> textFunction,
                                                 BiPredicate<T, String> filter) {
        ComboBoxFilter<T> decorator =
                new ComboBoxFilter<>(comboBox, valueProvider, filter, textFunction);
        decorator.init();
        return decorator;
    }
}