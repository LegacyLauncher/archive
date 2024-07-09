package net.legacylauncher.ui.background;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.background.fx.FxAudioPlayer;
import net.legacylauncher.ui.background.fx.MediaFxBackground;
import net.legacylauncher.ui.swing.extended.ExtendedLayeredPane;
import net.legacylauncher.util.Lazy;
import net.legacylauncher.util.shared.JavaVersion;

import javax.swing.*;

@Slf4j
public final class BackgroundManager extends ExtendedLayeredPane {
    private final static int BACKGROUND_INDEX = 1, COVER_INDEX = Integer.MAX_VALUE;
    final Worker worker;
    final Cover cover;

    private final Lazy<ImageBackground> imageBackground;
    public final Lazy<OldAnimatedBackground> oldBackground;
    private final FXWrapper<MediaFxBackground> mediaFxBackground;

    private IBackground background;

    public BackgroundManager(MainPane pane) {
        super(pane);

        worker = new Worker(this);

        cover = new Cover();
        add(cover, COVER_INDEX);

        imageBackground = Lazy.of(ImageBackground::new);
        oldBackground = Lazy.of(OldAnimatedBackground::new);
        FXWrapper<MediaFxBackground> _mediaFxBackground = null;
        try {
            if (JavaVersion.getCurrent().getMajor() >= 11) {
                _mediaFxBackground = new FXWrapper<>(MediaFxBackground.class);
            } else {
                log.info("MediaFxBackground is not be available because it requires Java 11+");
            }
        } catch (Throwable t) {
            log.info("MediaFxBackground will not be available: {}", t.toString());
            log.debug("Detailed exception", t);
        }
        mediaFxBackground = _mediaFxBackground;
    }

    public boolean isMediaFxAvailable() {
        return mediaFxBackground != null;
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
        String path = LegacyLauncher.getInstance().getSettings().get("gui.background");
        if (path != null && mediaFxBackground != null && (path.endsWith(".mp4") || path.endsWith(".flv"))) {
            worker.setBackground(mediaFxBackground, path);
        } else {
            if (nostalgic) {
                OldAnimatedBackground nostalgicBackground = oldBackground.get();
                nostalgicBackground.getAudioPlayer().value().ifPresent(FxAudioPlayer::play);
                worker.setBackground(nostalgicBackground, path);
            } else {
                worker.setBackground(imageBackground.get(), path);
            }
        }
    }

    private boolean nostalgic;

    public void setNostalgic(boolean state) {
        this.nostalgic = state;
        this.loadBackground();
    }
}
