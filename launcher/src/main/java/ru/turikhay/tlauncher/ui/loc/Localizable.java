package ru.turikhay.tlauncher.ui.loc;

import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.util.U;

import java.awt.*;

public class Localizable {
    public static final Object[] EMPTY_VARS = new Object[0];
    public static final Localizable.LocalizableFilter defaultFilter = comp -> true;
    private static LangConfiguration lang;

    public static void setLang(LangConfiguration l) {
        lang = l;
    }

    public static LangConfiguration get() {
        return lang;
    }

    public static boolean exists() {
        return lang != null;
    }

    public static String get(String path) {
        return lang != null ? lang.get(path) : path;
    }

    public static String get(String path, Object... vars) {
        return lang != null ? lang.get(path, vars) : path + " {" + U.toLog(vars) + "}";
    }

    public static String nget(String path) {
        return lang != null ? lang.nget(path) : null;
    }

    public static String[] checkVariables(Object[] check) {
        if (check == null) {
            throw new NullPointerException();
        } else {
            String[] string = new String[check.length];

            for (int i = 0; i < check.length; ++i) {
                if (check[i] == null) {
                    throw new NullPointerException("Variable at index " + i + " is NULL!");
                }

                string[i] = check[i].toString();
            }

            return string;
        }
    }

    public static void updateContainer(Container container, Localizable.LocalizableFilter filter) {
        Component[] var5;
        int var4 = (var5 = container.getComponents()).length;

        for (int var3 = 0; var3 < var4; ++var3) {
            Component c = var5[var3];

            if (c instanceof LocalizableComponent && filter.localize(c)) {
                ((LocalizableComponent) c).updateLocale();
            }

            if (c instanceof Container) {
                updateContainer((Container) c, filter);
            }
        }

    }

    public static void updateContainer(Container container) {
        updateContainer(container, defaultFilter);
    }

    public interface LocalizableFilter {
        boolean localize(Component var1);
    }
}
