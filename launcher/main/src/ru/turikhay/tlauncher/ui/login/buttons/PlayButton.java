package ru.turikhay.tlauncher.ui.login.buttons;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.event.*;

public class PlayButton extends LocalizableButton implements Blockable, LoginForm.LoginStateListener {
    private static final long serialVersionUID = 6944074583143406549L;
    private PlayButton.PlayButtonState state;
    private final LoginForm loginForm;

    private int mouseX, mouseY;
    private final JPopupMenu wrongButtonMenu = new JPopupMenu();

    {
        LocalizableMenuItem wrongButtonItem = new LocalizableMenuItem("loginform.wrongbutton");
        wrongButtonItem.setEnabled(false);
        wrongButtonItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                wrongButtonMenu.setVisible(false);
            }
        });
        wrongButtonMenu.add(wrongButtonItem);
    }

    PlayButton(LoginForm lf) {
        loginForm = lf;
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                switch (state) {
                    case CANCEL:
                        loginForm.stopLauncher();
                        break;
                    default:
                        loginForm.startLauncher();
                }

            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1)
                    wrongButtonMenu.show(PlayButton.this, mouseX, mouseY);
            }
        });
        setFont(getFont().deriveFont(1).deriveFont(TLauncherFrame.getFontSize() * 1.5f));
        setState(PlayButton.PlayButtonState.PLAY);
    }

    public PlayButton.PlayButtonState getState() {
        return state;
    }

    public void setState(PlayButton.PlayButtonState state) {
        if (state == null) {
            throw new NullPointerException();
        } else {
            this.state = state;
            setText(state.getPath());
            if (state == PlayButton.PlayButtonState.CANCEL) {
                setEnabled(true);
            }

        }
    }

    public void updateState() {
        VersionSyncInfo vs = loginForm.versions.getVersion();
        if (vs != null) {
            boolean installed = vs.isInstalled();
            boolean force = loginForm.checkbox.forceupdate.getState();
            if (!installed) {
                setState(PlayButton.PlayButtonState.INSTALL);
            } else {
                setState(force ? PlayButton.PlayButtonState.REINSTALL : PlayButton.PlayButtonState.PLAY);
            }

        }
    }

    public void loginStateChanged(LoginForm.LoginState state) {
        if (state == LoginForm.LoginState.LAUNCHING) {
            setState(PlayButton.PlayButtonState.CANCEL);
        } else {
            updateState();
            setEnabled(!Blocker.isBlocked(this));
        }

    }

    public void block(Object reason) {
        if (state != PlayButton.PlayButtonState.CANCEL) {
            setEnabled(false);
        }

    }

    public void unblock(Object reason) {
        setEnabled(true);
    }

    public enum PlayButtonState {
        REINSTALL("loginform.enter.reinstall"),
        INSTALL("loginform.enter.install"),
        PLAY("loginform.enter"),
        CANCEL("loginform.enter.cancel");

        private final String path;

        PlayButtonState(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }
    }
}
