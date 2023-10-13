package net.legacylauncher.ui.account;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.LangConfiguration;
import net.legacylauncher.managers.PromotedStoreManager;
import net.legacylauncher.stats.Stats;
import net.legacylauncher.ui.block.Blockable;
import net.legacylauncher.ui.block.Blocker;
import net.legacylauncher.ui.frames.RequireMinecraftAccountFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.loc.LocalizableButton;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.scenes.AccountManagerScene;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.swing.extended.ExtendedPanel;
import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.MojangUser;
import net.legacylauncher.user.UserSet;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;

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

        requireMinecraftAccount = LegacyLauncher.getInstance().getMetadata("require_minecraft_account", Boolean.class).orElse(false);

        grid = new ExtendedPanel();
        //grid.setBorder(BorderFactory.createLineBorder(Color.red));
        grid.setAlignmentX(0);
        grid.setLayout(new GridBagLayout());

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.gridy = -1;

        LOC_PREFIX = LOC_PREFIX_PATH + multipaneName() + ".";
        String ACCOUNT_TYPE_PREFIX = LOC_PREFIX + "type.";
        ely = addRow("logo-ely", ACCOUNT_TYPE_PREFIX + "ely", createListenerFor("add-account-ely", false));
        minecraft = addRow("logo-microsoft", ACCOUNT_TYPE_PREFIX + "minecraft", createListenerFor("process-account-minecraft", true));

        promotedStore = addRow("gift-1", LOC_PREFIX + "buy-minecraft", e -> {
            PromotedStoreManager.Info minecraftStore = new PromotedStoreManager.Info("minecraft", "https://minecraft.net");
            PromotedStoreManager.Info promotedStore = psm().getInfoNow().orElse(minecraftStore);
            Stats.showInterestInBuying(promotedStore.getId());
            OS.openLink(promotedStore.getUrl());
            if (promotedStore != minecraftStore && scene.getMainPane().getRootFrame().getLauncher().getSettings().getInteger("feedback.promotedStore", -1) < 0) {
                scene.getMainPane().getRootFrame().getLauncher().getSettings().set("feedback.promotedStore", 0);
            }
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
                UserSet userSet = LegacyLauncher.getInstance().getProfileManager().getAccountManager().getUserSet();
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
