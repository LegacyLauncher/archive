package net.legacylauncher.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.ui.LegacyLauncherFrame;
import net.legacylauncher.ui.images.Images;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.View;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class SwingUtil {
    private static final Lazy<List<Image>> favicons = Lazy.of(() -> createFaviconList("logo-tl"));
    private static final String base64s = "data:image/", base64e = ";base64,";
    private static final Lazy<Double> SCALING_FACTOR = Lazy.of(SwingUtil::queryScalingFactor);

    public static List<Image> createFaviconList(String iconName) {
        ArrayList<Image> favicons = new ArrayList<>();
        final int[] sizes = new int[]{256, 128, 96, 64, 48, 32, 24, 16};
        for (int size : sizes) {
            Image image = Images.loadIcon(iconName, size);
            if (image != null) {
                favicons.add(image);
            }
        }
        return Collections.unmodifiableList(favicons);
    }

    public static List<Image> getFavicons() {
        return favicons.get();
    }

    public static void setFavicons(JFrame frame) {
        frame.setIconImages(getFavicons());
    }

    public static void initFontSize(int defSize) {
        try {
            UIDefaults e = UIManager.getDefaults();
            int maxSize = defSize + 2;
            Enumeration<?> e1 = e.keys();

            while (e1.hasMoreElements()) {
                Object key = e1.nextElement();
                Object value = e.get(key);
                if (value instanceof Font) {
                    Font font = (Font) value;
                    int size = font.getSize();
                    if (size < defSize) {
                        size = defSize;
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
            log.warn("Cannot change font size", var9);
        }

    }

    public static Cursor getCursor(int type) {
        try {
            return Cursor.getPredefinedCursor(type);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to fetch cursor with type {}", type, e);
            return null;
        }
    }

    public static void setFontSize(JComponent comp, float size) {
        comp.setFont(comp.getFont().deriveFont(size));
    }

    public static int magnify(int i) {
        return (int) Math.rint(i * LegacyLauncherFrame.magnifyDimensions);
    }

    public static float magnify(float i) {
        return (float) (i * LegacyLauncherFrame.magnifyDimensions);
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

    public static Dimension getPrefSize(JComponent component, int width, int height) {
        Objects.requireNonNull(component, "component");
        View view;

        if (component instanceof JLabel) {
            view = (View) component.getClientProperty(javax.swing.plaf.basic.BasicHTML.propertyKey);
        } else if (component instanceof JEditorPane) {
            view = ((JEditorPane) component).getUI().getRootView((JEditorPane) component);
        } else {
            throw new IllegalArgumentException();
        }

        view.setSize(width, height);
        return new Dimension((int) Math.ceil(view.getPreferredSpan(View.X_AXIS)), (int) Math.ceil(view.getPreferredSpan(View.Y_AXIS)));
    }

    // when determining for indefinite height - use 0
    public static int getPrefWidth(JComponent component, int height, int step) {
        int minHeight = getPrefHeight(component, Integer.MAX_VALUE), curHeight, width = 0;
        do {
            curHeight = getPrefHeight(component, width += step);
        } while (curHeight >= height && curHeight != minHeight);

        return width;
    }

    // when determining for indefinite width - use Integer.MAX_VALUE
    public static int getPrefHeight(JComponent component, int width) {
        return getPrefSize(component, width, 0).height;
    }

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

        if (!source.startsWith(base64e, offset)) {
            return null;
        }

        offset += base64e.length();

        return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(source.substring(offset).getBytes(StandardCharsets.UTF_8))));
    }

    public static Image loadImage(String source) throws IOException {
        try {
            return SwingUtil.base64ToImage(source);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            return ImageIO.read(new URL(source));
        } catch (MalformedURLException ignored) {
        }

        return Images.loadIconById(source);
    }

    private static Runnable asRunnable(Callable<?> callable) {
        //noinspection Convert2Lambda,Anonymous2MethodRef
        return new Runnable() {
            @Override
            @SneakyThrows
            public void run() {
                callable.call();
            }
        };
    }

    public static void later(Callable<?> r) {
        later(asRunnable(r));
    }

    public static void later(Runnable r) {
        EventQueue.invokeLater(r);
    }

    public static void wait(Callable<?> r) {
        wait(asRunnable(r));
    }

    public static void wait(Runnable r) {
        if (EventQueue.isDispatchThread()) {
            invokeNow(r);
        } else {
            invokeAndWait(r);
        }
    }

    @SneakyThrows
    public static <V> V waitAndReturn(Callable<V> callable) {
        CompletableFuture<V> future = new CompletableFuture<>();
        wait(() -> {
            try {
                future.complete(callable.call());
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future.get();
    }

    @SneakyThrows
    private static void invokeNow(Runnable r) {
        r.run();
    }

    @SneakyThrows
    private static void invokeAndWait(Runnable r) {
        try {
            EventQueue.invokeAndWait(r);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw interruptedException;
        }
    }

    public static Executor executor() {
        return SwingUtil::later;
    }

    public static double getScalingFactor() {
        return SCALING_FACTOR.value().orElse(1.0);
    }

    private static double queryScalingFactor() throws Exception {
        try {
            GraphicsDevice graphicsDevice = GraphicsEnvironment
                    .getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            GraphicsConfiguration graphicsConfig = graphicsDevice
                    .getDefaultConfiguration();

            AffineTransform tx = graphicsConfig.getDefaultTransform();
            double scaleX = tx.getScaleX();
            double scaleY = tx.getScaleY();

            return Math.max(scaleX, scaleY);
        } catch (NoClassDefFoundError | NoSuchMethodError t) {
            throw new Exception(t);
        }
    }

    public static void updateUIContainer(Container container) {
        updateUIComponent(container);
        for (Component component : container.getComponents()) {
            updateUIComponent(component);
        }
    }

    private static void updateUIComponent(Component component) {
        if (component instanceof JComponent) {
            ((JComponent) component).updateUI();
        }
    }

    public static void updateUINullable(JComponent... components) {
        for (JComponent component : components) {
            if (component instanceof JPopupMenu) {
                updateUIContainer(component);
            } else if (component != null) {
                component.updateUI();
            }
        }
    }
}
