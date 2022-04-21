package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.DelayedIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;

class NoticeWrapper extends BorderPanel {
    static final int GAP = 5;
    private static final int BUTTON_ICON_WIDTH = 24, BUTTON_INSETS = 5;
    private final NoticeManager manager;
    final ParamPair paramPair;
    final int fixedIconWidth;

    private Notice notice;

    private final DelayedIcon iconLabel;
    final NoticeEditorPane editorPane;
    final ButtonPane buttonPane;

    public NoticeWrapper(NoticeManager manager, float fontSize, int fixedWidth, int iconWidth) {
        setHgap(SwingUtil.magnify(GAP));

        this.manager = manager;

        if (fixedWidth > 0) {
            this.paramPair = new ParamPair(fontSize, fixedWidth);
            this.fixedIconWidth = iconWidth;
        } else {
            this.paramPair = new ParamPair(fontSize, -1);
            this.fixedIconWidth = 0;
        }

        iconLabel = new DelayedIcon();
        //iconLabel.setBorder(BorderFactory.createLineBorder(Color.blue));
        setWest(iconLabel);

        editorPane = new NoticeEditorPane();
        //editorPane.setBorder(BorderFactory.createLineBorder(Color.magenta));
        setCenter(editorPane);

        buttonPane = new ButtonPane();
        //buttonPane.setBorder(BorderFactory.createLineBorder(Color.red));
        setEast(buttonPane);
    }

    NoticeWrapper(NoticeManager manager, ParamPair param) {
        this(manager, param.fontSize, param.width, -1);
    }

    void setNotice(Notice notice) {
        this.notice = notice;
        editorPane.setNotice(notice, paramPair);
        buttonPane.updateButton();
    }

    Dimension updateSize() {
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
        width += getHgap();
        //log("width", width);
        width += size.width + additionalWidth + editorPane.getInsets().left + editorPane.getInsets().right;
        //log("width", width);
        width += getHgap();
        //log("width", width);
        width += buttonSize.width;
        //log("width", width);
        width += getHgap();
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
                if (notice.getAction() != null) {
                    action.popup.clearMenu();
                    NoticeAction noticeAction = notice.getAction();
                    for (JMenuItem item : noticeAction.getMenuItemList()) {
                        action.popup.registerItem(item);
                    }
                }
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