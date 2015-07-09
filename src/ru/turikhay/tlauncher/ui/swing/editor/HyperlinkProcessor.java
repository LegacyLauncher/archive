package ru.turikhay.tlauncher.ui.swing.editor;

import java.net.URI;
import java.net.URISyntaxException;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.OS;

public abstract class HyperlinkProcessor {
   public static final HyperlinkProcessor defaultProcessor = new HyperlinkProcessor() {
      public void process(String link) {
         if (link != null) {
            URI uri;
            try {
               uri = new URI(link);
               uri.toURL();
            } catch (Exception var6) {
               try {
                  uri = new URI("http://" + link);
               } catch (URISyntaxException var5) {
                  Alert.showLocError("browser.hyperlink.create.error", var6);
                  return;
               }
            }

            OS.openLink(uri);
         }
      }
   };

   public abstract void process(String var1);
}
