package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;

import javax.swing.*;
import java.awt.*;

public class NoticePanel extends CenterPanel implements Blockable, NoticeManagerListener {
    private final NoticeWrapper noticeWrapper;

    protected final NoticeManager manager;
    private Notice notice;

    public NoticePanel(NoticeManager manager, float fontSize) {
        super(CenterPanel.tipTheme, new MagnifiedInsets(8, 10, 8, 10));
        setInsets(0, 0, 0, 0);

        this.manager = manager;

        noticeWrapper = new NoticeWrapper(manager, new ParamPair(fontSize, -1));
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

        noticeWrapper.setNotice(notice);

        Dimension
                insets = new Dimension(getInsets().left + getInsets().right, getInsets().top + getInsets().bottom),
                noticeSize = noticeWrapper.updateSize();

        if (noticeSize == null) {
            setSize(0, 0);
            return;
        }

        int width = 0;
        width += insets.width;
        width += noticeSize.width;
        width += 5; // to be sure

        int height = 0;
        height += insets.height;
        height += noticeSize.height;

        setSize(width, height);
    }

    public void redraw() {
        if (getGraphics() != null) {
            update(getGraphics());
        }
    }

    protected void registerExtraItems(JMenuItem... items) {
        noticeWrapper.buttonPane.extra.popup.clearMenu();
        for (JMenuItem item : items) {
            noticeWrapper.buttonPane.extra.popup.registerItem(item);
        }
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

    @Override
    public void onNoticePromoted(Notice promotedNotice) {
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
