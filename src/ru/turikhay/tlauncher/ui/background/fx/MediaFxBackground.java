package ru.turikhay.tlauncher.ui.background.fx;

import java.io.File;
import java.net.URL;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import ru.turikhay.tlauncher.ui.background.IFXBackground;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class MediaFxBackground extends Pane implements IFXBackground {
   private final MediaView view = new MediaView();
   private final Rectangle rect;

   public MediaFxBackground() {
      this.view.setCache(true);
      this.view.setCacheHint(CacheHint.SPEED);
      this.rect = new Rectangle();
      this.sceneProperty().addListener(new InvalidationListener() {
         public void invalidated(Observable observable) {
            Scene scene = MediaFxBackground.this.getScene();
            MediaFxBackground.this.prefWidthProperty().unbind();
            MediaFxBackground.this.prefWidthProperty().bind(scene.widthProperty());
            MediaFxBackground.this.prefHeightProperty().unbind();
            MediaFxBackground.this.prefHeightProperty().bind(scene.heightProperty());
         }
      });
      this.setBackground(new Background(new BackgroundFill[]{new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)}));
      this.rect.setFill(Color.BLACK);
      this.rect.widthProperty().bind(this.widthProperty());
      this.rect.heightProperty().bind(this.heightProperty());
      this.getChildren().addAll(new Node[]{this.view, this.rect});
   }

   public void startBackground() {
      if (this.view.getMediaPlayer() != null) {
         this.log("started");
         this.view.getMediaPlayer().play();
      }

   }

   public void pauseBackground() {
      if (this.view.getMediaPlayer() != null) {
         this.log("paused");
         this.view.getMediaPlayer().pause();
      }

   }

   public void loadBackground(String path) throws Exception {
      this.view.setMediaPlayer((MediaPlayer)null);
      this.view.layoutXProperty().unbind();
      this.view.layoutYProperty().unbind();
      this.view.fitWidthProperty().unbind();
      this.view.fitHeightProperty().unbind();
      if (path == null) {
         throw new NullPointerException("path");
      } else {
         URL url;
         if (FileUtil.fileExists(path)) {
            url = (new File(path)).toURI().toURL();
         } else {
            url = new URL(path);
         }

         final Media media = new Media(url.toExternalForm());
         final MediaPlayer player = new MediaPlayer(media);
         player.setMute(true);
         player.setCycleCount(-1);
         this.view.setMediaPlayer(player);
         player.setOnHalted(new Runnable() {
            public void run() {
               if (MediaFxBackground.this.view.getMediaPlayer() == player) {
                  MediaFxBackground.this.log("Error loading media", media, player.getError());
               }
            }
         });
         player.setOnReady(new Runnable() {
            public void run() {
               if (MediaFxBackground.this.view.getMediaPlayer() == player) {
                  NumberBinding ratio = Bindings.min(media.widthProperty().divide(MediaFxBackground.this.widthProperty()), media.heightProperty().divide(MediaFxBackground.this.heightProperty()));
                  NumberBinding width = media.widthProperty().divide(ratio);
                  NumberBinding height = media.heightProperty().divide(ratio);
                  NumberBinding x = Bindings.subtract(MediaFxBackground.this.widthProperty(), width).divide(2.0D);
                  NumberBinding y = Bindings.subtract(MediaFxBackground.this.heightProperty(), height).divide(2.0D);
                  MediaFxBackground.this.view.layoutXProperty().bind(x);
                  MediaFxBackground.this.view.layoutYProperty().bind(y);
                  MediaFxBackground.this.view.fitWidthProperty().bind(width);
                  MediaFxBackground.this.view.fitHeightProperty().bind(height);
                  FadeTransition transition = new FadeTransition();
                  transition.setNode(MediaFxBackground.this.rect);
                  transition.setDuration(Duration.millis(250.0D));
                  transition.setToValue(0.0D);
                  transition.play();
                  player.play();
               }
            }
         });
         if (player.getStatus() == Status.HALTED) {
            this.log("Could not load media:", player.getError());
         }

      }
   }

   private void log(Object... o) {
      U.log("[MediaFxBackground]", o);
   }
}
