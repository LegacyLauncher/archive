package net.legacylauncher.ui.notice;

import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.images.DelayedIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedButton;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

class NoticeCard extends ExtendedPanel {
    static final int GAP = 5;
    private static final int BUTTON_ICON_WIDTH = 24, BUTTON_INSETS = 5;
    private final NoticeManager manager;
    final ParamPair paramPair;
    final int fixedIconWidth;

    private Notice notice;

    private final DelayedIcon iconLabel;
    final NoticeEditorPane editorPane;
    final ButtonPane buttonPane;

    private final BorderPanel standardNotice = new BorderPanel();
    private final JLabel banner = new JLabel();

    public NoticeCard(NoticeManager manager, float fontSize, int fixedWidth, int iconWidth) {
        setLayout(new CardLayout());

        this.manager = manager;

        if (fixedWidth > 0) {
            this.paramPair = new ParamPair(fontSize, fixedWidth);
            this.fixedIconWidth = iconWidth;
        } else {
            this.paramPair = new ParamPair(fontSize, -1);
            this.fixedIconWidth = 0;
        }

        standardNotice.setHgap(SwingUtil.magnify(GAP));
        add(standardNotice, "standard");

        banner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        banner.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (notice == null) {
                    return;
                }
                int button = e.getButton();
                if (button == MouseEvent.BUTTON1) {
                    if (notice.getAction() != null) {
                        Runnable runnable = notice.getAction().getRunnable();
                        if (runnable != null) {
                            runnable.run();
                        } else {
                            showPopup(e);
                        }
                    }
                } else if (button == MouseEvent.BUTTON3) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                Popup popup = new Popup();
                setupActionPopup(popup);
                popup.registerItem(null);
                buttonPane.extra.popup.items.forEach(popup::registerItem);
                popup.updateMenu();
                popup.show(banner, e.getX(), e.getY());
            }
        });
        add(banner, "banner");

        iconLabel = new DelayedIcon();
        //iconLabel.setBorder(BorderFactory.createLineBorder(Color.blue));
        standardNotice.setWest(iconLabel);

        editorPane = new NoticeEditorPane();
        //editorPane.setBorder(BorderFactory.createLineBorder(Color.magenta));
        standardNotice.setCenter(editorPane);

        buttonPane = new ButtonPane();
        //buttonPane.setBorder(BorderFactory.createLineBorder(Color.red));
        standardNotice.setEast(buttonPane);
    }

    NoticeCard(NoticeManager manager, ParamPair param) {
        this(manager, param.fontSize, param.width, -1);
    }

    void setNotice(Notice notice) {
        this.notice = notice;
        if (notice.getBanner() != null) {
            ((CardLayout) getLayout()).show(this, "banner");
            notice.getBanner().handle((image, err) -> {
                if (err != null) {
                    setupStandardNotice();
                } else {
                    SwingUtil.later(() -> banner.setIcon(new ImageIcon(image)));
                }
                return null;
            });
        } else {
            setupStandardNotice();
        }
    }

    private void setupStandardNotice() {
        ((CardLayout) getLayout()).show(this, "standard");
        editorPane.setNotice(notice, paramPair);
        buttonPane.updateButton();
    }

    Dimension updateSize() {
        if (notice.getBanner() != null) {
            return Notice.BANNER_DIMENSIONS;
        }

        Dimension size = calcNoticeSize(notice);

        if (size.width == 0 && size.height == 0) {
            return null;
        }

        NoticeImage image = notice.getImage();

        final int setIconWidth, setIconHeight;
        int additionalWidth = 0;
        if (fixedIconWidth > 0 && size.height >= fixedIconWidth) {
            setIconWidth = fixedIconWidth;
            setIconHeight = 0;
        } else {
            setIconWidth = 0;
            setIconHeight = size.height;
        }
        iconLabel.setImage(image, setIconWidth, setIconHeight);

        int iconWidth = iconLabel.getIconWidth();
        if (fixedIconWidth > 0 && fixedIconWidth != iconWidth) {
            additionalWidth += fixedIconWidth - iconWidth;
        }

        Dimension buttonSize = buttonPane.updateSize(size.height);

        int width = 0;
        width += getInsets().left + getInsets().right;
        //log("width", width);
        width += iconLabel.getInsets().left + iconLabel.getInsets().right + iconWidth;
        //log("width", width);
        width += standardNotice.getHgap();
        //log("width", width);
        width += size.width + additionalWidth + editorPane.getInsets().left + editorPane.getInsets().right;
        //log("width", width);
        width += standardNotice.getHgap();
        //log("width", width);
        width += buttonSize.width;
        //log("width", width);
        width += standardNotice.getHgap();
        //log("width", width);

        int height = 0;
        height += getInsets().top + getInsets().bottom;
        //log("height", height);
        height += size.height;
        //log("height", height);

        return new Dimension(width, height);
    }

    private Dimension calcNoticeSize(Notice notice) {
        if (manager == null) {
            return new NoticeTextSize(notice).get(paramPair);
        } else {
            return manager.getTextSize(notice, paramPair);
        }
    }

    private void setupActionPopup(Popup popup) {
        popup.clearMenu();
        if (notice == null) {
            return;
        }
        NoticeAction noticeAction = notice.getAction();
        for (JMenuItem item : noticeAction.getMenuItemList()) {
            popup.registerItem(item);
        }
        if (notice.getErid() != null) {
            JMenuItem eridItem = new JMenuItem("Реклама. Erid: " + notice.getErid());
            eridItem.addActionListener(e1 ->
                    OS.openLink(notice.getEridUrl())
            );
            popup.registerItem(null);
            popup.registerItem(eridItem);
        }
    }

    class ButtonPane extends ExtendedPanel {
        private final GridBagConstraints c = new GridBagConstraints();
        final Button action, extra;

        ButtonPane() {
            setInsets(0, 0, 0, 0);
            this.action = new Button();
            action.registerAction(e -> {
                if (notice == null) {
                    return;
                }
                setupActionPopup(action.popup);
                action.popup.show(action);
            });

            this.extra = new Button();

            setLayout(new GridBagLayout());

            c.gridx = 0;
            c.gridy = 0;
            c.fill = GridBagConstraints.VERTICAL;

            add(action, c);
        }

        void updatePopup() {
            action.popup.updateMenu();
            extra.popup.updateMenu();
        }

        void updateButton() {
            if (notice != null && notice.getAction() instanceof LauncherNoticeAction) {
                action.setGoColorIcon();
            } else {
                action.setGoIcon();
            }
        }

        Dimension updateSize(int height) {
            int width;

            if (!extra.popup.items.isEmpty() || extra.listener != null) {
                boolean hor = height < SwingUtil.magnify(BUTTON_ICON_WIDTH) * 2;
                int useHeight = hor ? height : height / 2;
                int actionWidth = action.updateSize(useHeight).width, extraWidth = extra.updateSize(useHeight).width;

                if (hor) {
                    width = actionWidth + extraWidth;
                } else {
                    width = Math.max(actionWidth, extraWidth);
                }

                removeAll();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = hor ? GridBagConstraints.WEST : GridBagConstraints.NORTH;
                c.fill = hor ? GridBagConstraints.VERTICAL : GridBagConstraints.HORIZONTAL;

                add(action, c);

                if (hor) {
                    c.gridx++;
                } else {
                    c.gridy++;
                }

                c.anchor = hor ? GridBagConstraints.EAST : GridBagConstraints.SOUTH;
                add(extra, c);
            } else {
                width = action.updateSize(height).width;
                remove(extra);
            }

            return new Dimension(width, height);
        }
    }

    class Button extends ExtendedButton {
        final Popup popup;
        private ActionListener listener;

        Button() {
            setMargin(new Insets(0, 0, 0, 0));

            popup = new Popup();
            setIcon(Images.getIcon24("ellipsis-v"));
            addActionListener(e -> {
                if (listener != null) {
                    listener.actionPerformed(e);
                } else {
                    popup.show(Button.this);
                }
            });
        }

        /*void updateIcon(String image) {
            setIcon(Images.getIcon(image, BUTTON_ICON_WIDTH));
        }*/

        void setGoIcon() {
            setIcon(Images.getIcon24("play-circle-o"));
        }

        void setGoColorIcon() {
            setIcon(Images.getIcon24("play-circle-o-1"));
        }

        void registerAction(ActionListener l) {
            setGoIcon();
            listener = l;
        }

        Dimension updateSize(int height) {
            final int insets = SwingUtil.magnify(BUTTON_INSETS) * 2;
            Dimension size = new Dimension(insets + BUTTON_ICON_WIDTH, height);
            setPreferredSize(size);
            return size;
        }
    }

    class Popup extends JPopupMenu {
        {
            addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    if (manager != null) {
                        Blocker.block(manager, this);
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    if (manager != null) {
                        Blocker.unblock(manager, this);
                    }
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    if (manager != null) {
                        Blocker.unblock(manager, this);
                    }
                }
            });
        }

        private final java.util.List<JMenuItem> items = new ArrayList<>();
        private ActionListener visbilityListener;

        void clearMenu() {
            items.clear();
            removeAll();
        }

        void registerItem(JMenuItem item) {
            items.add(item);
        }

        void registerVisibilityHandler(ActionListener l) {
            visbilityListener = l;
        }

        void updateMenu() {
            removeAll();

            if (notice == null) {
                return;
            }

            if (!items.isEmpty()) {
                for (JMenuItem item : items) {
                    if (item == null) {
                        addSeparator();
                    } else {
                        add(item);
                    }
                }
            }

            if (visbilityListener != null) {
                visbilityListener.actionPerformed(null);
            }
        }

        void show(Component comp) {
            updateMenu();
            show(comp, comp.getWidth(), 0);
        }
    }
}
