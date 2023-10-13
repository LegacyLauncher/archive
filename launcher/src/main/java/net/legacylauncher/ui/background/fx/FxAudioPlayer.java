
package net.legacylauncher.ui.background.fx;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.function.Consumer;

public class FxAudioPlayer {

    private final URL url;

    private MediaPlayer player;

    public FxAudioPlayer(URL url) {
        this.url = url;
    }

    public void play() {
        Platform.runLater(() -> {
            if (player == null) {
                player = new MediaPlayer(new Media(url.toExternalForm()));
                player.setCycleCount(MediaPlayer.INDEFINITE);
                player.setVolume(.1);
            }
            player.play();
        });
    }

    public void pause() {
        runIfInitialized(MediaPlayer::pause);
    }

    public void stop() {
        runIfInitialized(MediaPlayer::stop);
    }

    private void runIfInitialized(Consumer<MediaPlayer> action) {
        Platform.runLater(() -> {
            if (player != null) {
                action.accept(player);
            }
        });
    }

    public static FxAudioPlayer create(URL url) throws NoClassDefFoundError, NoSuchMethodError {
        PlatformImpl.startup(() -> {});
        return new FxAudioPlayer(url);
    }
}
