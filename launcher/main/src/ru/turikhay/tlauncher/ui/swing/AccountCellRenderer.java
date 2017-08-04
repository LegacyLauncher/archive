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

    private static final ImageIcon MOJANG_USER_ICON = Images.getIcon("mojang.png", SwingUtil.magnify(16));
    private static final ImageIcon MOJANG_USER_ICON_BIG = Images.getIcon("mojang.png", SwingUtil.magnify(24));

    private static final ImageIcon ELY_USER_ICON = Images.getIcon("ely.png", SwingUtil.magnify(16));
    private static final ImageIcon ELY_USER_ICON_BIG = Images.getIcon("ely.png", SwingUtil.magnify(24));

    private static final ImageIcon USER_ICON = Images.getIcon("user-circle-o.png", SwingUtil.magnify(16));
    private static final ImageIcon USER_ICON_BIG = Images.getIcon("user-circle-o.png", SwingUtil.magnify(24));

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
        if (value != null && value != EMPTY) {
            if (value == MANAGE) {
                renderer.setText(Localizable.get("account.manage"));
                ImageIcon.setup(renderer, MANAGE_ICON);
            } else {
                Icon icon = null;
                switch (value.getType()) {
                    case ELY:
                    case ELY_LEGACY:
                        ImageIcon _icon = type == AccountCellType.EDITOR? ELY_USER_ICON_BIG : ELY_USER_ICON;
                        icon = TLauncher.getInstance().getElyManager().isRefreshing() ? _icon.getDisabledInstance() : _icon;
                        break;
                    case MOJANG:
                        icon = type == AccountCellType.EDITOR? MOJANG_USER_ICON_BIG : MOJANG_USER_ICON;
                        break;
                    case PLAIN:
                        icon = type == AccountCellType.EDITOR? USER_ICON_BIG : USER_ICON;
                        break;
                }

                if (icon != null) {
                    if(icon instanceof ImageIcon) {
                        ImageIcon.setup(renderer, (ImageIcon) icon);
                    } else {
                        renderer.setIcon(icon);
                    }
                    renderer.setFont(renderer.getFont().deriveFont(1));
                } else {
                    renderer.setIcon(null);
                    renderer.setDisabledIcon(null);
                }

                switch (type) {
                    case EDITOR:
                        renderer.setText(value.getUsername());
                        break;
                    default:
                        if ((value.getType() == Account.AccountType.ELY || value.getType() == Account.AccountType.ELY_LEGACY) && TLauncher.getInstance().getElyManager().isRefreshing()) {
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
