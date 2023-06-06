package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.managers.PromotedStoreManager;
import ru.turikhay.tlauncher.stats.Stats;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.frames.RequireMinecraftAccountFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.user.MinecraftUser;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.UserSet;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Optional;

public class AccountAdd extends BorderPanel implements AccountMultipaneCompCloseable, Blockable, LocalizableComponent {

    private final AccountManagerScene scene;

    private final ExtendedPanel grid;
    private final GridBagConstraints c;

    private final LocalizableButton minecraft, ely, free, idontknow, promotedStore;

    private final boolean requireMinecraftAccount;

    private final String LOC_PREFIX;

    public AccountAdd(final AccountManagerScene scene) {
        this.scene = scene;

        requireMinecraftAccount = TLauncher.getInstance().getCapability("require_minecraft_account", Boolean.class).orElse(false);

        grid = new ExtendedPanel();
        //grid.setBorder(BorderFactory.createLineBorder(Color.red));
        grid.setAlignmentX(0);
        grid.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridy = -1;

        LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";
        String ACCOUNT_TYPE_PREFIX = LOC_PREFIX + "type.";
        ely = addRow("logo-ely", ACCOUNT_TYPE_PREFIX + "ely", createListenerFor("add-account-ely", false));
        minecraft = addRow("logo-microsoft", ACCOUNT_TYPE_PREFIX + "minecraft", createListenerFor("process-account-minecraft", true));

        promotedStore = addRow("gift-1", LOC_PREFIX + "buy-minecraft", e -> {
            String url = "https://minecraft.net";
            boolean promotedStore = false;
            Optional<String> urlOpt = psm().getInfoNow().map(PromotedStoreManager.Info::getUrl);
            if (urlOpt.isPresent()) {
                url = urlOpt.get();
                promotedStore = true;
            }
            Stats.showInterestInBuying(promotedStore);
            OS.openLink(url);
        });

        free = addRow("user-circle-o", ACCOUNT_TYPE_PREFIX + "free", createListenerFor("add-account-plain", false));
        idontknow = addRow("info-circle", LOC_PREFIX + "hint", e -> Blocker.toggle(AccountAdd.this, "idontknow"));

        c.gridy++;
        c.gridx = 1;
        c.fill = GridBagConstraints.VERTICAL;
        c.insets = new Insets(0, 0, 0, 0);
        c.weighty = 1.0;
        grid.add(new ExtendedPanel(), c);

        setCenter(grid);

        updateLocale();
    }

    private ActionListener createListenerFor(String name, boolean isMinecraftAccount) {
        return e -> {
            if (requireMinecraftAccount && !isMinecraftAccount) {
                UserSet userSet = TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet();
                boolean hasMinecraftAccount = userSet.findByType(MinecraftUser.TYPE).isPresent();
                boolean hasMojangAccount = userSet.findByType(MojangUser.TYPE).isPresent();
                if (!hasMinecraftAccount && !hasMojangAccount) {
                    new RequireMinecraftAccountFrame().showAtCenter();
                    return;
                }
            }
            AccountAdd.this.scene.multipane.showTip(name);
        };
    }

    private LocalizableButton addRow(String image, String label, ActionListener action) {
        c.gridy++;

        LocalizableButton button = new LocalizableButton(label);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(SwingUtil.magnify(16));
        button.addActionListener(action);
        //button.setPreferredSize(SwingUtil.magnify(new Dimension(48, 48)));
        button.setIcon(Images.getIcon32(image));
        c.gridx = 0;
        //c.weightx = 0;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(SwingUtil.magnify(5), 0, 0, 0);
        //c.fill = GridBagConstraints.NONE;
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.add(button, c);

        /*c.gridx = 1;
        c.weightx = 1.0;
        c.insets = new Insets(0, SwingUtil.magnify(10), 0, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        grid.add(new LocalizableLabel(label), c);*/

        return button;
    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "add-account";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        Blocker.unblock(AccountAdd.this, "idontknow");
    }

    @Override
    public void multipaneClosed() {
        Blocker.unblock(AccountAdd.this, "idontknow");
    }

    @Override
    public void block(Object var1) {
        if (!"idontknow".equals(var1)) {
            Blocker.blockComponents(var1, free, ely, idontknow, minecraft);
        }
        if (requireMinecraftAccount) {
            Blocker.blockComponents(var1, free, ely);
        } else {
            Blocker.blockComponents(var1, ely, minecraft);
        }
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblockComponents(var1, free, ely, idontknow, minecraft);
    }

    @Override
    public void updateLocale() {
        if (!scene.getMainPane().getRootFrame().getLauncher().getLang().getLocale().equals(LangConfiguration.ru_RU)) {
            promotedStore.setEnabled(true);
            return;
        }
        promotedStore.setEnabled(false);
        psm().requestOrGetInfo().whenComplete((info, t) -> {
            if (info != null) {
                SwingUtil.later(() -> promotedStore.setText(LOC_PREFIX + "buy-minecraft-price", info.getPrice()));
            }
            SwingUtil.later(() -> promotedStore.setEnabled(true));
        });
    }

    private PromotedStoreManager psm() {
        return scene.getMainPane().getRootFrame().getLauncher().getPromotedStoreManager();
    }
}
