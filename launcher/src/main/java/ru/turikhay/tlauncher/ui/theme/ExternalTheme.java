package ru.turikhay.tlauncher.ui.theme;

import ru.turikhay.exceptions.ParseException;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.U;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public final class ExternalTheme extends ChildTheme {
    private final Properties properties = new Properties();

    ExternalTheme(String name, InputStream input) throws IOException {
        super(name);
        properties.load(U.requireNotNull(input, "input"));
    }

    private Color getColor(String key, Color defaultColor) {
        Object obj = properties.get(key);

        if(obj == null) {
            return defaultColor;
        }

        if(obj instanceof Color) {
            return (Color) obj;
        }

        int[] a;
        try {
            IntegerArray arr = IntegerArray.parseIntegerArray(String.valueOf(obj), '.');
            if(arr.size() < 3 || arr.size() > 4) {
                throw new ParseException("illegal size");
            }
            a = arr.toArray();
            for(int i : a) {
                if(i < 0 || i > 255) {
                    throw new ParseException("illegal byte:" + i);
                }
            }
        } catch(RuntimeException rE) {
            log("Could not parse color: ", key, rE);
            properties.remove(key);
            return defaultColor;
        }

        Color color = a.length == 4? new Color(a[0], a[1], a[2], a[3]) : new Color(a[0], a[1], a[2], 255);
        properties.put(key, color);

        return color;
    }

    private int getSize(String key, int maxValue, int defValue) {
        String value = properties.getProperty(key);
        if(value == null) {
            return defValue;
        }

        int intVal;
        try {
            intVal = Integer.parseInt(value);
            if(intVal < 0 || intVal > maxValue) {
                throw new ParseException("illegal value [0, "+maxValue+"]: " + key);
            }
        } catch(RuntimeException rE) {
            log("Could not parse integer: " + key, rE);
            return defValue;
        }

        return intVal;
    }

    @Override
    public Color getForeground() {
        return getColor("foreground", super.getForeground());
    }

    @Override
    public Color getSemiForeground() {
        return getColor("semiforeground", super.getSemiForeground());
    }

    @Override
    public Color getBackground() {
        return getColor("background", super.getBackground());
    }

    @Override
    public Color getPanelBackground() {
        return getColor("panelbackground", super.getPanelBackground());
    }

    @Override
    public Color getSuccess() {
        return getColor("success", super.getSuccess());
    }

    @Override
    public Color getFailure() {
        return getColor("failure", super.getFailure());
    }

    @Override
    public int getBorderSize() {
        return getSize("border.size", SystemTheme.MAX_BORDER, super.getBorderSize());
    }

    @Override
    public Color getBorder(Border border) {
        Color color = getColor("border." + U.requireNotNull(border, "border").name().toLowerCase(), null);
        if(color != null) {
            return color;
        }
        return getColor("border.color", super.getBorder(border));
    }

    @Override
    public Color getShadow(Border border) {
        Color color = getColor("shadow." + U.requireNotNull(border, "border").name().toLowerCase(), null);
        if(color != null) {
            return color;
        }

        String shadowValue = properties.getProperty("shadow");

        if("border".equalsIgnoreCase(shadowValue)) {
            return getBorder(border);
        }

        return getColor("shadow", super.getShadow(border));
    }

    @Override
    public int getArc(Border border) {
        int size = getSize("arc." + U.requireNotNull(border, "border").name().toLowerCase(), SystemTheme.MAX_ARC, -1);

        if(size > -1) {
            return size;
        }

        return getSize("arc", SystemTheme.MAX_ARC, super.getArc(border));
    }

    @Override
    public URL loadAsset(String name) throws IOException {
        String path = properties.getProperty("images");

        if(path != null) {
            File dir = new File(path);
            if(dir.isDirectory()) {
                File file = new File(dir, name);
                if(file.isFile()) {
                    return file.toURI().toURL();
                }
            }
            throw new FileNotFoundException("not a directory: " + dir.getAbsolutePath());
        }

        String subFolder = properties.getProperty("images.subFolder");
        if(subFolder != null) {
            try {
                return U.requireNotNull(super.loadAsset(subFolder + "/" + name));
            } catch(Exception e) {
                // ignore
            }
        }

        return super.loadAsset(name);
    }
}
