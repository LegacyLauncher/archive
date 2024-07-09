package net.legacylauncher.ui.account;

import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableLabel;
import net.legacylauncher.ui.progress.ProgressBar;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.util.SwingUtil;

import java.awt.*;

public class AccountMinecraftProcess extends BorderPanel implements AccountMultipaneCompCloseable {
    final String LOC_PREFIX = LOC_PREFIX_PATH + multipaneName() + ".";

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
