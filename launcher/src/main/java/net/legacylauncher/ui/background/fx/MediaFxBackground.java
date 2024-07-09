package net.legacylauncher.ui.background.fx;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.ui.background.IFXBackground;
import net.legacylauncher.util.FileUtil;

import java.io.File;
import java.net.URL;

@Slf4j
public class MediaFxBackground extends Pane implements IFXBackground {
    private final MediaView view = new MediaView();

    {
        view.setCache(true);
        view.setCacheHint(CacheHint.SPEED);
    }

    private final Rectangle rect = new Rectangle();

    public MediaFxBackground() {
        //minWidthProperty().bind(prefWidthProperty());
        //minHeightProperty().bind(prefHeightProperty());

        sceneProperty().addListener(observable -> {
            final Scene scene = getScene();

            prefWidthProperty().unbind();
            prefWidthProperty().bind(scene.widthProperty());

            prefHeightProperty().unbind();
            prefHeightProperty().bind(scene.heightProperty());
        });

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));

        rect.setFill(Color.BLACK);
        rect.widthProperty().bind(widthProperty());
        rect.heightProperty().bind(heightProperty());

        getChildren().addAll(view, rect);
    }

    @Override
    public void startBackground() {
        if (view.getMediaPlayer() != null) {
            log.debug("started");
            view.getMediaPlayer().play();
        }
    }

    @Override
    public void pauseBackground() {
        if (view.getMediaPlayer() != null) {
            log.debug("paused");
            view.getMediaPlayer().pause();
        }
    }

    @Override
    public void loadBackground(String path) throws Exception {
        view.setMediaPlayer(null);
        view.layoutXProperty().unbind();
        view.layoutYProperty().unbind();
        view.fitWidthProperty().unbind();
        view.fitHeightProperty().unbind();

        if (path == null) {
            throw new NullPointerException("path");
        }

        URL url;

        if (FileUtil.fileExists(path)) {
            url = new File(path).toURI().toURL();
        } else {
            url = new URL(path);
        }

        log.debug("Loading media {}", url);

        final Media media = new Media(url.toExternalForm());
        final MediaPlayer player = new MediaPlayer(media);
        player.setMute(true);
        player.setCycleCount(-1);

        view.setMediaPlayer(player);

        player.setOnHalted(() -> {
            if (view.getMediaPlayer() != player) {
                return;
            }
            log.error("Error loading media {}", media, player.getError());
        });

        player.setOnReady(() -> {
            if (view.getMediaPlayer() != player) {
                return;
            }

            NumberBinding
                    ratio = Bindings.min(media.widthProperty().divide(widthProperty()), media.heightProperty().divide(heightProperty())),
                    width = media.widthProperty().divide(ratio),
                    height = media.heightProperty().divide(ratio),
                    x = Bindings.subtract(widthProperty(), width).divide(2.),
                    y = Bindings.subtract(heightProperty(), height).divide(2.);

            view.layoutXProperty().bind(x);
            view.layoutYProperty().bind(y);
            view.fitWidthProperty().bind(width);
            view.fitHeightProperty().bind(height);

            FadeTransition transition = new FadeTransition();
            transition.setNode(rect);
            transition.setDuration(Duration.millis(250));
            transition.setToValue(0.);

            transition.play();
            player.play();
        });

        if (player.getStatus() == MediaPlayer.Status.HALTED) {
            log.error("Could not load media {}", media.getSource(), player.getError());
        }
    }
}
