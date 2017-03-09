package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;

public class AccountCellRenderer implements ListCellRenderer<Account> {
    public static final Account EMPTY = Account.randomAccount();
    public static final Account MANAGE = Account.randomAccount();
    private static final ImageIcon MANAGE_ICON = Images.getIcon("gear.png", SwingUtil.magnify(16));
    private static final ImageIcon MOJANG_USER_ICON = Images.getIcon("star.png", SwingUtil.magnify(16));
    private static final ImageIcon ELY_USER_ICON = Images.getIcon("ely.png", SwingUtil.magnify(16));
    private final DefaultListCellRenderer defaultRenderer;
    private AccountCellRenderer.AccountCellType type;

    public AccountCellRenderer(AccountCellRenderer.AccountCellType type) {
        if (type == null) {
            throw new NullPointerException("CellType cannot be NULL!");
        } else {
            defaultRenderer = new DefaultListCellRenderer();
            this.type = type;
        }
    }

    public AccountCellRenderer() {
        this(AccountCellRenderer.AccountCellType.PREVIEW);
    }

    public AccountCellRenderer.AccountCellType getType() {
        return type;
    }

    public void setType(AccountCellRenderer.AccountCellType type) {
        if (type == null) {
            throw new NullPointerException("CellType cannot be NULL!");
        } else {
            this.type = type;
        }
    }

    public Component getListCellRendererComponent(JList<? extends Account> list, Account value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        renderer.setAlignmentY(0.5F);
        if (value != null && !value.equals(EMPTY)) {
            if (value.equals(MANAGE)) {
                renderer.setText(Localizable.get("account.manage"));
                renderer.setIcon(MANAGE_ICON);
            } else {
                Object icon = null;
                switch (value.getType()) {
                    case ELY:
                        icon = TLauncher.getInstance().getElyManager().isRefreshing() ? ELY_USER_ICON.getDisabledInstance() : ELY_USER_ICON;
                        break;
                    case MOJANG:
                        icon = MOJANG_USER_ICON;
                }

                if (icon != null) {
                    renderer.setIcon((Icon) icon);
                    renderer.setFont(renderer.getFont().deriveFont(1));
                }

                switch (type) {
                    case EDITOR:
                        if (!value.hasUsername()) {
                            renderer.setText(Localizable.get("account.creating"));
                            renderer.setFont(renderer.getFont().deriveFont(2));
                        } else {
                            renderer.setText(value.getUsername());
                        }
                        break;
                    default:
                        if (value.getType() == Account.AccountType.ELY && TLauncher.getInstance().getElyManager().isRefreshing()) {
                            renderer.setText(value.getDisplayName() + " " + Localizable.get("account.loading.ely"));
                        } else {
                            renderer.setText(value.getDisplayName());
                        }
                }
            }
        } else {
            renderer.setText(Localizable.get("account.empty"));
        }

        return renderer;
    }

    public enum AccountCellType {
        PREVIEW,
        EDITOR
    }
}
