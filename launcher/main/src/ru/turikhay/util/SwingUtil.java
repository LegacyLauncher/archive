package ru.turikhay.util;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.tlauncher.ui.images.Images;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.View;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class SwingUtil {
    private static final List<Image> favicons = new ArrayList();

    public static List<Image> getFavicons() {
        if (!favicons.isEmpty()) {
            return Collections.unmodifiableList(favicons);
        } else {
            int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};
            String loaded = "";
            int[] var5 = sizes;
            int var4 = sizes.length;

            for (int var3 = 0; var3 < var4; ++var3) {
                int i = var5[var3];
                Image image = Images.getImage("fav" + i + ".png", false);
                if (image != null) {
                    loaded = loaded + ", " + i + "px";
                    favicons.add(image);
                }
            }

            if (loaded.isEmpty()) {
                log("No favicon is loaded.");
            } else {
                log("Favicons loaded:", loaded.substring(2));
            }

            return favicons;
        }
    }

    public static void setFavicons(JFrame frame) {
        frame.setIconImages(getFavicons());
    }

    private static boolean allowSystemLookAndFeel = true;

    public static boolean initLookAndFeel() {
        if(allowSystemLookAndFeel) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                return true;
            } catch (Exception e) {
                log("Can't set system look and feel.", e);
                allowSystemLookAndFeel = false;
                return false;
            }
        }
        return false;
    }

    public static void resetLookAndFeel() {
        allowSystemLookAndFeel = false;
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            log("Can't set default look and feel!", e);
        }
    }

    public static void initFontSize(int defSize) {
        try {
            UIDefaults e = UIManager.getDefaults();
            int minSize = defSize;
            int maxSize = defSize + 2;
            Enumeration e1 = e.keys();

            while (e1.hasMoreElements()) {
                Object key = e1.nextElement();
                Object value = e.get(key);
                if (value instanceof Font) {
                    Font font = (Font) value;
                    int size = font.getSize();
                    if (size < minSize) {
                        size = minSize;
                    } else if (size > maxSize) {
                        size = maxSize;
                    }

                    if (value instanceof FontUIResource) {
                        e.put(key, new FontUIResource(font.getName(), font.getStyle(), size));
                    } else {
                        e.put(key, new Font(font.getName(), font.getStyle(), size));
                    }
                }
            }
        } catch (Exception var9) {
            log("Cannot change font sizes!", var9);
        }

    }

    public static Cursor getCursor(int type) {
        try {
            return Cursor.getPredefinedCursor(type);
        } catch (IllegalArgumentException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static void setFontSize(JComponent comp, float size) {
        comp.setFont(comp.getFont().deriveFont(size));
    }

    public static int magnify(int i) {
        return (int) Math.rint(i * TLauncherFrame.magnifyDimensions);
    }

    public static float magnify(float i) {
        return (float) (i * TLauncherFrame.magnifyDimensions);
    }

    public static Dimension magnify(Dimension d) {
        return new Dimension(magnify(d.width), magnify(d.height));
    }

    public static Insets magnify(Insets i) {
        i.top = magnify(i.top);
        i.left = magnify(i.left);
        i.bottom = magnify(i.bottom);
        i.right = magnify(i.right);
        return i;
    }

    public static boolean isThiner(Dimension d1, Dimension d2) {
        return Math.min(d1.width, d2.width) == d1.width || Math.min(d1.height, d2.height) == d1.height;
    }

    private static void log(Object... o) {
        U.log("[Swing]", o);
    }

    public static Dimension getPrefSize(JComponent component, int width, int height) {
        U.requireNotNull(component, "component");
        View view;

        if(component instanceof JLabel) {
            view = (View) component.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
        } else if(component instanceof JEditorPane) {
            view = ((JEditorPane) component).getUI().getRootView((JEditorPane) component);
        } else {
            throw new IllegalArgumentException();
        }

        view.setSize(width, height);
        return new Dimension((int) Math.ceil(view.getPreferredSpan(View.X_AXIS)), (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)));
    }

    // when determining for indefinite height - use 0
    public static int getPrefWidth(JComponent component, int height) {
        int minHeight = getPrefHeight(component, Integer.MAX_VALUE), curHeight, width = 0;
        do {
            curHeight = getPrefHeight(component, width += 1);
        } while(curHeight >= height && curHeight != minHeight);

        return width;
    }

    // when determining for indefinite width - use Integer.MAX_VALUE
    public static int getPrefHeight(JComponent component, int width) {
        return getPrefSize(component, width, 0).height;
    }

    public static void setClipboard(String text) {
        if (text == null) {
            return;
        }

        try {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, new ClipboardOwner() {
                @Override
                public void lostOwnership(Clipboard clipboard, Transferable contents) {
                }
            });
        } catch (Exception e) {
            U.log("Could not copy", e);
        }
    }

    private static final String base64s = "data:image/", base64e = ";base64,";

    public static BufferedImage base64ToImage(String source) throws IOException {
        if (!source.startsWith(base64s)) {
            throw new IllegalArgumentException("not a bse64 format");
        }

        int offset = base64s.length();

        if (source.startsWith("png", offset) || source.startsWith("jpg", offset) || source.startsWith("gif")) {
            offset += 3;
        } else if (source.startsWith("jpeg", offset)) {
            offset += 4;
        } else {
            return null;
        }

        if (!source.substring(offset, offset + base64e.length()).equals(base64e)) {
            return null;
        }

        offset += base64e.length();

        return ImageIO.read(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(source.substring(offset))));
    }

    public static Image loadImage(String source) throws IOException {
        try {
            return SwingUtil.base64ToImage(source);
        } catch (IllegalArgumentException ile) {
            // ignore
        }

        URL src = U.makeURL(source);
        if (src != null) {
            return Images.loadMagnifiedImage(src);
        }

        return Images.getImage(source, false);
    }
}
