package ru.turikhay.tlauncher.ui.background;

import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.MainPane;
import ru.turikhay.tlauncher.ui.background.fx.MediaFxBackground;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.util.JavaVersion;

import javax.swing.*;

public final class BackgroundManager extends ExtendedLayeredPane {
    private static final Logger LOGGER = LogManager.getLogger(BackgroundManager.class);

    private final static int BACKGROUND_INDEX = 1, COVER_INDEX = Integer.MAX_VALUE;
    final Worker worker;
    final Cover cover;

    private final ImageBackground imageBackground;
    private final FXWrapper<MediaFxBackground> mediaFxBackground;

    private IBackground background;

    public BackgroundManager(MainPane pane) {
        super(pane);

        worker = new Worker(this);

        cover = new Cover();
        add(cover, COVER_INDEX);

        imageBackground = new ImageBackground();
        OldAnimatedBackground oldAnimatedBackground = null;//new OldAnimatedBackground();
        FXWrapper<MediaFxBackground> _mediaFxBackground = null;
        try {
            if (JavaVersion.getCurrent().getMajor() >= 11) {
                _mediaFxBackground = new FXWrapper<>(MediaFxBackground.class);
            } else {
                LOGGER.info("MediaFxBackground is not be available because it requires Java 11+");
            }
        } catch (Throwable t) {
            LOGGER.info("MediaFxBackground will not be available: {}", t.toString());
            LOGGER.debug("Detailed exception", t);
        }
        mediaFxBackground = _mediaFxBackground;
    }

    public ImageBackground getImageBackground() {
        return imageBackground;
    }

    public FXWrapper<MediaFxBackground> getMediaFxBackground() {
        return mediaFxBackground;
    }

    void setBackground(IBackground background) {
        if (this.background == background) {
            return;
        }

        if (this.background != null) {
            this.background.pauseBackground();
            remove((JComponent) this.background);
        }

        if (background != null) {
            add((JComponent) background, BACKGROUND_INDEX);
            background.startBackground();
        }

        this.background = background;
        onResize();
    }

    public void startBackground() {
        if (background != null) {
            background.startBackground();
        }
    }

    public void pauseBackground() {
        if (background != null) {
            background.pauseBackground();
        }
    }

    public void loadBackground() {
        String path = TLauncher.getInstance().getSettings().get("gui.background");
        if (path != null && mediaFxBackground != null && (path.endsWith(".mp4") || path.endsWith(".flv"))) {
            worker.setBackground(mediaFxBackground, path);
        } else {
            worker.setBackground(imageBackground, path);
        }
        /*if(path == null) {
            worker.setBackground(oldAnimatedBackground, null);
        } else {
            if (mediaFxBackground != null && (path.endsWith(".mp4") || path.endsWith(".flv"))) {
                worker.setBackground(mediaFxBackground, path);
            } else {
                worker.setBackground(imageBackground, path);
            }
        }*/
    }
}
