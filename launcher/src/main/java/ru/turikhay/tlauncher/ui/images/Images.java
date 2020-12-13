package ru.turikhay.tlauncher.ui.images;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;

public class Images {
    private static final Logger LOGGER = LogManager.getLogger(Images.class);

    private static final Map<URL, WeakReference<BufferedImage>> loadedImages = new Hashtable<URL, WeakReference<BufferedImage>>();
    private static final Map<URL, WeakReference<Image>> magnifiedImages = new Hashtable<URL, WeakReference<Image>>();
    private static final boolean THROW_IF_ERROR = true;

    public static BufferedImage loadImage(URL url, boolean throwIfError) {
        if (url == null) {
            throw new NullPointerException("URL is NULL");
        } else {
            WeakReference<BufferedImage> ref = loadedImages.get(url);
            BufferedImage image = ref == null ? null : ref.get();
            if (image != null) return image;

            try {
                image = ImageIO.read(url);
            } catch (Exception var3) {
                if (throwIfError) {
                    throw new RuntimeException("could not load the image", var3);
                }
                LOGGER.warn("could not load the image: {}", url, var3);
                return null;
            }

            loadedImages.put(url, new WeakReference<BufferedImage>(image));
            return image;
        }
    }

    static Image loadMagnifiedImage(URL url, boolean throwIfError) {
        WeakReference<Image> ref = magnifiedImages.get(url);

        Image cached = ref == null ? null : ref.get();
        if (cached != null) {
            return cached;
        }

        BufferedImage image = loadImage(url, throwIfError);
        if (image == null) {
            return null;
        }

        Image scaled = image.getScaledInstance(SwingUtil.magnify(image.getWidth()), SwingUtil.magnify(image.getHeight()), 4);
        magnifiedImages.put(url, new WeakReference<Image>(scaled));
        return scaled;
    }

    public static Image loadMagnifiedImage(URL url) {
        return loadMagnifiedImage(url, THROW_IF_ERROR);
    }

    public static BufferedImage getImage(String uri, boolean throwIfError) {
        return loadImage(getRes(uri), throwIfError);
    }

    public static BufferedImage getImage(String uri) {
        return getImage(uri, true);
    }

    public static ImageIcon getIcon(String uri, int width, int height, boolean throwIfError) {
        return new ImageIcon(getImage(uri, throwIfError), width, height);
    }

    public static ImageIcon getIcon(String uri, int width, int height) {
        return getIcon(uri, width, height, true);
    }

    public static ImageIcon getIcon(String uri, int widthNheight) {
        return getIcon(uri, widthNheight, widthNheight, true);
    }

    public static ImageIcon getIcon(String uri, boolean throwIfError) {
        return new ImageIcon(getImage(uri, throwIfError));
    }

    public static ImageIcon getIcon(String uri) {
        return getIcon(uri, 0, 0);
    }

    public static ImageIcon getScaledIcon(String uri, int widthNheight) {
        return getIcon(uri, SwingUtil.magnify(widthNheight));
    }

    public static URL getScaledIconUrl(Image original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        File file;
        try {
            file = File.createTempFile("tlauncher-scaledimage", null);
        } catch (IOException ioE) {
            throw new RuntimeException("could not create temp file", ioE);
        }
        file.deleteOnExit();

        try {
            ImageIO.write(resized, "png", file);
        } catch (IOException ioE) {
            throw new RuntimeException("could not write resized image to the file", ioE);
        }

        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("could not get url of resized image file", e);
        }
    }

    public static URL getRes(String uri, boolean throwIfNull) {
        if (uri == null) {
            if (throwIfNull) {
                throw new NullPointerException();
            } else {
                return null;
            }
        } else {
            try {
                return Theme.getTheme().loadAsset(uri);
            } catch (IOException e) {
                if(throwIfNull) {
                    throw new RuntimeException("cannot find resource: \""+ uri +"\"");
                } else {
                    return null;
                }
            }
        }
    }

    public static URL getRes(String uri) {
        return getRes(uri, true);
    }
}
