package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.Multipane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AccountMultipane extends CenterPanel implements LocalizableComponent {
    private final AccountManagerScene scene;

    private final ExtendedButton back;
    private final LocalizableLabel title;
    private final Multipane multipane;

    private final Map<String, AccountMultipaneComp> byName = new HashMap<>();
    private AccountMultipaneComp currentComp;

    private final ArrayList<AccountMultipaneComp> breadcrumbs = new ArrayList<>();

    public AccountMultipane(AccountManagerScene scene) {
        super(squareInsets);

        this.scene = scene;

        BorderPanel wrapper = new BorderPanel(0, SwingUtil.magnify(10));
        wrapper.setInsets(0, 0, 0, 0);

        BorderPanel titlePanel = new BorderPanel();
        titlePanel.setHgap(SwingUtil.magnify(10));

        back = new ExtendedButton();
        updateBackButtonTooltip();
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goBack();
            }
        });
        back.setIcon(Images.getIcon("arrow-left.png", SwingUtil.magnify(16)));
        titlePanel.setWest(back);

        this.title = new LocalizableLabel();
        title.setFont(title.getFont().deriveFont(title.getFont().getSize2D() + 8.f).deriveFont(Font.BOLD));
        title.setForeground(U.shiftAlpha(title.getForeground(), -96));
        title.setNotEmpty(true);
        titlePanel.setCenter(title);

        wrapper.setNorth(titlePanel);

        this.multipane = new Multipane();
        wrapper.setCenter(multipane);

        add(wrapper);
    }

    public String currentTip() {
        return currentComp == null? null : currentComp.multipaneName();
    }

    public void goBack() {
        if(breadcrumbs.size() < 2) {
            showTip("welcome");
        } else {
            showTip(breadcrumbs.get(breadcrumbs.size() - 2), true);
        }
    }

    public void clearBreadcrumbs() {
        breadcrumbs.clear();
    }

    public void registerTip(AccountMultipaneComp comp) {
        byName.put(comp.multipaneName(), comp);
        multipane.add(comp.multipaneComp(), comp.multipaneName());
    }

    public void showTip(String name) {
        AccountMultipaneComp comp = byName.get(name);

        if(comp == null) {
            throw new IllegalArgumentException("no tip found: " + name);
        }

        showTip(comp, false);
    }

    private void showTip(AccountMultipaneComp comp, boolean gotBack) {
        if(currentComp != null && currentComp instanceof AccountMultipaneCompCloseable) {
            ((AccountMultipaneCompCloseable) currentComp).multipaneClosed();
        }

        if (!(comp instanceof AccountMultipaneCompCloseable)) {
            breadcrumbs.clear();
        } else {
            if(!gotBack) {
                breadcrumbs.add(comp);
            } else {
                breadcrumbs.remove(breadcrumbs.size() - 1);
            }
        }

        this.currentComp = comp;

        if(comp.multipaneLocksView()) {
            Blocker.block(scene, "tip-lock");
        } else {
            Blocker.unblock(scene, "tip-lock");
        }

        title.setText(AccountMultipaneComp.LOC_PREFIX_PATH + comp.multipaneName() + ".title");
        multipane.show(comp.multipaneName());
        back.setVisible(comp instanceof AccountMultipaneCompCloseable);

        repaint();

        comp.multipaneShown(gotBack);
    }

    @Override
    public void updateLocale() {
        updateBackButtonTooltip();
        Localizable.updateContainer(title);
        Localizable.updateContainer(multipane);
    }

    private void updateBackButtonTooltip() {
        back.setToolTipText(Localizable.get("account.manager.multipane.back"));
    }
}
