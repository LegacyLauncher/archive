package ru.turikhay.tlauncher.ui;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.AnimatorAction;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.editor.ExtendedHTMLEditorKit;
import ru.turikhay.tlauncher.ui.swing.editor.ServerHyperlinkProcessor;
import ru.turikhay.tlauncher.updater.Notices;
import ru.turikhay.tlauncher.updater.Stats;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;
import java.util.List;

public class NoticePanel extends CenterPanel implements ResizeableComponent, UpdaterListener, LocalizableComponent, Stats.StatsListener {
    private static final int MARGIN = 10;
    private static final float FONT_SIZE = SwingUtil.magnify(12.0F);
    private final NoticePanel.InfoPanelAnimator animator = new NoticePanel.InfoPanelAnimator();
    private final EditorPane browser;
    private final DefaultScene parent;
    private final Object animationLock = new Object();
    private final int timeFrame = 5;
    private float opacity;
    private boolean shown;
    private boolean canshow;
    private Notices ads;
    private String content;
    private int width;
    private int height;
    private int mouseX;
    private int mouseY;
    private Notices.Notice notice;

    public NoticePanel(DefaultScene p) {
        super(CenterPanel.tipTheme, new MagnifiedInsets(5, 10, 5, 10));
        //this.setMagnifyGaps(false);
        parent = p;
        browser = new EditorPane(getFont().deriveFont(FONT_SIZE));
        if (browser.getEditorKit() instanceof ExtendedHTMLEditorKit) {
            ((ExtendedHTMLEditorKit) browser.getEditorKit()).setHyperlinkProcessor(new ServerHyperlinkProcessor() {
                public void showPopup(JPopupMenu menu) {
                    menu.show(browser, mouseX, (int) ((float) mouseY + FONT_SIZE));
                }

                public void open(VersionSyncInfo vs, ServerList.Server server) {
                    LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
                    if (vs != null) {
                        lf.versions.setSelectedValue(vs);
                        if (!vs.equals(lf.versions.getSelectedValue())) {
                            return;
                        }
                    }

                    Account account = lf.accounts.getSelectedValue();
                    if (account != null && !server.isAccountTypeAllowed(account.getType())) {
                        List allowedList = server.getAllowedAccountTypeList();
                        String message;
                        if (allowedList.size() == 1) {
                            message = Localizable.get("ad.server.choose-account", Localizable.get("account.type." + allowedList.get(0)));
                        } else {
                            StringBuilder messageBuilder = (new StringBuilder(Localizable.get("ad.server.choose-account.multiple"))).append('\n');
                            Iterator var9 = allowedList.iterator();

                            while (var9.hasNext()) {
                                Account.AccountType type = (Account.AccountType) var9.next();
                                messageBuilder.append(Localizable.get("ad.server.choose-account.multiple.prefix", new Object[]{Localizable.get("account.type." + type)})).append('\n');
                            }

                            message = messageBuilder.substring(0, messageBuilder.length() - 1);
                        }

                        lf.scene.getMainPane().openAccountEditor();
                        Alert.showError(Localizable.get("ad.server.choose-account.title"), message);
                    } else {
                        lf.startLauncher(server);
                    }
                }
            });
        }

        browser.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (!onClick()) {
                    e.consume();
                }

            }

            public void mousePressed(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

                mouseX = e.getX();
                mouseY = e.getY();
            }

            public void mouseReleased(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mouseEntered(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mouseExited(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }
        });
        add(browser);
        shown = false;
        setVisible(false);

        TLauncher.getInstance().getUpdater().addListener(this);
        Stats.addListener(this);
    }

    void setContent(String text, int width, int height) {
        if (width >= 1 && height >= 1) {
            this.width = width;
            this.height = height;
            browser.setText(text);
            onResize();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void onResize() {
        Graphics g = getGraphics();
        if (g != null) {
            Insets insets = getInsets();
            int compWidth = width + insets.left + insets.right;
            int compHeight = height + insets.top + insets.bottom;
            Point loginFormLocation = parent.loginForm.getLocation();
            Dimension loginFormSize = parent.loginForm.getSize();
            int x = loginFormLocation.x + loginFormSize.width / 2 - compWidth / 2;
            if (x + compWidth > parent.getWidth() - 10) {
                x = parent.getWidth() - compWidth - 10;
            }

            if (x < 10) {
                x = 10;
            }

            int y;
            switch (parent.getLoginFormDirection()) {
                case TOP_LEFT:
                case TOP:
                case TOP_RIGHT:
                case CENTER_LEFT:
                case CENTER:
                case CENTER_RIGHT:
                    y = loginFormLocation.y + loginFormSize.height + 10;
                    break;
                case BOTTOM_LEFT:
                case BOTTOM:
                case BOTTOM_RIGHT:
                    y = loginFormLocation.y - compHeight - 10;
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            if (y + compHeight > parent.getHeight() - 10) {
                y = parent.getHeight() - compHeight - 10;
            }

            if (y < 10) {
                y = 10;
            }

            setBounds(x, y, compWidth, compHeight);
        }
    }

    public void paint(Graphics g0) {
        Graphics2D g = (Graphics2D) g0;
        Composite oldComp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(3, opacity));
        super.paint(g0);
        g.setComposite(oldComp);
    }

    public void show(boolean animate) {
        if (canshow) {
            onResize();
            if (!shown) {
                Object var2 = animationLock;
                synchronized (animationLock) {
                    setVisible(true);
                    browser.setVisible(true);
                    opacity = 0.0F;
                    float selectedOpacity = 1F;
                    if (animate) {
                        while (opacity < selectedOpacity) {
                            opacity += 0.05F;
                            if (opacity > selectedOpacity) {
                                opacity = selectedOpacity;
                            }

                            repaint();
                            U.sleepFor((long) timeFrame);
                        }
                    } else {
                        opacity = selectedOpacity;
                        repaint();
                    }

                    shown = true;
                }

                if (notice != null) {
                    Stats.noticeViewed(notice);
                }

            }
        }
    }

    public void show() {
        animator.act(AnimatorAction.SHOW);
    }

    void hide(boolean animate) {
        if (shown) {
            Object var2 = animationLock;
            synchronized (animationLock) {
                if (animate) {
                    while (opacity > 0.0F) {
                        opacity -= 0.05F;
                        if (opacity < 0.0F) {
                            opacity = 0.0F;
                        }

                        repaint();
                        U.sleepFor((long) timeFrame);
                    }
                }

                setVisible(false);
                browser.setVisible(false);
                if (!animate) {
                    opacity = 0.0F;
                }

                shown = false;
            }
        }
    }

    public void hide() {
        animator.act(AnimatorAction.HIDE);
    }

    public void setShown(boolean shown, boolean animate) {
        if (animate) {
            if (shown) {
                show();
            } else {
                hide();
            }
        } else if (shown) {
            show(false);
        } else {
            hide(false);
        }

    }

    boolean onClick() {
        return isEnabled() && shown;
    }

    public void onUpdaterRequesting(Updater u) {
        hide(true);
    }

    public void onUpdaterErrored(Updater.SearchFailed failed) {
    }

    public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
        ads = succeeded.getResponse().getNotices();
        updateNotice(true);
    }

    public void updateNotice(boolean animate) {
        hide(animate);
        canshow = prepareNotice();
        if (parent.getSidePanel() != DefaultScene.SidePanel.SETTINGS) {
            show(animate);
        }

    }

    private boolean prepareNotice() {
        if (ads == null) {
            notice = null;
            return false;
        } else {
            String locale = parent.getMainPane().getRootFrame().getLauncher().getSettings().getLocale().toString();
            Notices.NoticeList noticeList = ads.getByName(locale);
            if (noticeList != null && !noticeList.getList().isEmpty()) {
                notice = noticeList.getRandom();
                if (notice == null) {
                    return false;
                } else {
                    boolean isAllowed = allowAlways || (!notice.getType().isAdvert() || tlauncher.getSettings().getBoolean("gui.notice." + notice.getType().name().toLowerCase()));
                    if (!isAllowed) {
                        notice = null;
                        return false;
                    } else {
                        StringBuilder builder = new StringBuilder();
                        int width = (int) (notice.getWidth() * TLauncherFrame.magnifyDimensions), height = (int) (notice.getHeight() * TLauncherFrame.magnifyDimensions);
                        builder.append("<table width=\"").append(width).append("\" height=\"").append(height).append("\"><tr><td align=\"center\" valign=\"center\">");
                        if (notice.getImage() != null) {
                            builder.append("<img src=\"").append(notice.getImage()).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
                        }

                        builder.append(notice.getContent());
                        builder.append("</td></tr></table>");
                        content = builder.toString();
                        setContent(content, width, height);
                        return true;
                    }
                }
            } else {
                return false;
            }
        }
    }

    public void block(Object reason) {
    }

    public void unblock(Object reason) {
    }

    public void updateLocale() {
        updateNotice(false);
    }

    private boolean allowAlways = false;

    @Override
    public void onInvalidSubmit(String message) {
        if ("multiple".equals(message)) {
            allowAlways = true;

            for (Notices.NoticeType type : Notices.NoticeType.values()) {
                tlauncher.getSettings().set("gui.notice." + type.name().toLowerCase(), true, false);
            }
            tlauncher.getSettings().store();

            if (notice == null) {
                updateNotice(TLauncher.getInstance().isReady());
            }
        }
    }

    private class InfoPanelAnimator extends ExtendedThread {
        private AnimatorAction currentAction;

        InfoPanelAnimator() {
            startAndWait();
        }

        void act(AnimatorAction action) {
            if (action == null) {
                throw new NullPointerException("action");
            } else {
                currentAction = action;
                if (isThreadLocked()) {
                    unlockThread("start");
                }

            }
        }

        public void run() {
            lockThread("start");

            while (true) {
                while (currentAction == null) {
                    U.sleepFor(100L);
                }

                AnimatorAction action = currentAction;
                switch (action) {
                    case SHOW:
                        show(true);
                        break;
                    case HIDE:
                        hide(true);
                        break;
                    default:
                        throw new RuntimeException("unknown action: " + currentAction);
                }

                if (currentAction == action) {
                    currentAction = null;
                }
            }
        }
    }
}
