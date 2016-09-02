package ru.turikhay.tlauncher.ui.swing.editor;

import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;

import javax.swing.*;
import javax.swing.text.Element;
import java.awt.*;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class ServerHyperlinkProcessor extends HyperlinkProcessor implements Blockable {
    private final JPopupMenu popup = new JPopupMenu();
    private boolean blocked;

    @Override
    public JPopupMenu process(String link) {
        if (link != null && link.startsWith("server:")) {
            if (!blocked) {
                try {
                    return openServer(link);
                } catch (Exception var3) {
                    Alert.showLocError("ad.server.error", new RuntimeException("\"" + link + "\"", var3));
                }
            }
            return null;
        } else {
            return HyperlinkProcessor.defaultProcessor.process(link);
        }
    }

    private JPopupMenu openServer(String link) {
        final ServerList.Server server = ServerList.Server.parseFromString(link.substring("server:".length()));
        popup.removeAll();
        LocalizableMenuItem openWith = new LocalizableMenuItem("ad.server.openwith", server.getName());
        openWith.setEnabled(false);
        popup.add(openWith);
        ArrayList list = new ArrayList();
        Iterator copyAddress = TLauncher.getInstance().getVersionManager().getVersions().iterator();

        while (copyAddress.hasNext()) {
            VersionSyncInfo currentVersion = (VersionSyncInfo) copyAddress.next();
            int requiredAccount = server.getVersions().indexOf(currentVersion.getID());
            if (requiredAccount != -1) {
                list.add(currentVersion);
            }
        }

        for (int var11 = list.size() - 1; var11 > -1; --var11) {
            final VersionSyncInfo var13 = (VersionSyncInfo) list.get(var11);
            JMenuItem var15 = new JMenuItem(var13.getID());
            var15.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    open(var13, server);
                }
            });
            popup.add(var15);
        }

        LocalizableMenuItem var12 = new LocalizableMenuItem("ad.server.openwith.current", server.getName());
        var12.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open(null, server);
            }
        });
        popup.add(var12);
        popup.addSeparator();
        LocalizableMenuItem var14 = new LocalizableMenuItem("ad.server.copy");
        var14.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    StringSelection rE = new StringSelection(server.getAddress());
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(rE, null);
                } catch (RuntimeException var3) {
                    Alert.showMessage("IP:", null, server.getAddress());
                }

            }
        });
        popup.add(var14);
        if (!server.getAllowedAccountTypeList().isEmpty()) {
            LocalizableMenuItem var16;
            label46:
            {
                StringBuilder types;
                Iterator var10;
                switch (server.getAllowedAccountTypeList().size()) {
                    case 1:
                        var16 = new LocalizableMenuItem("ad.server.required-account", Localizable.get("account.type." + server.getAllowedAccountTypeList().get(0)));
                        break label46;
                    default:
                        types = new StringBuilder();
                        var10 = server.getAllowedAccountTypeList().iterator();
                }

                while (var10.hasNext()) {
                    Account.AccountType type = (Account.AccountType) var10.next();
                    types.append(Localizable.get("account.type." + type)).append(", ");
                }

                var16 = new LocalizableMenuItem("ad.server.required-account.multiple", types.substring(0, types.length() - ", ".length()));
            }

            var16.setEnabled(false);
            popup.add(var16);
        }

        return popup;
    }

    public abstract void open(VersionSyncInfo var1, ServerList.Server var2);

    public void block(Object reason) {
        blocked = true;
    }

    public void unblock(Object reason) {
        blocked = false;
    }
}