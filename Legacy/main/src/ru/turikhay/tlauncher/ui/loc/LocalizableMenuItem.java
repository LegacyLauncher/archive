package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LocalizableMenuItem extends JMenuItem implements LocalizableComponent {
    private static final long serialVersionUID = 1364363532569997394L;
    private static List<LocalizableMenuItem> items = Collections.synchronizedList(new ArrayList());
    private String path;
    private String[] variables;

    public LocalizableMenuItem(String path, Object... vars) {
        items.add(this);
        setText(path, vars);
    }

    public LocalizableMenuItem(String path) {
        this(path, Localizable.EMPTY_VARS);
    }

    public void setText(String path, Object... vars) {
        this.path = path;
        variables = Localizable.checkVariables(vars);
        String value = Localizable.get(path);

        for (int i = 0; i < variables.length; ++i) {
            value = value.replace("%" + i, variables[i]);
        }

        super.setText(value);
    }

    public void setText(String path) {
        setText(path, Localizable.EMPTY_VARS);
    }

    public void setVariables(Object... vars) {
        setText(path, vars);
    }

    public void updateLocale() {
        setText(path, variables);
    }

    public static void updateLocales() {
        Iterator var1 = items.iterator();

        while (var1.hasNext()) {
            LocalizableMenuItem item = (LocalizableMenuItem) var1.next();
            if (item != null) {
                item.updateLocale();
            }
        }
    }

    public static LocalizableMenuItem newItem(String path, String image, ActionListener action) {
        LocalizableMenuItem item = new LocalizableMenuItem(path);
        if(image != null) {
            item.setIcon(Images.getIcon(image, SwingUtil.magnify(16)));
        }
        item.addActionListener(action);
        return item;
    }

    public static LocalizableMenuItem newItem(String path, ActionListener action) {
        return newItem(path, null, action);
    }
}
