package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

public class AccountTip extends CenterPanel implements LocalizableComponent {
    public static final int WIDTH = SwingUtil.magnify(500);
    private final AccountEditorScene scene;
    public final AccountTip.Tip freeTip;
    public final AccountTip.Tip mojangTip;
    public final AccountTip.Tip elyTip;
    private AccountTip.Tip tip;
    private final EditorPane content;

    public AccountTip(AccountEditorScene sc) {
        super(CenterPanel.tipTheme, smallSquareInsets);
        setMagnifyGaps(false);
        scene = sc;
        content = new EditorPane(getFont().deriveFont(TLauncherFrame.getFontSize()));
        content.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mousePressed(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mouseReleased(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mouseEntered(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }

            public void mouseExited(MouseEvent e) {
                if (!isVisible()) {
                    e.consume();
                }

            }
        });
        add(content);
        freeTip = new AccountTip.Tip(Account.AccountType.FREE, null);
        mojangTip = new AccountTip.Tip(Account.AccountType.MOJANG, Images.getImage("star.png"));
        elyTip = new AccountTip.Tip(Account.AccountType.ELY, Images.getImage("ely.png"));
        setTip(null);
    }

    public void setAccountType(Account.AccountType type) {
        if (type != null) {
            switch (type) {
                case ELY:
                    setTip(elyTip);
                    return;
                case MOJANG:
                    setTip(mojangTip);
                    return;
                case FREE:
                    setTip(freeTip);
                    return;
            }
        }

        setTip(null);
    }

    public AccountTip.Tip getTip() {
        return tip;
    }

    public void setTip(AccountTip.Tip tip) {
        this.tip = tip;
        if (tip == null) {
            setVisible(false);
        } else {
            setVisible(true);

            String localized = Localizable.get(tip.path), htmlized = "<html>" + localized + "</html>";
            ExtendedLabel l = new ExtendedLabel();
            l.setText(htmlized);

            int width = WIDTH, height = height = SwingUtil.getPrefHeight(l, width), imageWidth = 0, imageHeight = 0;
            if (tip.image != null) {
                imageWidth = (int) ((double) height * tip.image.getWidth(null) / tip.image.getHeight(null));
                height = SwingUtil.getPrefHeight(l, width - imageWidth);
                imageHeight = height;
            }

            StringBuilder builder = new StringBuilder();
            builder.append("<table width=\"").append(width).append("\" height=\"").append(height).append("\"><tr><td align=\"center\" valign=\"center\">");

            if (tip.image != null) {
                URL url = Images.getScaledIconUrl(tip.image, imageWidth, imageHeight);
                builder.append("<img src=\"").append(url.toExternalForm()).append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");
            }

            builder.append(localized);
            builder.append("</td></tr></table>");
            setContent(builder.toString(), width, height);
        }
    }

    void setContent(String text, int width, int height) {
        if (width >= 1 && height >= 1) {
            content.setText(text);
            setSize(width + getInsets().left + getInsets().right, height + getInsets().top + getInsets().bottom * 2);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void updateLocale() {
        setTip(tip);
    }

    public class Tip {
        private final Account.AccountType type;
        private final String path;
        private final Image image;

        Tip(Account.AccountType type, Image image) {
            this.type = type;
            path = "auth.tip." + type;
            this.image = image;
        }
    }
}
