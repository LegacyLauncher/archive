package ru.turikhay.tlauncher.ui.settings;

import java.io.IOException;
import java.io.StringReader;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

public class HTMLPage extends BorderPanel implements LocalizableComponent {
   private final HTMLPage.AboutPageTokenResolver resolver;
   private final String source;
   private final EditorPane editor;

   HTMLPage(String resourceName) {
      String tempSource;
      try {
         tempSource = FileUtil.getResource(this.getClass().getResource(resourceName));
      } catch (Exception var4) {
         U.log(var4);
         tempSource = null;
      }

      this.source = tempSource;
      this.resolver = new HTMLPage.AboutPageTokenResolver((HTMLPage.AboutPageTokenResolver)null);
      this.editor = new EditorPane();
      this.updateLocale();
      this.setCenter(this.editor);
   }

   public EditorPane getEditor() {
      return this.editor;
   }

   public String getSource() {
      return this.source;
   }

   public void updateLocale() {
      if (this.source != null) {
         StringBuilder string = new StringBuilder();
         TokenReplacingReader replacer = new TokenReplacingReader(new StringReader(this.source), this.resolver);

         label54: {
            try {
               while(true) {
                  int read;
                  if ((read = replacer.read()) <= 0) {
                     break label54;
                  }

                  string.append((char)read);
               }
            } catch (IOException var8) {
               var8.printStackTrace();
            } finally {
               U.close(replacer);
            }

            return;
         }

         this.editor.setText(string.toString());
      }
   }

   private class AboutPageTokenResolver implements ITokenResolver {
      private static final String image = "image:";
      private static final String loc = "loc:";
      private static final String color = "color";

      private AboutPageTokenResolver() {
      }

      public String resolveToken(String token) {
         if (token.startsWith("image:")) {
            return Images.getRes(token.substring("image:".length())).toExternalForm();
         } else if (token.startsWith("loc:")) {
            return Localizable.get(token.substring("loc:".length()));
         } else {
            return token.equals("color") ? "black" : token;
         }
      }

      // $FF: synthetic method
      AboutPageTokenResolver(HTMLPage.AboutPageTokenResolver var2) {
         this();
      }
   }
}
