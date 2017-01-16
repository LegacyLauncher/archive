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
    protected final Popup popupMenu;
    private Notice notice;

    public NoticePanel(NoticeManager manager, float fontSize) {
        super(CenterPanel.tipTheme, new MagnifiedInsets(8, 10, 8, 10));
        setInsets(0, 0, 0, 0);

        this.manager = manager;
        this.fontSize = fontSize;

        noticeWrapper = new NoticeWrapper();
        add(noticeWrapper);

        this.popupMenu = new Popup();

        Server server = new Server("MaxCraft", "mc.maxcraft.ru", 25565) {
            {
                setFamily("1.10");
                getAccountTypeSet().clear();
                getAccountTypeSet().add(Account.AccountType.ELY);
            }
        };
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
        private final ActionButton actionButton;

        private NoticeWrapper() {
            setHgap(SwingUtil.magnify(10));

            iconLabel = new DelayedIcon();
            //iconLabel.setBorder(BorderFactory.createLineBorder(Color.blue));
            setWest(iconLabel);

            editorPane = new NoticeEditorPane();
            //editorPane.setBorder(BorderFactory.createLineBorder(Color.magenta));
            setCenter(editorPane);

            actionButton = new ActionButton();
            //actionButton.setBorder(BorderFactory.createLineBorder(Color.red));
            setEast(actionButton);
        }

        Dimension updateSize() {
            Dimension size = calcNoticeSize(notice);
            log("notice size:", size);

            NoticeImage image = notice.getImage();
            iconLabel.setImage(image.getTask(), image.getWidth(), image.getHeight(), 0, size.height);

            int iconWidth = iconLabel.getIconWidth();
            Dimension buttonSize = actionButton.updateSize(size.height);

            int width = 0;
            width += getInsets().left + getInsets().right;
            log("width", width);
            width += iconLabel.getInsets().left + iconLabel.getInsets().right + iconWidth;
            log("width", width);
            width += getHgap();
            log("width", width);
            width += size.width + editorPane.getInsets().left + editorPane.getInsets().right;
            log("width", width);
            width += getHgap();
            log("width", width);
            width += buttonSize.width;
            log("width", width);
            width += getHgap();
            log("width", width);

            int height = 0;
            height += getInsets().top + getInsets().bottom;
            log("height", height);
            height += size.height;
            log("height", height);


            log("noticewrapper:", new Dimension(width, height));
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

    private class ActionButton extends ExtendedButton {
        private final ImageIcon icon;

        ActionButton() {
            setIcon(icon = new ImageIcon(Images.getImage("go.png"), BUTTON_ICON_WIDTH, true));
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    popupMenu.show(ActionButton.this);
                }
            });
        }

        Dimension updateSize(int height) {
            Dimension size = new Dimension(SwingUtil.magnify(BUTTON_INSETS) * 2 + icon.getIconWidth(), height);
            setPreferredSize(size);
            return size;
        }
    }

    class Popup extends JPopupMenu {
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
    }
}
