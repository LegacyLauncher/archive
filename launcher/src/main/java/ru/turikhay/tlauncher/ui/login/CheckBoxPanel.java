package ru.turikhay.tlauncher.ui.login;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.BlockablePanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.swing.CheckBoxListener;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class CheckBoxPanel extends BlockablePanel implements LoginForm.LoginProcessListener {
    private static final long serialVersionUID = 768489049585749260L;
    private static final List<String> phrases = Collections.singletonList("This ain't easter egg.");
    public final LocalizableCheckbox autologin;
    public final LocalizableCheckbox forceupdate;
    private boolean state;
    private final LoginForm loginForm;

    CheckBoxPanel(LoginForm lf) {
        BoxLayout lm = new BoxLayout(this, BoxLayout.PAGE_AXIS);
        setLayout(lm);
        setOpaque(false);
        setAlignmentX(0.5F);
        loginForm = lf;
        autologin = new LocalizableCheckbox("loginform.checkbox.autologin", lf.global.getBoolean("login.auto"));
        autologin.addItemListener(new CheckBoxListener() {
            public void itemStateChanged(boolean newstate) {
                loginForm.autologin.setEnabled(newstate);
                if (newstate) {
                    AsyncThread.execute(() -> Alert.showLocMessage("loginform.checkbox.autologin.tip"));
                }

            }
        });
        forceupdate = new LocalizableCheckbox("loginform.checkbox.forceupdate");
        forceupdate.addItemListener(new CheckBoxListener() {
            private byte clicks = 0;

            public void itemStateChanged(boolean newstate) {
                if (++clicks == 10) {
                    forceupdate.setText(U.getRandom(CheckBoxPanel.phrases));
                    clicks = 0;
                }

                state = newstate;
                loginForm.buttons.play.updateState();
            }
        });
        add(autologin);
        add(Box.createHorizontalGlue());
        add(forceupdate);
    }

    public void logginingIn() throws LoginException {
        VersionSyncInfo syncInfo = loginForm.versions.getVersion();
        if (syncInfo != null) {
            boolean supporting = syncInfo.hasRemote();
            boolean installed = syncInfo.isInstalled();
            if (state) {
                if (!supporting) {
                    Alert.showLocError("forceupdate.local");
                    throw new LoginException("Cannot update local version!");
                }

                if (installed && !Alert.showLocQuestion("forceupdate.question")) {
                    throw new LoginException("User has cancelled force updating.");
                }
            }

        }
    }

    public void loginFailed() {
    }

    public void loginSucceed() {
    }
}
