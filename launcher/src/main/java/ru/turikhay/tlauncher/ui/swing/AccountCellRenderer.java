package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.user.User;

import javax.swing.*;
import java.awt.*;

public class AccountCellRenderer implements ListCellRenderer<Account<? extends User>> {
    public static final Account<?> EMPTY = Account.randomAccount();
    public static final Account<?> MANAGE = Account.randomAccount();
    private static final ImageIcon MANAGE_ICON = Images.getIcon16("gear");

    private static final ImageIcon MOJANG_USER_ICON = Images.getIcon16("logo-mojang");
    private static final ImageIcon MOJANG_USER_ICON_BIG = Images.getIcon24("logo-mojang");

    private static final ImageIcon MINECRAFT_USER_ICON = Images.getIcon16("logo-microsoft");
    private static final ImageIcon MINECRAFT_USER_ICON_BIG = Images.getIcon24("logo-microsoft");

    private static final ImageIcon ELY_USER_ICON = Images.getIcon16("logo-ely");
    private static final ImageIcon ELY_USER_ICON_BIG = Images.getIcon24("logo-ely");

    private static final ImageIcon MCLEAKS_USER_ICON = Images.getIcon16("logo-mcleaks");
    private static final ImageIcon MCLEAKS_USER_ICON_BIG = Images.getIcon24("logo-mcleaks");

    private static final ImageIcon USER_ICON = Images.getIcon16("user-circle-o");
    private static final ImageIcon USER_ICON_BIG = Images.getIcon24("user-circle-o");

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

    public Component getListCellRendererComponent(JList<? extends Account<? extends User>> list, Account<? extends User> value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        renderer.setAlignmentY(0.5F);
        if (value != null && value != EMPTY) {
            if (value == MANAGE) {
                renderer.setText(Localizable.get("account.manage"));
                ImageIcon.setup(renderer, MANAGE_ICON);
            } else {
                ImageIcon icon = null;
                switch (value.getType()) {
                    case MCLEAKS:
                        icon = type == AccountCellType.EDITOR ? MCLEAKS_USER_ICON_BIG : MCLEAKS_USER_ICON;
                        break;
                    case ELY:
                    case ELY_LEGACY:
                        icon = type == AccountCellType.EDITOR ? ELY_USER_ICON_BIG : ELY_USER_ICON;
                        break;
                    case MOJANG:
                        icon = type == AccountCellType.EDITOR ? MOJANG_USER_ICON_BIG : MOJANG_USER_ICON;
                        break;
                    case MINECRAFT:
                        icon = type == AccountCellType.EDITOR ? MINECRAFT_USER_ICON_BIG : MINECRAFT_USER_ICON;
                        break;
                    case PLAIN:
                        icon = type == AccountCellType.EDITOR ? USER_ICON_BIG : USER_ICON;
                        break;
                }

                if (icon != null) {
                    ImageIcon.setup(renderer, icon);
                    renderer.setFont(renderer.getFont().deriveFont(Font.BOLD));
                } else {
                    renderer.setIcon(null);
                    renderer.setDisabledIcon(null);
                }

                switch (type) {
                    case EDITOR:
                        renderer.setText(value.getUsername());
                        break;
                    default:
                        renderer.setText(value.getDisplayName());
                        /*if (TLauncher.getInstance().getLibraryManager().isRefreshing()) {
                            renderer.setText(value.getDisplayName() + " " + Localizable.get("account.loading.ely"));
                        } else {
                            renderer.setText(value.getDisplayName());
                        }*/
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
