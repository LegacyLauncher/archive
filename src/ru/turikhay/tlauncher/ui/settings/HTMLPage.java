package ru.turikhay.tlauncher.ui.settings;

import java.awt.Color;
import java.io.IOException;
import java.io.StringReader;
import javax.swing.JLabel;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

public class HTMLPage extends BorderPanel implements LocalizableComponent {
   private final String textColor;
   private final HTMLPage.AboutPageTokenResolver resolver;
   private final String source;
   private final EditorPane editor;

   HTMLPage(String resourceName) {
      Color color = (new JLabel()).getForeground();
      this.textColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
      color = null;

      String tempSource;
      try {
         tempSource = FileUtil.getResource(this.getClass().getResource(resourceName));
      } catch (Exception var4) {
         U.log(var4);
         tempSource = null;
      }

      this.source = tempSource;
      this.resolver = new HTMLPage.AboutPageTokenResolver();
      this.editor = new EditorPane();
      if (!OS.WINDOWS.isCurrent()) {
         this.editor.setMargin(new MagnifiedInsets(10, 0, 5, 0));
      }

      this.updateLocale();
      this.setCenter(this.editor);
   }

   public void updateLocale() {
      if (this.source != null) {
         StringBuilder string = new StringBuilder();
         TokenReplacingReader replacer = new TokenReplacingReader(new StringReader(this.source), this.resolver);

         label62: {
            try {
               while(true) {
                  int read;
                  if ((read = replacer.read()) <= 0) {
                     break label62;
                  }

                  string.append((char)read);
               }
            } catch (IOException var7) {
               var7.printStackTrace();
            } finally {
               U.close(replacer);
            }

            return;
         }

         this.editor.setText(string.toString());
      }

   }

   private class AboutPageTokenResolver implements ITokenResolver {
      private AboutPageTokenResolver() {
      }

      public String resolveToken(String token) {
         if (token.equals("width")) {
            return String.valueOf(SwingUtil.magnify(425));
         } else {
            return token.startsWith("image:") ? Images.getRes(token.substring("image:".length())).toExternalForm() : (token.startsWith("loc:") ? Localizable.get(token.substring("loc:".length())) : (token.equals("color") ? HTMLPage.this.textColor : token));
         }
      }

      // $FF: synthetic method
      AboutPageTokenResolver(Object x1) {
         this();
      }
   }
}
