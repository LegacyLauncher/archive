package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedTextArea;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class LoggerFrame extends JFrame implements LocalizableComponent {
    private final Configuration config;
    private final int capacity;

    private final ExtendedTextArea textArea;
    private final ScrollPane scrollPane;
    private final BottomPanel bottomPanel;

    LoggerFrame(Configuration config, int capacity) {
        this.config = config;
        this.capacity = capacity;

        textArea = new ExtendedTextArea();
        initTextArea();

        scrollPane = new ScrollPane(textArea);
        initScrollPane();

        bottomPanel = new BottomPanel();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, "Center");
        getContentPane().add(bottomPanel, "South");

        initFrame();
    }

    LoggerFrame(Configuration config) {
        this(config, 1024 * 1024);
    }

    public void append(String text) {
        SwingUtilities.invokeLater(() -> syncAppend(text));
    }

    public void clear() {
        SwingUtilities.invokeLater(this::syncClear);
    }

    public void showFrame() {
        SwingUtilities.invokeLater(() -> {
            updateFromConfiguration();
            setVisible(true);
            scrollDown();
        });
    }

    public void disposeFrame() {
        this.listenerThread.dispose();
        SwingUtilities.invokeLater(this::dispose);
    }

    public void setFolderAction(Runnable action) {
        SwingUtilities.invokeLater(() -> bottomPanel.setFolder(action));
    }

    public void setSaveAction(Runnable action) {
        SwingUtilities.invokeLater(() -> bottomPanel.setSave(action));
    }

    public void setKillAction(Runnable action) {
        SwingUtilities.invokeLater(() -> bottomPanel.setKill(action));
    }

    private void updateFromConfiguration() {
        String prefix = "gui.logger.";
        int width = config.getInteger(prefix + "width", 670);
        int height = config.getInteger(prefix + "height", 500);
        int x = config.getInteger(prefix + "x", 0);
        int y = config.getInteger(prefix + "y", 0);
        setSize(width, height);
        setLocation(x, y);
    }

    private void delayedSave() {
        SwingUtilities.invokeLater(this::saveStateInfoConfiguration);
    }

    private void saveStateInfoConfiguration() {
        if (!isDisplayable() || !isVisible()) {
            return;
        }
        String prefix = "gui.logger.";
        Dimension size = getSize();
        Point location = getLocation();
        config.set(prefix + "width", size.width, false);
        config.set(prefix + "height", size.height, false);
        config.set(prefix + "x", location.x, false);
        config.set(prefix + "y", location.y, false);
    }

    private void syncAppend(String text) {
        boolean mustScrollDown;
        try {
            mustScrollDown = insertText(text);
        } catch (RuntimeException | BadLocationException ignored) {
            mustScrollDown = false;
        }
        if (mustScrollDown || shouldScrollDown) {
            SwingUtilities.invokeLater(this::scrollDown);
        }
    }

    private boolean insertText(String text) throws BadLocationException {
        final Document document = textArea.getDocument();
        document.insertString(document.getLength(), text, null);
        int length = document.getLength();
        if (length >= capacity) {
            document.remove(0, capacity / 2);
            return true;
        }
        return false;
    }

    private void syncClear() {
        final Document document = textArea.getDocument();
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException ignored) {
        }
    }

    private void scrollDown() {
        final JScrollBar vert = scrollPane.getVerticalScrollBar();
        vert.setValue(vert.getMaximum());
    }

    private void initTextArea() {
        textArea.setLineWrap(true);
        textArea.setEditable(false);
        textArea.setAutoscrolls(true);
        textArea.setMargin(new Insets(0, 0, 0, 0));
        textArea.setFont(new Font("DialogInput", Font.PLAIN, (int) ((double) (new LocalizableLabel()).getFont().getSize() * 1.2D)));
        textArea.setForeground(Color.white);
        textArea.setCaretColor(Color.white);
        textArea.setBackground(Color.black);
        textArea.setSelectionColor(Color.gray);
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(2);
        textArea.addMouseListener(new LoggerPopup(this));
    }

    private void initScrollPane() {
        scrollPane.setBorder(null);
        scrollPane.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        initVerticalScrollbar();
    }

    private boolean shouldScrollDown = true;
    private int lastWindowWidth, scrollBarValue;

    private void initVerticalScrollbar() {
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                lastWindowWidth = getWidth();
            }
        });
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (getWidth() == lastWindowWidth) {
                BoundedRangeModel vsbModel = scrollPane.getVerticalScrollBar().getModel();
                int nv = e.getValue();
                if (nv < scrollBarValue) {
                    shouldScrollDown = false;
                } else if (nv == vsbModel.getMaximum() - vsbModel.getExtent()) {
                    shouldScrollDown = true;
                }

                scrollBarValue = nv;
            }
        });
    }

    private ExtendedComponentAdapter listenerThread;

    private void initFrame() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (config.getLoggerType() == Configuration.LoggerType.GLOBAL) {
                    config.setLoggerType(Configuration.LoggerType.NONE);
                    if (TLauncher.getInstance().isReady() &&
                            TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.isLoaded()) {
                        TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get().updateValues();
                    }
                }
            }
        });
        addComponentListener(listenerThread = new ExtendedComponentAdapter(this) {
            public void onComponentResized(ComponentEvent e) {
                delayedSave();
            }

            public void onComponentMoved(ComponentEvent e) {
                delayedSave();
            }
        });
        SwingUtil.setFavicons(this);
        updateLocale();
    }

    @Override
    public void updateLocale() {
        SwingUtilities.invokeLater(() -> setTitle(Localizable.get("logger")));
    }
}
