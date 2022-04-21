package ru.turikhay.tlauncher.ui.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.user.*;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;

public class AccountElyProcess extends BorderPanel implements AccountMultipaneCompCloseable {
    private static final Logger LOGGER = LogManager.getLogger(AccountElyProcess.class);
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    private final AccountManagerScene scene;

    private final ProgressBar progressBar;
    private final LocalizableLabel label;
    private final LocalizableButton button;

    private final LocalizableMenuItem fallbackMenuItem;

    private Future<ElyAuthCode> authProcess;
    private ElyUser user;

    public AccountElyProcess(final AccountManagerScene scene) {
        this.scene = scene;

        ExtendedPanel panel = new ExtendedPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = -1;

        progressBar = new ProgressBar();
        progressBar.setPreferredSize(SwingUtil.magnify(new Dimension(1, 24)));
        progressBar.setIndeterminate(true);
        c.gridy++;
        panel.add(progressBar, c);

        label = new LocalizableLabel(LOC_PREFIX + "waiting");
        c.gridy++;
        panel.add(label, c);

        setCenter(panel);

        final JPopupMenu menu = new JPopupMenu();
        menu.add(fallbackMenuItem = LocalizableMenuItem.newItem(LOC_PREFIX + "failed.fallback", e -> activateFallbackFlow()));
        menu.add(LocalizableMenuItem.newItem(LOC_PREFIX + "failed.classic", e -> {
            cancelCurrentFlow();
            scene.multipane.clearBreadcrumbs();
            scene.multipane.showTip("add-account-ely_legacy");
        }));

        button = new LocalizableButton();
        button.addActionListener(e -> menu.show(button, 0, button.getHeight()));
        button.setIcon(Images.getIcon24("warning-2"));
        button.setIconTextGap(SwingUtil.magnify(10));
        button.setText(LOC_PREFIX + "failed");
        setSouth(button);
    }

    private void setState(FlowState state) {
        String labelText;
        int progress = -1;
        boolean helpEnabled = true;

        switch (state) {
            case INIT:
                labelText = "init";
                break;
            case WAITING:
                labelText = "waiting";
                break;
            case CANCELLED:
                labelText = "cancelled";
                progress = 0;
                break;
            case INPUT_WAITING:
                labelText = "input_waiting";
                break;
            case EXCHANGE:
                labelText = "exchange";
                helpEnabled = false;
                break;
            case ERROR:
                labelText = "error";
                progress = 0;
                break;
            case COMPLETE:
                labelText = "complete";
                progress = 1;
                helpEnabled = false;
                break;
            default:
                return;
        }

        label.setText(LOC_PREFIX + "flow." + labelText);
        switch (progress) {
            case -1:
                progressBar.setIndeterminate(true);
                break;
            case 0:
                progressBar.setIndeterminate(false);
                progressBar.setValue(0);
                break;
            case 1:
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                break;
        }
        button.setEnabled(helpEnabled);
    }

    private void activatePrimaryFlow() {
        cancelCurrentFlow();
        setState(FlowState.INIT);
        fallbackMenuItem.setEnabled(true); // restore fallback menu item state

        PrimaryElyAuthFlow primaryFlow = AccountManager.getElyAuth().getPrimaryFlow();
        primaryFlow.registerListener(new PrimaryElyAuthFlowListener() {
            @Override
            public void primaryStrategyServerCreated(PrimaryElyAuthFlow strategy, int port) {
                setState(FlowState.INIT);
            }

            @Override
            public void primaryStrategyUrlOpened(PrimaryElyAuthFlow strategy, URL url) {
                setState(FlowState.WAITING);
            }

            @Override
            public ElyFlowWaitTask<Boolean> primaryStrategyCodeParseFailed(PrimaryElyAuthFlow strategy, URI uri) {
                return null;
            }

            @Override
            public void strategyStarted(ElyAuthFlow<?> strategy) {
                setState(FlowState.INIT);
            }

            @Override
            public void strategyErrored(ElyAuthFlow<?> strategy, Exception e) {
                Stats.accountCreation("ely", "primary", "", false);
                setState(FlowState.ERROR);
            }

            @Override
            public void strategyUrlOpened(ElyAuthFlow<?> strategy, URL url) {
                setState(FlowState.WAITING);
            }

            @Override
            public void strategyUrlOpeningFailed(ElyAuthFlow<?> strategy, URL url) {
                Stats.accountCreation("ely", "primary", "url_opening", false);
                setState(FlowState.ERROR);
            }

            @Override
            public void strategyCancelled(ElyAuthFlow<?> strategy) {
                Stats.accountCreation("ely", "primary", "cancelled", false);
                setState(FlowState.CANCELLED);
            }

            @Override
            public void strategyComplete(ElyAuthFlow<?> strategy, ElyAuthCode code) {
                fetchCode("primary", code);
                Stats.accountCreation("ely", "primary", "", true);
            }
        });
        authProcess = AsyncThread.future(primaryFlow);
    }

    private void activateFallbackFlow() {
        cancelCurrentFlow();
        setState(FlowState.INIT);
        fallbackMenuItem.setEnabled(false); // block fallback strategy reinit

        FallbackElyAuthFlow fallbackFlow = AccountManager.getElyAuth().getFallbackFlow();
        fallbackFlow.registerListener(new FallbackElyAuthFlowListener() {
            @Override
            public ElyFlowWaitTask<String> fallbackStrategyRequestedInput(FallbackElyAuthFlow strategy) {
                setState(FlowState.INPUT_WAITING);
                return () -> Alert.showLocInputQuestion("account.manager.multipane.process-account-ely.flow.input_waiting.alert");
            }

            @Override
            public void strategyStarted(ElyAuthFlow<?> strategy) {
                setState(FlowState.INIT);
            }

            @Override
            public void strategyErrored(ElyAuthFlow<?> strategy, Exception e) {
                Stats.accountCreation("ely", "fallback", "", false);
                setState(FlowState.ERROR);
            }

            @Override
            public void strategyUrlOpened(ElyAuthFlow<?> strategy, URL url) {
                setState(FlowState.WAITING);
            }

            @Override
            public void strategyUrlOpeningFailed(ElyAuthFlow<?> strategy, URL url) {
                Stats.accountCreation("ely", "fallback", "url_opening", false);
                setState(FlowState.ERROR);
            }

            @Override
            public void strategyCancelled(ElyAuthFlow<?> strategy) {
                Stats.accountCreation("ely", "fallback", "cancelled", false);
                setState(FlowState.CANCELLED);
            }

            @Override
            public void strategyComplete(ElyAuthFlow<?> strategy, ElyAuthCode code) {
                Stats.accountCreation("ely", "fallback", "", true);
                fetchCode("fallback", code);
            }
        });
        authProcess = AsyncThread.future(fallbackFlow);
    }

    private void fetchCode(String strategy, ElyAuthCode code) {
        setState(FlowState.EXCHANGE);

        TLauncher.getInstance().getFrame().requestFocus();
        TLauncher.getInstance().getFrame().toFront();

        ElyUser user;
        try {
            user = code.getUser();
        } catch (Exception e) {
            LOGGER.error("Could not get user", e);
            setState(FlowState.ERROR);
            return;
        }

        setState(FlowState.COMPLETE);

        StandardAccountPane.removeAccountIfFound(user.getUsername(), Account.AccountType.ELY);
        StandardAccountPane.removeAccountIfFound(user.getUsername(), Account.AccountType.ELY_LEGACY);

        TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().add(user);
        scene.multipane.showTip("success-add");
        scene.list.select(new Account<>(user));
    }

    private void cancelCurrentFlow() {
        if (authProcess != null) {
            authProcess.cancel(true);
        }
    }

    @Override
    public void multipaneClosed() {
        cancelCurrentFlow();
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "process-account-ely";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        activatePrimaryFlow();
    }

    public enum FlowState {
        INIT, WAITING, CANCELLED, INPUT_WAITING, EXCHANGE, ERROR, COMPLETE
    }
}
