package ru.turikhay.tlauncher.ui.login.buttons;

import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class ButtonPanel extends BlockablePanel {
    private static final long serialVersionUID = -2155145867054136409L;
    public final PlayButton play;
    private final JPanel manageButtonsPanel;
    public final SupportButton support;
    public final FolderButton folder;
    public final RefreshButton refresh;
    public final SettingsButton settings;
    public final CancelAutoLoginButton cancel;
    private ButtonPanel.ButtonPanelState state;

    public ButtonPanel(LoginForm lf) {
        boolean letUserDoWhatHeWants = !lf.global.getBoolean("gui.settings.blocked");
        BorderLayout lm = new BorderLayout(SwingUtil.magnify(1), SwingUtil.magnify(2));
        setLayout(lm);
        setOpaque(false);
        play = new PlayButton(lf);
        add("Center", play);
        cancel = new CancelAutoLoginButton(lf);
        manageButtonsPanel = new JPanel(new GridLayout(0, letUserDoWhatHeWants ? 4 : 2));
        manageButtonsPanel.setOpaque(false);
        support = new SupportButton(lf);
        manageButtonsPanel.add(support);
        folder = new FolderButton(lf);
        if (letUserDoWhatHeWants) {
            manageButtonsPanel.add(folder);
        }

        refresh = new RefreshButton(lf);
        manageButtonsPanel.add(refresh);
        settings = new SettingsButton(lf);
        if (letUserDoWhatHeWants) {
            manageButtonsPanel.add(settings);
        }

        setState(lf.autologin.isEnabled() ? ButtonPanel.ButtonPanelState.AUTOLOGIN_CANCEL : ButtonPanel.ButtonPanelState.MANAGE_BUTTONS);
    }

    public ButtonPanel.ButtonPanelState getState() {
        return state;
    }

    public void setState(ButtonPanel.ButtonPanelState state) {
        if (state == null) {
            throw new NullPointerException();
        } else {
            this.state = state;
            switch (state) {
                case AUTOLOGIN_CANCEL:
                    remove(manageButtonsPanel);
                    add("South", cancel);
                    break;
                case MANAGE_BUTTONS:
                    remove(cancel);
                    add("South", manageButtonsPanel);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown state: " + state);
            }

            validate();
        }
    }

    public enum ButtonPanelState {
        AUTOLOGIN_CANCEL,
        MANAGE_BUTTONS
    }
}
