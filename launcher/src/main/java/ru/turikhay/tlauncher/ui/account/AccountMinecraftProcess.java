package ru.turikhay.tlauncher.ui.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

public class AccountMinecraftProcess extends BorderPanel implements AccountMultipaneCompCloseable {
    private static final Logger LOGGER = LogManager.getLogger(AccountMinecraftProcess.class);
    final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    final AccountManagerScene scene;
    private final AccountMinecraftProcessWorker worker;

    private final ProgressBar progressBar;
    private final LocalizableLabel label;

    public AccountMinecraftProcess(final AccountManagerScene scene) {
        this.scene = scene;

        this.worker = new AccountMinecraftProcessWorker(this);
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
    }

    void setProcessLabel(String label) {
        this.label.setText(LOC_PREFIX + label);
    }

    void stopProgress() {
        progressBar.setIndeterminate(false);
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        setProcessLabel("starting");
        worker.start();
    }

    @Override
    public void multipaneClosed() {
        worker.cancel();
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "process-account-minecraft";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }
}
