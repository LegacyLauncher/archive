package ru.turikhay.tlauncher.ui.notice;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ReleaseType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.PromotedServer;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class ServerNoticeAction extends NoticeAction {
    private static final int MAX_FAMILY_MEMBERS = 2;

    private final PromotedServer server;
    private final int serverId;
    private final ImageIcon installedVersion;

    ServerNoticeAction(PromotedServer server, int serverId) {
        super("server");
        this.server = U.requireNotNull(server, "server");
        this.serverId = serverId;

        installedVersion = Images.getIcon16("check-square");
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("server", server);
    }

    @Override
    List<? extends JMenuItem> getMenuItemList() {
        List<JMenuItem> list = new ArrayList<JMenuItem>();

        if (server.hasAccountTypeRestriction()) {
            Set<Account.AccountType> accountTypes = new LinkedHashSet<Account.AccountType>(server.getAccountTypes());

            boolean supportsFree = accountTypes.remove(Account.AccountType.PLAIN);
            String path = L10N_PREFIX + "account." + (supportsFree ? "supported" : "required");

            for (Account.AccountType accountType : accountTypes) {
                LocalizableMenuItem accountItem = new LocalizableMenuItem(path, Localizable.get("account.type." + accountType.name().toLowerCase(java.util.Locale.ROOT)));
                accountItem.setEnabled(false);
                if (accountType.getIcon() != null) {
                    accountItem.setDisabledIcon(Images.getIcon16(accountType.getIcon()));
                }
                list.add(accountItem);
            }
        }

        LocalizableMenuItem selectItem = new LocalizableMenuItem(L10N_PREFIX + "choose-version", server.getName());
        selectItem.setDisabledIcon(Images.getIcon16("play-circle-o"));
        selectItem.setEnabled(false);
        list.add(selectItem);

        LocalizableMenuItem currentItem = new LocalizableMenuItem(L10N_PREFIX + "choose-version.current");
        currentItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VersionSyncInfo vs = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.versions.getVersion();
                startVersion(vs);
            }
        });
        list.add(currentItem);

        if (server.getFamily() != null) {
            List<VersionSyncInfo> syncInfoList = getFamilyMembers(
                    server.getFamily(), TLauncher.getInstance().getVersionManager().getVersions(false)
            );
            for (final VersionSyncInfo syncInfo : syncInfoList) {
                JMenuItem item = new JMenuItem(syncInfo.getID());
                if (syncInfo.isInstalled() && syncInfo.isUpToDate()) {
                    installedVersion.setup(item);
                }
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startVersion(syncInfo);
                    }
                });
                list.add(item);
            }
        }

        return list;
    }

    void startVersion(VersionSyncInfo syncInfo) {
        if (syncInfo == null) {
            return;
        }

        LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;

        prepareStart:
        if (server.hasAccountTypeRestriction()) {
            Account account = lf.accounts.getAccount();
            if (account == null) {
                break prepareStart; // show error message
            }

            if (server.getAccountTypes().contains(account.getType())) {
                break prepareStart; // ok
            }

            Set<Account.AccountType> allowedTypes = server.getAccountTypes();
            Collection<Account> accounts = TLauncher.getInstance().getProfileManager().getAuthDatabase().getAccounts();
            String body;
            boolean haveOne;

            if (allowedTypes.size() == 1) {
                Account.AccountType allowedType = allowedTypes.iterator().next();
                haveOne = haveAccountWithType(accounts, allowedType);

                body = Localizable.get(
                        "notice.action.server.account.required.error.single." +
                                ((haveOne) ? "have" : "register"),
                        Localizable.get("account.type." + allowedType.toString())
                );
            } else {
                haveOne = false;
                StringBuilder b = new StringBuilder(Localizable.get("notice.action.server.account.required.error.multiple")).append("\n\n");
                for (Account.AccountType allowedType : allowedTypes) {
                    b.append("– ").append(Localizable.get("account.type." + allowedType.toString())).append('\n');
                    haveOne |= haveAccountWithType(accounts, allowedType);
                }
                b.append('\n').append(Localizable.get("notice.action.server.account.required.error.multiple." + (haveOne ? "have" : "register")));
                body = b.toString();
            }

            if (!haveOne) {
                TLauncher.getInstance().getFrame().mp.openAccountEditor();
            }

            Alert.showError(Localizable.get("notice.action.server.account.required.error.title"), body, null);
            return;
        }

        TLauncher.getInstance().getFrame().mp.openDefaultScene();
        lf.versions.setSelectedValue(syncInfo);
        lf.startLauncher(server, serverId);
    }

    static List<VersionSyncInfo> getFamilyMembers(List<String> familyList, List<VersionSyncInfo> syncInfoList) {
        List<VersionSyncInfo> fullVersionList = new ArrayList<VersionSyncInfo>();

        for(String family : familyList) {
            List<VersionSyncInfo> curVersionList = new ArrayList<VersionSyncInfo>();
            for (VersionSyncInfo syncInfo : syncInfoList) {
                String currentFamily;

                if(syncInfo.getAvailableVersion().getReleaseType() == ReleaseType.SNAPSHOT) {
                    continue;
                }
                if (family.equals(syncInfo.getID())) {
                    currentFamily = family;
                } else if (syncInfo.getLocalCompleteVersion() != null) {
                    currentFamily = syncInfo.getLocalCompleteVersion().getFamily();
                } else {
                    currentFamily = CompleteVersion.getFamilyOf(syncInfo.getID());
                }

                if (family.equals(currentFamily)) {
                    curVersionList.add(syncInfo);
                }
            }
            fullVersionList.addAll(filterLatestVersions(curVersionList));
        }

        return fullVersionList;
    }

    static List<VersionSyncInfo> filterLatestVersions(List<VersionSyncInfo> syncInfoList) {
        Collections.sort(syncInfoList, new Comparator<VersionSyncInfo>() {
            public int compare(VersionSyncInfo a, VersionSyncInfo b) {
                Date aDate = a.getLatestVersion().getReleaseTime();
                Date bDate = b.getLatestVersion().getReleaseTime();
                return aDate != null && bDate != null ? bDate.compareTo(aDate) : 1;
            }
        });
        return syncInfoList.size() > MAX_FAMILY_MEMBERS ? syncInfoList.subList(0, MAX_FAMILY_MEMBERS) : syncInfoList;
    }

    private static boolean haveAccountWithType(Collection<Account> accounts, Account.AccountType type) {
        for (Account acc : accounts) {
            if (acc.getType() == type) {
                return true;
            }
        }
        return false;
    }
}
