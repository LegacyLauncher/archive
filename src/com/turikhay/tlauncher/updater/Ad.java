package com.turikhay.tlauncher.updater;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.util.U;
import java.net.URI;
import net.minecraft.launcher_.OperatingSystem;

public class Ad {
   private int id;
   private String title;
   private String text;
   private String textarea;
   private URI uri;
   private boolean shown;

   private Ad(int id, String title, String text, String textarea, URI uri) {
      this.id = id;
      this.title = title;
      this.text = text;
      this.textarea = textarea;
      this.uri = uri;
   }

   Ad(int id, String title, String text, String textarea) {
      this.id = id;
      this.title = title;
      this.text = text;
      this.textarea = textarea;
   }

   Ad(int id, String title, String text, String textarea, String uri) {
      this(id, title, text, textarea, U.makeURI(uri));
   }

   Ad(Settings settings) {
      if (settings == null) {
         throw new NullPointerException("Settings is NULL!");
      } else {
         this.id = settings.getInteger("ad.id");
         this.title = settings.nget("ad.title");
         this.text = settings.nget("ad.text");
         this.textarea = settings.nget("ad.textarea");
         this.uri = U.makeURI(settings.nget("ad.url"));
      }
   }

   public int getID() {
      return this.id;
   }

   public String getTitle() {
      return this.title;
   }

   public String getText() {
      return this.text;
   }

   public String getTextarea() {
      return this.textarea;
   }

   public URI getURI() {
      return this.uri;
   }

   public boolean canBeShown() {
      return !this.shown && this.id != 0;
   }

   public void show(boolean force) {
      if (!this.shown) {
         this.shown = true;
         if (this.uri != null) {
            if (Alert.showQuestion(this.title, this.text, this.textarea, force)) {
               OperatingSystem.openLink(this.uri);
            }
         } else {
            Alert.showMessage(this.title, this.text, this.textarea);
         }

      }
   }
}
