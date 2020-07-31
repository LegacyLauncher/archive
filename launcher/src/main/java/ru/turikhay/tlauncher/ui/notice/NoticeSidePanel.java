package ru.turikhay.tlauncher.ui.notice;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.BlockableButton;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.Del;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;


public class NoticeSidePanel extends CenterPanel implements LocalizableComponent {
    private static final int BUTTON_IMAGE_SIZE = 24, BUTTON_PANEL_HEIGHT = 36, NOTICE_IMAGE_SIZE = 48;

    private final DefaultScene scene;
    private final NoticeManager noticeManager;

    private final ExtendedPanel panel;
    private final ScrollPane scrollPane;

    private final ButtonPanel buttonPanel;
    private final int workHeight;

    public NoticeSidePanel(DefaultScene scene) {
        super(new MagnifiedInsets(15, 15, 10, 15));

        this.scene = scene;
        setSize(SwingUtil.magnify(new Dimension(600, 550)));

        BorderPanel wrapper = new BorderPanel();
        wrapper.setInsets(0, 0, 0, 0);
        add(wrapper);

        panel = new ExtendedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setInsets(0, 0, 0, 0);

        scrollPane = new ScrollPane(panel, ScrollPane.ScrollBarPolicy.AS_NEEDED, ScrollPane.ScrollBarPolicy.NEVER, false);
        scrollPane.setBorder(null);
        wrapper.setCenter(scrollPane);
        //add(del(Del.CENTER));
        wrapper.setSouth(buttonPanel = new ButtonPanel());

        workHeight = getHeight() -
                getInsets().top - getInsets().bottom -
                buttonPanel.controlButton.getInsets().top - buttonPanel.controlButton.getInsets().bottom - SwingUtil.magnify(BUTTON_PANEL_HEIGHT); // - 10 as safezone

        noticeManager = scene.getMainPane().getRootFrame().getNotices();
        updateLocale();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                if(page != null) {
                    Stats.noticeListViewed(page.noticeList);
                }
            }
        });
    }

    /*@Override
    public void onNoticeSelected(Notice notice) {
        panel.removeAll();
        NoticeWrapper wrapper = new NoticeWrapper(noticeManager, TLauncherFrame.getFontSize(), 350, 32);
        wrapper.setNotice(notice);
        wrapper.setMaximumSize(wrapper.updateSize());
        panel.add(wrapper);
    }*/

    private final List<Page> pages = new ArrayList<>();
    private Page page;
    private int pageNumber;

    private void fillPages(List<Notice> noticeList) {
        pages.clear();
        if(noticeList == null) {
            return;
        }
        Page page = new Page();
        for(final Notice notice : noticeList) {
            NoticeWrapper wrapper = new NoticeWrapper(noticeManager, TLauncherFrame.getFontSize(), getWidth() - SwingUtil.magnify(NoticeWrapper.GAP) * 2 - getInsets().left - getInsets().right - SwingUtil.magnify(NOTICE_IMAGE_SIZE) - SwingUtil.magnify(32), SwingUtil.magnify(NOTICE_IMAGE_SIZE));
            /*final LocalizableMenuItem showAtMenuItem = LocalizableMenuItem.newItem("notice.sidepanel.popup.show-at-menu", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    noticeManager.selectNotice(notice, true);
                }
            });
            wrapper.buttonPane.action.popup.registerItem(showAtMenuItem);

            final LocalizableMenuItem hideItem = LocalizableMenuItem.newItem("notice.sidepanel.popup.make-promoted", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    noticeManager.setPromoted(notice);
                }
            });
            wrapper.buttonPane.action.popup.registerItem(hideItem);
            wrapper.buttonPane.action.popup.registerVisibilityHandler(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showAtMenuItem.setEnabled(noticeManager.getSelectedNotice() != notice);
                    hideItem.setEnabled(noticeManager.isPromotedAllowed() && !noticeManager.isHidden(notice) && noticeManager.getSelectedNotice() != notice);
                }
            });*/
            wrapper.setNotice(notice);

            Dimension size = wrapper.updateSize();
            if(size == null) {
                continue;
            }
            wrapper.setMaximumSize(size);

            Del del = del(Del.CENTER);

            int height = size.height + Del.SIZE;
            if(page.height + height > workHeight) {
                pages.add(page);
                page = new Page();
            }

            page.height += height;
            page.list.add(wrapper);
            page.list.add(del);
            page.noticeList.add(notice);
        }
        if(!page.list.isEmpty()) {
            pages.add(page);
        }
    }

    private void renderPage(int i) {
        pageNumber = i;
        panel.removeAll();

        Blocker.setBlocked(buttonPanel.left, "firstPage", i < 1);
        Blocker.setBlocked(buttonPanel.right, "lastPage", (i + 1) >= pages.size());

        boolean invisible = Blocker.isBlocked(buttonPanel.left) && Blocker.isBlocked(buttonPanel.right);
        buttonPanel.setEast(null);
        buttonPanel.setWest(null);
        if(invisible) {
            buttonPanel.setEast(buttonPanel.controlButton);
        } else {
            buttonPanel.setWest(buttonPanel.controlButton);
            buttonPanel.setEast(buttonPanel.pageButtons);
        }

        if(i < 0 || i >= pages.size()) {
            return;
        }

        page = pages.get(i);
        for(Component comp : page.list) {
            panel.add(comp);
        }

        validate();
        repaint();

        if(TLauncher.getInstance() != null && TLauncher.getInstance().isReady()) {
            if(page != null) {
                Stats.noticeListViewed(page.noticeList);
            }
        }
    }

    @Override
    public void updateLocale() {
        List<Notice> list = noticeManager.getForCurrentLocale();
        fillPages(list);
        renderPage(0);

        //buttonPanel.promotedItem.setVisible(Configuration.isUSSRLocale(TLauncher.getInstance().getLang().getLocale().toString()));
    }

    private class Page {
        private final List<Component> list;
        private final List<Notice> noticeList;
        private int height;

        Page() {
            list = new ArrayList<>();
            noticeList = new ArrayList<>();
        }
    }

    private class ButtonPanel extends BorderPanel {
        JPopupMenu popup = new JPopupMenu();
        LocalizableMenuItem promotedItem;

        ExtendedButton controlButton;
        ExtendedPanel pageButtons;
        BlockableButton left, right;

        ButtonPanel() {
            setPreferredSize(new Dimension(
                    NoticeSidePanel.this.getWidth() - NoticeSidePanel.this.getInsets().left - NoticeSidePanel.this.getInsets().right,
                    SwingUtil.magnify(BUTTON_PANEL_HEIGHT)
            ));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, SwingUtil.magnify(BUTTON_PANEL_HEIGHT)));

            promotedItem = new LocalizableMenuItem();
            promotedItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    togglePromoted();
                }
            });
            popup.add(promotedItem);
            refreshPromoted();

            popup.add(LocalizableMenuItem.newItem("notice.sidepanel.control.restore", "refresh.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    noticeManager.restoreHidden();
                }
            }));

            popup.addSeparator();

            popup.add(LocalizableMenuItem.newItem("notice.sidepanel.control.hide", "collapse.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    scene.setNoticeSidePanelEnabled(false);
                }
            }));

            controlButton = newButton("bars.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    refreshPromoted();
                    popup.show(controlButton, 0, controlButton.getHeight());
                }
            });
            //setWest(controlButton);
            //setEast(controlButton);

            pageButtons = new ExtendedPanel();
            pageButtons.setInsets(0, 0, 0, 0);
            pageButtons.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            c.gridx = -1;
            c.insets = new Insets(0, SwingUtil.magnify(2), 0, SwingUtil.magnify(2));
            c.weighty = 1.0;

            c.gridx++;
            pageButtons.add(left = newButton("arrow-left.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    renderPage(pageNumber - 1);
                }
            }), c);

            c.gridx++;
            pageButtons.add(right = newButton("arrow-right.png", new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    renderPage(pageNumber + 1);
                }
            }), c);

            //setEast(p);
        }

        BlockableButton newButton(String path, ActionListener listener) {
            BlockableButton button = new BlockableButton(Images.getIcon(path, SwingUtil.magnify(BUTTON_IMAGE_SIZE)));
            button.addActionListener(listener);
            return button;
        }

        private void togglePromoted() {
            boolean current = noticeManager.isPromotedAllowed();
            noticeManager.setPromotedAllowed(!current);
            refreshPromoted();
        }

        private void refreshPromoted() {
            boolean enabled = TLauncher.getInstance().getSettings().getBoolean("notice.promoted");
            String path = "notice.promoted.", image;
            if(enabled) {
                path += "hide";
                image = "eye-slash.png";
            } else {
                path += "restore";
                image = "eye.png";
            }
            promotedItem.setText(path);
            promotedItem.setIcon(Images.getIcon(image, 16));
        }

        /*private void togglePromoted() {
            boolean current = TLauncher.getInstance().getSettings().getBoolean("minecraft.servers.promoted");
            TLauncher.getInstance().getSettings().set("minecraft.servers.promoted", !current);
            refreshPromoted();
        }

        private void refreshPromoted() {
            boolean enabled = TLauncher.getInstance().getSettings().getBoolean("minecraft.servers.promoted");
            String path = "notice.sidepanel.control.promoted.", image;
            if(enabled) {
                path += "disable";
                image = "eye-slash.png";
            } else {
                path += "enable";
                image = "eye.png";
            }
            promotedItem.setText(path);
            promotedItem.setIcon(Images.getIcon(image, 16));
        }*/
    }
}
