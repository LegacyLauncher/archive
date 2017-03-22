package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.DelayedIcon;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.*;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class NoticePanel extends CenterPanel implements Blockable, NoticeManagerListener {
    private static final int BUTTON_ICON_WIDTH = 24, BUTTON_INSETS = 5;

    private final float fontSize;
    private final NoticeWrapper noticeWrapper;

    protected final NoticeManager manager;
    private Notice notice;

    public NoticePanel(NoticeManager manager, float fontSize) {
        super(CenterPanel.tipTheme, new MagnifiedInsets(8, 10, 8, 10));
        setInsets(0, 0, 0, 0);

        this.manager = manager;
        this.fontSize = fontSize;

        noticeWrapper = new NoticeWrapper();
        add(noticeWrapper);
    }

    public NoticePanel(NoticeManager manager) {
        this(manager, TLauncherFrame.getFontSize());
    }

    public final Notice getNotice() {
        return notice;
    }

    public final void setNotice(Notice notice) {
        this.notice = notice;
        updateNotice();
    }

    protected void updateNotice() {
        if (notice == null) {
            setSize(0, 0);
            return;
        }

        noticeWrapper.editorPane.setNotice(notice, fontSize);

        Dimension
                insets = new Dimension(getInsets().left + getInsets().right, getInsets().top + getInsets().bottom),
                noticeSize = noticeWrapper.updateSize();

        if(noticeSize == null) {
            setSize(0, 0);
            return;
        }

        log("noticeSize", noticeSize);

        int width = 0;
        width += insets.width;
        width += noticeSize.width;
        width += 5; // to be sure

        int height = 0;
        height += insets.height;
        height += noticeSize.height;

        log("size:", width, height);
        setSize(width, height);
    }

    public void redraw() {
        if (getGraphics() != null) {
            update(getGraphics());
        }
    }

    protected void registerExtraAction(String image, ActionListener listener) {
        noticeWrapper.buttonPane.extra.registerAction(image, listener);
    }

    protected void registerExtraItems(JMenuItem... items) {
        noticeWrapper.buttonPane.extra.popup.clearMenu();
        for(JMenuItem item : items) {
            noticeWrapper.buttonPane.extra.popup.registerItem(item);
        }
    }

    protected void log(Object... o) {
        U.log("[NoticePanel]", o);
    }

    @Override
    public void block(Object var1) {
        Blocker.blockComponents(var1, noticeWrapper);
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblockComponents(var1, noticeWrapper);
    }

    @Override
    public void onNoticeSelected(Notice notice) {
        setNotice(notice);
    }

    private class NoticeWrapper extends BorderPanel {
        private final DelayedIcon iconLabel;
        private final NoticeEditorPane editorPane;
        private final ButtonPane buttonPane;
        //private final Button actionButton, extraButton;

        private NoticeWrapper() {
            setHgap(SwingUtil.magnify(10));

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

        Dimension updateSize() {
            Dimension size = calcNoticeSize(notice);
            log("notice size:", size);

            if(size.width == 0 && size.height == 0) {
                return null;
            }

            NoticeImage image = notice.getImage();
            iconLabel.setImage(image.getTask(), image.getWidth(), image.getHeight(), 0, size.height);

            int iconWidth = iconLabel.getIconWidth();
            Dimension buttonSize = buttonPane.updateSize(size.height);

            int width = 0;
            width += getInsets().left + getInsets().right;
            //log("width", width);
            width += iconLabel.getInsets().left + iconLabel.getInsets().right + iconWidth;
            //log("width", width);
            width += getHgap();
            //log("width", width);
            width += size.width + editorPane.getInsets().left + editorPane.getInsets().right;
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
            if(manager == null) {
                return new NoticeTextSize(notice).get(fontSize);
            } else {
                return manager.getTextSize(notice, fontSize);
            }
        }
    }

    private class ButtonPane extends ExtendedPanel {
        private final GridBagConstraints c = new GridBagConstraints();
        private final Button action, extra;

        ButtonPane() {
            setInsets(0, 0, 0, 0);
            this.action = new Button();
            action.registerAction("go.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(notice.getAction() != null) {
                        action.popup.clearMenu();
                        NoticeAction noticeAction = notice.getAction();
                        for (JMenuItem item : noticeAction.getMenuItemList()) {
                            action.popup.registerItem(item);
                        }
                        action.popup.show(action);
                    }
                }
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

        Dimension updateSize(int height) {
            int width;

            if(!extra.popup.items.isEmpty() || extra.listener != null) {
                boolean hor = height < SwingUtil.magnify(BUTTON_ICON_WIDTH) * 2;
                int useHeight = hor? height : height / 2;
                int actionWidth = action.updateSize(useHeight).width, extraWidth = extra.updateSize(useHeight).width;

                if(hor) {
                    width = actionWidth + extraWidth;
                } else {
                    width = Math.max(actionWidth, extraWidth);
                }

                removeAll();
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.NORTH;
                c.fill = hor? GridBagConstraints.VERTICAL : GridBagConstraints.HORIZONTAL;

                add(action, c);

                if(hor) {
                    c.gridx++;
                } else {
                    c.gridy++;
                }

                c.anchor = GridBagConstraints.SOUTH;
                add(extra, c);
            } else {
                width = action.updateSize(height).width;
                remove(extra);
            }

            return new Dimension(width, height);
        }
    }

    private class Button extends ExtendedButton {
        private final Popup popup;
        private ActionListener listener;
        private final ImageIcon icon;

        Button() {
            setMargin(new Insets(0, 0, 0, 0));

            popup = new Popup();
            setIcon(icon = new ImageIcon(Images.getImage("ellipsis-v.png"), BUTTON_ICON_WIDTH, true));
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(listener != null) {
                        listener.actionPerformed(e);
                    } else {
                        popup.show(Button.this);
                    }
                }
            });
        }

        void registerAction(String image, ActionListener l) {
            icon.setImage(Images.getImage(image), BUTTON_ICON_WIDTH, true);
            listener = l;
        }

        Dimension updateSize(int height) {
            final int insets = SwingUtil.magnify(BUTTON_INSETS) * 2;
            if(height < insets + icon.getHeight()) {
                icon.setIconHeight(height - insets);
            }
            Dimension size = new Dimension(insets + icon.getIconWidth(), height);
            setPreferredSize(size);
            return size;
        }
    }

    class Popup extends JPopupMenu {
        private final List<JMenuItem> items = new ArrayList<>();

        void clearMenu() {
            items.clear();
            removeAll();
        }

        void registerItem(JMenuItem item) {
            items.add(item);
        }

        void updateMenu() {
            removeAll();

            if(notice == null) {
                return;
            }

            if(!items.isEmpty()) {
                for(JMenuItem item : items) {
                    if(item == null) {
                        addSeparator();
                    } else {
                        add(item);
                    }
                }
            }
        }

        void show(Component comp) {
            updateMenu();
            show(comp, comp.getWidth(), 0);
        }
    }

    /*class Popup extends JPopupMenu {
        private final List<JMenuItem> items = new ArrayList<JMenuItem>();

        void registerItem(JMenuItem item) {
            items.add(item);
        }

        void updateMenu() {
            removeAll();

            if(notice == null) {
                return;
            }

            if(notice.getAction() != null) {
                NoticeAction action = notice.getAction();
                for (JMenuItem item : action.getMenuItemList()) {
                    add(item);
                }
            }

            if(!items.isEmpty()) {
                if(notice.getAction() != null) {
                    addSeparator();
                }

                for(JMenuItem item : items) {
                    add(item);
                }
            }
        }

        void show(Component comp) {
            updateMenu();
            show(comp, comp.getWidth(), 0);
        }
    }*/
}
