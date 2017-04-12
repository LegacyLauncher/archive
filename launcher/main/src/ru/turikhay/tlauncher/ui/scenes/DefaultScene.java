package ru.turikhay.tlauncher.ui.scenes;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.SideNotifier;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.notice.MainNoticePanel;
import ru.turikhay.tlauncher.ui.notice.NoticePanel;
import ru.turikhay.tlauncher.ui.notice.NoticeSidePanel;
import ru.turikhay.tlauncher.ui.settings.SettingsPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.Direction;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;
import ru.turikhay.util.async.LoopedThread;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultScene extends PseudoScene {
    public static final Dimension LOGIN_SIZE = new Dimension(250, 240);
    public static final Dimension SETTINGS_SIZE = new Dimension(500, 520);
    public static final int EDGE_INSETS = 10;
    public static final int INSETS = 15;
    public final SideNotifier notifier;
    public final LoginForm loginForm;
    public final SettingsPanel settingsForm;
    public final NoticeSidePanel noticeSidePanel;
    private DefaultScene.SidePanel sidePanel;
    private ExtendedPanel sidePanelComp;
    private Direction lfDirection;
    //public final NoticePanel infoPanel;
    public final MainNoticePanel noticePanel;

    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$ru$turikhay$util$Direction;
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$scenes$DefaultScene$SidePanel;

    public DefaultScene(MainPane main) {
        super(main);
        notifier = main.notifier;
        settingsForm = new SettingsPanel(this);
        settingsForm.setSize(SwingUtil.magnify(SETTINGS_SIZE));
        settingsForm.setVisible(false);
        add(settingsForm);
        loginForm = new LoginForm(this);
        loginForm.setSize(SwingUtil.magnify(LOGIN_SIZE));
        add(loginForm);
        noticePanel = new MainNoticePanel(this);
        add(noticePanel);
        noticeSidePanel = new NoticeSidePanel(this);
        noticeSidePanel.setVisible(false);
        add(noticeSidePanel);
        //infoPanel = new NoticePanel(this);
        //add(infoPanel);
        updateDirection();

        if(isNoticeSidePanelEnabled() && getMainPane().getRootFrame().getNotices().getForCurrentLocale() != null) {
            setSidePanel(null);
        } else {
            //noticePanel.panelShown();
        }
    }

    public void setShown(boolean shown, boolean animate) {
        super.setShown(shown, animate);
        if(shown) {
            if(getMainPane().getRootFrame().getNotices().isHidden(noticePanel.getNotice())) {
                getMainPane().getRootFrame().getNotices().selectRandom();
            }
            noticePanel.redraw();
        }
    }

    public void onResize() {
        if (parent != null) {
            setBounds(0, 0, parent.getWidth(), parent.getHeight());
            updateCoords();
        }
    }

    private static final int MARGIN = 10, SPACE_BETWEEN = 15;

    private void updateCoords() {
        final int margin = SwingUtil.magnify(MARGIN), spaceBetween = SwingUtil.magnify(SPACE_BETWEEN);

        int w = getWidth();
        int h = getHeight();
        int hw = w / 2;
        int hh = h / 2;
        int lf_w = loginForm.getWidth();
        int lf_h = loginForm.getHeight();
        int lf_x;
        int lf_y;
        int n_x;
        if (sidePanel == null) {
            switch (lfDirection) {
                case TOP_LEFT:
                case CENTER_LEFT:
                case BOTTOM_LEFT:
                    lf_x = 10;
                    break;
                case TOP:
                case CENTER:
                case BOTTOM:
                    lf_x = hw - lf_w / 2;
                    break;
                case TOP_RIGHT:
                case CENTER_RIGHT:
                case BOTTOM_RIGHT:
                    lf_x = w - lf_w - margin;
                    break;
                default:
                    throw new RuntimeException("unknown direction:" + lfDirection);
            }

            switch (lfDirection) {
                case TOP_LEFT:
                case TOP:
                case TOP_RIGHT:
                    lf_y = margin;
                    break;
                case CENTER_LEFT:
                case CENTER:
                case CENTER_RIGHT:
                    lf_y = hh - lf_h / 2;
                    break;
                case BOTTOM_LEFT:
                case BOTTOM:
                case BOTTOM_RIGHT:
                    lf_y = h - margin - lf_h;
                    break;
                default:
                    throw new RuntimeException("unknown direction:" + lfDirection);
            }
        } else {
            n_x = sidePanelComp.getWidth();
            int n_y = sidePanelComp.getHeight();
            int bw = lf_w + n_x + spaceBetween;
            int hbw = bw / 2;
            int sp_x;
            int sp_y;
            if (w > bw) {
                switch (lfDirection) {
                    case TOP_LEFT:
                    case CENTER_LEFT:
                    case BOTTOM_LEFT:
                        lf_x = margin;
                        sp_x = lf_x + lf_w + spaceBetween;
                        break;
                    case TOP:
                    case CENTER:
                    case BOTTOM:
                        lf_x = hw - hbw;
                        sp_x = lf_x + lf_w + spaceBetween;
                        break;
                    case TOP_RIGHT:
                    case CENTER_RIGHT:
                    case BOTTOM_RIGHT:
                        lf_x = w - margin - lf_w;
                        sp_x = lf_x - spaceBetween - n_x;
                        break;
                    default:
                        throw new RuntimeException("unknown direction:" + lfDirection);
                }

                switch (lfDirection) {
                    case TOP_LEFT:
                    case TOP:
                    case TOP_RIGHT:
                        sp_y = margin;
                        lf_y = margin;
                        break;
                    case CENTER_LEFT:
                    case CENTER:
                    case CENTER_RIGHT:
                        lf_y = hh - lf_h / 2;
                        sp_y = hh - n_y / 2;
                        break;
                    case BOTTOM_LEFT:
                    case BOTTOM:
                    case BOTTOM_RIGHT:
                        lf_y = h - margin - lf_h;
                        sp_y = h - margin - n_y;
                        break;
                    default:
                        throw new RuntimeException("unknown direction:" + lfDirection);
                }
            } else {
                lf_x = w * 2;
                lf_y = 0;
                sp_x = hw - n_x / 2;
                sp_y = hh - n_y / 2;
            }
            sidePanelComp.setLocation(sp_x, sp_y);
        }

        byte n_y1 = 10;
        switch (lfDirection) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                n_x = getMainPane().getWidth() - margin - notifier.getWidth();
                break;
            default:
                n_x = margin;
        }

        notifier.setLocation(n_x, n_y1);
        loginForm.setLocation(lf_x, lf_y);
        noticePanel.onResize();
    }

    public DefaultScene.SidePanel getSidePanel() {
        return sidePanel;
    }

    public boolean isNoticeSidePanelEnabled() {
        return getMainPane().getRootFrame().getConfiguration().getBoolean("gui.notices.enabled");
    }

    public void setNoticeSidePanelEnabled(boolean e) {
        getMainPane().getRootFrame().getConfiguration().set("gui.notices.enabled", e);
        setSidePanel(null);
    }

    public void setSidePanel(DefaultScene.SidePanel side) {
        if(side == null && isNoticeSidePanelEnabled() && getMainPane().getRootFrame().getNotices().getForCurrentLocale() != null) {
            side = SidePanel.NOTICES;
        }
        if (sidePanel != side) {
            boolean noSidePanel = side == null;
            if (sidePanelComp != null) {
                sidePanelComp.setVisible(false);
            }

            sidePanel = side;
            sidePanelComp = noSidePanel ? null : getSidePanelComp(side);
            if (!noSidePanel) {
                sidePanelComp.setVisible(true);
            }

            noticePanel.setVisible(noSidePanel);
            /*if(noSidePanel) {
                noticePanel.panelShown();
            }*/
            //noticePanel.setVisible(noSidePanel);
            updateCoords();
        }
    }

    public void toggleSidePanel(DefaultScene.SidePanel side) {
        if (sidePanel == side) {
            side = null;
        }

        setSidePanel(side);
    }

    public ExtendedPanel getSidePanelComp(DefaultScene.SidePanel side) {
        if (side == null) {
            throw new NullPointerException("side");
        } else {
            switch (side) {
                case SETTINGS:
                    return settingsForm;
                case NOTICES:
                    return noticeSidePanel;
                default:
                    throw new RuntimeException("unknown side:" + side);
            }
        }
    }

    public Direction getLoginFormDirection() {
        return lfDirection;
    }

    public void updateDirection() {
        loadDirection();
        updateCoords();
    }

    private void loadDirection() {
        Configuration config = getMainPane().getRootFrame().getConfiguration();
        Direction loginFormDirection = config.getDirection("gui.direction.loginform");
        if (loginFormDirection == null) {
            loginFormDirection = Direction.CENTER;
        }

        lfDirection = loginFormDirection;
    }

    public enum SidePanel {
        SETTINGS, NOTICES;

        public final boolean requiresShow;

        SidePanel(boolean requiresShow) {
            this.requiresShow = requiresShow;
        }

        SidePanel() {
            this(false);
        }
    }
}
