package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.tlauncher.ui.notice.NoticeManager;
import ru.turikhay.tlauncher.ui.notice.NoticeManagerListener;
import ru.turikhay.tlauncher.ui.notice.NoticePanel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedToggleButton;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class NoticeScene extends PseudoScene implements ResizeableComponent {
    public static final float ADDED_FONT_SIZE = 2.f;
    private final static int IMAGE_WIDTH = 24, SAFE_ZONE = 10;

    private final NoticePanel noticePanel;
    private final ButtonPanel buttonPanel;

    private final float fontSize = TLauncherFrame.getFontSize() + ADDED_FONT_SIZE;

    public NoticeScene(MainPane main) {
        super(main);

        this.noticePanel = new NoticePanel(main.getRootFrame().getNotices(), fontSize) {
            @Override
            protected void updateNotice() {
                super.updateNotice();
                NoticeScene.this.onResize();
            }
        };
        add(noticePanel);

        this.buttonPanel = new ButtonPanel();
        add(buttonPanel);

        getMainPane().getRootFrame().getNotices().addListener(noticePanel, true);
        getMainPane().getRootFrame().getNotices().addListener(new NoticeManagerListener() {
            @Override
            public void onNoticeSelected(Notice notice) {
                updateNoticeVisibility();
            }

            @Override
            public void onNoticePromoted(Notice promotedNotice) {
                updateNoticeVisibility();
            }
        }, true);
    }

    private void changeNotice(int indexDelta) {
        setVisible(false);

        Notice selected = null;
        List<Notice> noticeList = getMainPane().getRootFrame().getNotices().getForCurrentLocale();

        if (noticeList != null) {
            int currentIndex = noticeList.indexOf(noticePanel.getNotice());

            if (currentIndex != -1) {
                currentIndex = (currentIndex + indexDelta + noticeList.size()) % noticeList.size();
                selected = noticeList.get(currentIndex);
            }
        }

        getMainPane().getRootFrame().getNotices().selectNotice(selected, true);

        setVisible(true);
        noticePanel.redraw();
    }

    private void updateNoticeVisibility() {
        if (noticePanel.getNotice() != null) {
            boolean hidden = getMainPane().getRootFrame().getNotices().isHidden(noticePanel.getNotice());

            Blocker.setBlocked(noticePanel, "notice_disabled", hidden);
            buttonPanel.visible.setEnabled(true);
            buttonPanel.visible.setSelected(!hidden);
        } else {
            buttonPanel.visible.setEnabled(false);
        }
    }

    public void setShown(boolean shown, boolean animate) {
        super.setShown(shown, animate);

        if (shown) {
            Stats.noticeSceneShown();
            NoticeManager manager = getMainPane().getRootFrame().getNotices();

            List<Notice> list = manager.getForCurrentLocale();
            if (list != null) {
                if (manager.getSelectedNotice() == null) {
                    manager.selectNotice(list.get(0), false);
                }
            }

            noticePanel.redraw();
        }
    }

    @Override
    public void onResize() {
        super.onResize();
        noticePanel.setLocation((getWidth() - noticePanel.getWidth()) / 2, (getHeight() - noticePanel.getHeight()) / 2);
        buttonPanel.setLocation((getWidth() - buttonPanel.getWidth()) / 2, getHeight() - buttonPanel.getHeight() - SwingUtil.magnify(SAFE_ZONE));
    }

    private class ButtonPanel extends ExtendedPanel implements ResizeableComponent {
        private final Insets insets = new MagnifiedInsets(0, 5, 0, 5);
        private final ExtendedButton prev, next, home;
        private final ExtendedToggleButton visible;

        ButtonPanel() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.insets = insets;
            c.gridx = -1;
            c.gridy = 0;
            c.fill = GridBagConstraints.VERTICAL;

            prev = newButton("arrow-left", e -> changeNotice(-1));
            c.gridx++;
            add(prev, c);

            next = newButton("arrow-right", e -> changeNotice(1));
            c.gridx++;
            add(next, c);

            visible = newToggleButton("eye.png", "eye-slash.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (noticePanel.getNotice() == null) {
                        return;
                    }
                    boolean hidden = !visible.isSelected();
                    getMainPane().getRootFrame().getNotices().setHidden(noticePanel.getNotice(), hidden);
                    updateNoticeVisibility();
                }
            });
            c.gridx++;
            add(visible, c);

            home = newButton("home", e -> getMainPane().openDefaultScene());
            c.gridx++;
            add(home, c);
        }

        @Override
        public void onResize() {
            int width = 0;
            width += (insets.left + insets.right) * 4;
            width += prev.getPreferredSize().width;
            width += next.getPreferredSize().width;
            width += home.getPreferredSize().width;
            width += visible.getPreferredSize().width;

            int height = 0;
            height = Math.max(height, prev.getPreferredSize().height);
            height = Math.max(height, next.getPreferredSize().height);
            height = Math.max(height, home.getPreferredSize().height);
            height = Math.max(height, visible.getPreferredSize().height);

            setBounds(0, 0, width, height);
        }

        private void setupButton(AbstractButton b, String iconPath, ActionListener l) {
            ImageIcon icon = newImage(iconPath);
            icon.setup(b);

            Dimension d = new Dimension(
                    b.getInsets().left + b.getInsets().right + icon.getIconWidth(),
                    b.getInsets().top + b.getInsets().bottom + icon.getIconHeight()
            );

            b.setPreferredSize(d);
            b.setSize(d);
            b.addActionListener(l);
        }

        private ExtendedButton newButton(String iconPath, ActionListener l) {
            ExtendedButton b = new ExtendedButton();
            setupButton(b, iconPath, l);
            return b;
        }

        private ExtendedToggleButton newToggleButton(String enabledIcon, String disabledIcon, ActionListener l) {
            final ExtendedToggleButton b = new ExtendedToggleButton();
            final ImageIcon enabled = newImage(enabledIcon), disabled = newImage(disabledIcon);

            b.addChangeListener(e -> {
                b.setIcon(b.isSelected() ? enabled : disabled);
                b.setDisabledIcon(disabled);
            });

            b.setSelected(true);
            //b.setIcon(enabled);
            //b.setDisabledIcon(disabled);

            Dimension d = new Dimension(
                    b.getInsets().left + b.getInsets().right + Math.max(enabled.getIconWidth(), disabled.getIconWidth()),
                    b.getInsets().top + b.getInsets().bottom + Math.max(enabled.getIconHeight(), disabled.getIconHeight())
            );

            b.setPreferredSize(d);
            b.setSize(d);
            b.addActionListener(l);

            return b;
        }

        private ImageIcon newImage(String iconPath) {
            return Images.getIcon24(iconPath);
        }
    }

}
