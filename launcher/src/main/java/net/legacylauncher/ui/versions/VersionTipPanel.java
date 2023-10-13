package net.legacylauncher.ui.versions;

import net.legacylauncher.ui.center.CenterPanel;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.ResizeableComponent;
import net.legacylauncher.ui.swing.extended.HTMLLabel;
import net.legacylauncher.util.OS;

import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public class VersionTipPanel extends CenterPanel implements LocalizableComponent, ResizeableComponent {
    private final HTMLLabel tip = new HTMLLabel();

    VersionTipPanel(VersionHandler handler) {
        super(CenterPanel.tipTheme, CenterPanel.squareInsets);
        add(tip);
        tip.addPropertyChangeListener("html", evt -> {
            Object o = evt.getNewValue();
            if (o instanceof View) {
                View view = (View) o;
                BasicHTML.getHTMLBaseline(view, 300 - getHorizontalInsets(), 0);
            }
        });
        updateLocale();
    }

    public void updateLocale() {
        tip.setText("");
        String text = Localizable.get("version.list.tip");
        if (text != null) {
            text = text.replace("{Ctrl}", OS.OSX.isCurrent() ? "Command" : "Ctrl");
            tip.setText(text);
            onResize();
        }
    }

    public void onResize() {
        setSize(300, tip.getHeight() + getVerticalInsets());
    }

    private int getVerticalInsets() {
        return getInsets().top + getInsets().bottom;
    }

    private int getHorizontalInsets() {
        return getInsets().left + getInsets().right;
    }
}
