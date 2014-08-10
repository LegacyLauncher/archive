package ru.turikhay.tlauncher.ui.swing.editor;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;

public class ExtendedHTMLEditorKit extends HTMLEditorKit {
   protected static final ExtendedHTMLEditorKit.ExtendedHTMLFactory extendedFactory = new ExtendedHTMLEditorKit.ExtendedHTMLFactory();

   public ViewFactory getViewFactory() {
      return extendedFactory;
   }

   public static class ExtendedHTMLFactory extends HTMLFactory {
      public View create(Element elem) {
         View view = super.create(elem);
         return (View)(!(view instanceof ImageView) ? view : new ExtendedImageView(elem));
      }
   }
}
