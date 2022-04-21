package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.managers.McleaksManager;
import ru.turikhay.tlauncher.managers.McleaksStatus;
import ru.turikhay.tlauncher.managers.McleaksStatusListener;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.HtmlSubstitutor;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;
import java.util.Objects;

public class AccountMcleaksStart extends BorderPanel implements AccountMultipaneCompCloseable, LocalizableComponent, McleaksStatusListener, Blockable {
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    private final LocalizableButton button;
    private final Blockable buttonBlocker = new Blockable() {
        @Override
        public void block(Object var1) {
            button.setEnabled(false);
        }

        @Override
        public void unblock(Object var1) {
            button.setEnabled(true);
        }
    };
    private final EditorPane content;
    private final ProgressBar progressBar;

    private StartState state;

    private boolean managerUpdated;

    public AccountMcleaksStart(final AccountManagerScene scene) {

        this.content = new EditorPane();
        setCenter(content);

        ExtendedPanel panel = new ExtendedPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = -1;

        button = new LocalizableButton();
        button.addActionListener(e -> {
            switch (state) {
                case DESCRIPTION:
                    setState(StartState.WAITING);
                    break;
                case GET_ALT_TOKEN:
                case WAITING:
                    scene.multipane.showTip("process-account-mcleaks");
                    break;
            }
        });
        button.setIcon(Images.getIcon24("logo-mcleaks"));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        c.gridy++;
        panel.add(button, c);

        progressBar = new ProgressBar();
        progressBar.setPreferredSize(new Dimension(1, SwingUtil.magnify(10)));
        c.gridy++;
        panel.add(progressBar, c);

        setSouth(panel);

        if (McleaksManager.isUnsupported()) {
            setState(StartState.UNSUPPORTED);
        } else {
            setState(StartState.DESCRIPTION);
            McleaksManager.getStatus().addListener(this);
        }
    }

    private void setState(StartState state) {
        if (McleaksManager.isUnsupported() || (mcleaksUpdated && !mcleaksAvailable)) {
            state = StartState.UNSUPPORTED;
        } else if (state == StartState.WAITING && mcleaksUpdated) {
            state = StartState.GET_ALT_TOKEN;
        }
        this.state = Objects.requireNonNull(state, "state");
        button.setText(LOC_PREFIX + state.toString().toLowerCase(java.util.Locale.ROOT) + ".button");
        Blocker.setBlocked("state-required", state.blockButton, buttonBlocker);
        content.setText(TokenReplacingReader.resolveVars(Localizable.get(LOC_PREFIX + (state == StartState.UNSUPPORTED ? StartState.DESCRIPTION : state).toString().toLowerCase(java.util.Locale.ROOT) + ".body"), new HtmlSubstitutor()));
    }

    @Override
    public void setMaximumSize(Dimension maximumSize) {
        super.setMaximumSize(maximumSize);
        content.setPreferredSize(new Dimension(0, 0));
    }

    @Override
    public void multipaneClosed() {

    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "add-account-mcleaks";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        if (!McleaksManager.isUnsupported()) {
            McleaksManager.triggerConnection();
        }
        setState(StartState.DESCRIPTION);
    }

    @Override
    public void updateLocale() {
        setState(state);
    }

    private boolean mcleaksUpdated, mcleaksAvailable;

    @Override
    public void onMcleaksUpdating(McleaksStatus status) {
        this.mcleaksUpdated = false;
        progressBar.setIndeterminate(true);
        progressBar.setValue(0);
    }

    @Override
    public void onMcleaksUpdated(McleaksStatus status) {
        this.mcleaksUpdated = true;
        this.mcleaksAvailable = status.getServerIp() != null;

        progressBar.setIndeterminate(false);
        if (mcleaksAvailable) {
            progressBar.setValue(100);
        } else {
            progressBar.setValue(0);
        }

        if (state == StartState.WAITING) {
            setState(StartState.GET_ALT_TOKEN);
        }
    }

    @Override
    public void block(Object var1) {
        Blocker.block(buttonBlocker, var1);
        Blocker.blockComponents(var1, content, progressBar);
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblock(buttonBlocker, var1);
        Blocker.unblockComponents(var1, content, progressBar);
    }

    private enum StartState {
        UNSUPPORTED(true),
        DESCRIPTION,
        GET_ALT_TOKEN,
        WAITING(true);

        private final boolean blockButton;

        StartState(boolean blockButton) {
            this.blockButton = blockButton;
        }

        StartState() {
            this(false);
        }
    }
}
