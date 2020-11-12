package ru.turikhay.tlauncher.ui.logger;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.EmptyAction;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedTextArea;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.tlauncher.pasta.Pasta;
import ru.turikhay.tlauncher.pasta.PastaListener;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class LoggerFrame extends JFrame implements LocalizableComponent {
    public static final int MIN_WIDTH = 670;
    public static final int MIN_HEIGHT = 500;
    public final Logger logger;
    public final JTextArea textarea;
    public final JScrollBar vScrollbar;
    public final LoggerFrameBottom bottom;
    public final LoggerTextPopup popup;
    private int lastWindowWidth;
    private int scrollBarValue;
    private boolean scrollDown;
    private final Object busy = new Object();
    boolean hiding;

    LoggerFrame(Logger logger) {
        this.logger = logger;
        textarea = new ExtendedTextArea();
        textarea.setLineWrap(true);
        textarea.setEditable(false);
        textarea.setAutoscrolls(true);
        textarea.setMargin(new Insets(0, 0, 0, 0));
        textarea.setFont(new Font("DialogInput", 0, (int) ((double) (new LocalizableLabel()).getFont().getSize() * 1.2D)));
        textarea.setForeground(Color.white);
        textarea.setCaretColor(Color.white);
        textarea.setBackground(Color.black);
        textarea.setSelectionColor(Color.gray);
        ((DefaultCaret) textarea.getCaret()).setUpdatePolicy(2);
        popup = new LoggerTextPopup();
        textarea.addMouseListener(popup);
        ScrollPane scrollPane = new ScrollPane(textarea);
        scrollPane.setBorder(null);
        scrollPane.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        vScrollbar = scrollPane.getVerticalScrollBar();
        final BoundedRangeModel vsbModel = vScrollbar.getModel();
        vScrollbar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                if (getWidth() == lastWindowWidth) {
                    int nv = e.getValue();
                    if (nv < scrollBarValue) {
                        scrollDown = false;
                    } else if (nv == vsbModel.getMaximum() - vsbModel.getExtent()) {
                        scrollDown = true;
                    }

                    scrollBarValue = nv;
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                lastWindowWidth = getWidth();
            }
        });
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(scrollPane, "Center");
        getContentPane().add(bottom = new LoggerFrameBottom(this), "South");
        SwingUtil.setFavicons(this);
    }

    public void println(String string) {
        print(string + '\n');
    }

    public void print(String string) {
        Object var2 = busy;
        synchronized (busy) {
            Document document = textarea.getDocument();

            try {
                document.insertString(document.getLength(), string, null);
            } catch (Throwable var5) {
            }

            if (scrollDown) {
                scrollDown();
            }

        }
    }

    public void clear() {
        textarea.setText("");
    }

    public void scrollDown() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                vScrollbar.setValue(vScrollbar.getMaximum());
            }
        });
    }

    public void updateLocale() {
        Localizable.updateContainer(this);
    }

    void hideIn(final long millis) {
        hiding = true;
        bottom.closeCancelButton.setVisible(true);
        bottom.closeCancelButton.setText("logger.close.cancel", Long.valueOf(millis / 1000L));
        AsyncThread.execute(new Runnable() {
            long remaining = millis;

            public void run() {
                bottom.closeCancelButton.setText("logger.close.cancel", Long.valueOf(remaining / 1000L));

                while (hiding && remaining > 1999L) {
                    remaining -= 1000L;
                    bottom.closeCancelButton.setText("logger.close.cancel", Long.valueOf(remaining / 1000L));
                    U.sleepFor(1000L);
                }

                if (hiding) {
                    dispose();
                }

            }
        });
    }

    public class LoggerTextPopup extends TextPopup {
        private final Action saveAllAction = new EmptyAction() {
            public void actionPerformed(ActionEvent e) {
                logger.saveAs();
            }
        };
        private final Action clearAllAction = new EmptyAction() {
            public void actionPerformed(ActionEvent e) {
                onClearCalled();
            }
        };

        protected JPopupMenu getPopup(MouseEvent e, JTextComponent comp) {
            JPopupMenu menu = super.getPopup(e, comp);
            if (menu == null) {
                return null;
            } else {
                menu.addSeparator();
                menu.add(saveAllAction).setText(Localizable.get("logger.save.popup"));
                menu.addSeparator();
                menu.add(clearAllAction).setText(Localizable.get("logger.clear.popup"));
                return menu;
            }
        }

        protected void onClearCalled() {
            logger.clear();
        }
    }
}
