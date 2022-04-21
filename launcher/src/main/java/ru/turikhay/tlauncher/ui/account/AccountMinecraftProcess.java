package ru.turikhay.tlauncher.ui.account;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
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
    private final LocalizableButton button;

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
        c.gridy++;
        panel.add(progressBar, c);

        label = new LocalizableLabel(LOC_PREFIX + "waiting");
        c.gridy++;
        panel.add(label, c);

        setCenter(panel);

        button = new LocalizableButton(LOC_PREFIX + "link.open");
        button.addActionListener(e -> {
            if (buttonLink != null) {
                Alert.showLocMessage(LOC_PREFIX + "link.open.alert", buttonLink);
            }
        });
        button.setIconTextGap(SwingUtil.magnify(10));
        button.setIcon(Images.getIcon24("share"));
        setSouth(button);
    }

    void setProcessLabel(String label) {
        this.label.setText(LOC_PREFIX + label);
    }

    private String buttonLink;

    void setButtonLink(String link) {
        this.buttonLink = link;
        button.setVisible(link != null);
    }

    void stopProgress() {
        progressBar.setIndeterminate(false);
        button.setVisible(false);
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        setProcessLabel("starting");
        progressBar.setIndeterminate(true);
        worker.start();
    }

    @Override
    public void multipaneClosed() {
        progressBar.setIndeterminate(false);
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
