package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit.HTMLFactory;
import javax.swing.text.html.HTMLEditorKit.LinkController;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;

public class ExtendedHTMLEditorKit extends HTMLEditorKit {
   protected static final ExtendedHTMLEditorKit.ExtendedHTMLFactory extendedFactory = new ExtendedHTMLEditorKit.ExtendedHTMLFactory();
   protected final ExtendedHTMLEditorKit.ExtendedLinkController linkController = new ExtendedHTMLEditorKit.ExtendedLinkController();
   private HyperlinkProcessor hlProc;
   private boolean processPopup;
   private static final Cursor HAND = Cursor.getPredefinedCursor(12);
   private String popupHref;
   private final JPopupMenu popup;

   public ExtendedHTMLEditorKit() {
      this.hlProc = HyperlinkProcessor.defaultProcessor;
      this.processPopup = true;
      this.popup = new JPopupMenu();
      LocalizableMenuItem open = new LocalizableMenuItem("browser.hyperlink.popup.open");
      open.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            ExtendedHTMLEditorKit.this.hlProc.process(ExtendedHTMLEditorKit.this.popupHref);
         }
      });
      this.popup.add(open);
      LocalizableMenuItem copy = new LocalizableMenuItem("browser.hyperlink.popup.copy");
      copy.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ExtendedHTMLEditorKit.this.popupHref), (ClipboardOwner)null);
         }
      });
      this.popup.add(copy);
      LocalizableMenuItem show = new LocalizableMenuItem("browser.hyperlink.popup.show");
      show.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            Alert.showLocMessage("browser.hyperlink.popup.show.alert", ExtendedHTMLEditorKit.this.popupHref);
         }
      });
      this.popup.add(show);
   }

   public ViewFactory getViewFactory() {
      return extendedFactory;
   }

   public void install(JEditorPane pane) {
      super.install(pane);
      MouseListener[] var5;
      int var4 = (var5 = pane.getMouseListeners()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         MouseListener listener = var5[var3];
         if (listener instanceof LinkController) {
            pane.removeMouseListener(listener);
            pane.removeMouseMotionListener((MouseMotionListener)listener);
            pane.addMouseListener(this.linkController);
            pane.addMouseMotionListener(this.linkController);
         }
      }

   }

   public final HyperlinkProcessor getHyperlinkProcessor() {
      return this.hlProc;
   }

   public final void setHyperlinkProcessor(HyperlinkProcessor processor) {
      this.hlProc = processor == null ? HyperlinkProcessor.defaultProcessor : processor;
   }

   public final boolean getProcessPopup() {
      return this.processPopup;
   }

   public final void setProcessPopup(boolean process) {
      this.processPopup = process;
   }

   private static Tag getTag(Element elem) {
      AttributeSet attrs = elem.getAttributes();
      Object elementName = attrs.getAttribute("$ename");
      Object o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
      return o instanceof Tag ? (Tag)o : null;
   }

   private static String getAnchorHref(MouseEvent e) {
      JEditorPane editor = (JEditorPane)e.getSource();
      if (!(editor.getDocument() instanceof HTMLDocument)) {
         return null;
      } else {
         HTMLDocument hdoc = (HTMLDocument)editor.getDocument();
         Element elem = hdoc.getCharacterElement(editor.viewToModel(e.getPoint()));
         Tag tag = getTag(elem);
         if (tag == Tag.CONTENT) {
            Object anchorAttr = elem.getAttributes().getAttribute(Tag.A);
            if (anchorAttr != null && anchorAttr instanceof AttributeSet) {
               AttributeSet anchor = (AttributeSet)anchorAttr;
               Object hrefObject = anchor.getAttribute(Attribute.HREF);
               if (hrefObject != null && hrefObject instanceof String) {
                  return (String)hrefObject;
               }
            }
         }

         return null;
      }
   }

   public class ExtendedLinkController extends MouseAdapter {
      public void mouseClicked(MouseEvent e) {
         JEditorPane editor = (JEditorPane)e.getSource();
         if (editor.isEnabled() || editor.isDisplayable()) {
            String href = ExtendedHTMLEditorKit.getAnchorHref(e);
            if (href != null) {
               switch(e.getButton()) {
               case 3:
                  if (ExtendedHTMLEditorKit.this.processPopup) {
                     ExtendedHTMLEditorKit.this.popupHref = href;
                     ExtendedHTMLEditorKit.this.popup.show(editor, e.getX(), e.getY());
                  }
                  break;
               default:
                  ExtendedHTMLEditorKit.this.hlProc.process(href);
               }
            }
         }

      }

      public void mouseMoved(MouseEvent e) {
         JEditorPane editor = (JEditorPane)e.getSource();
         if (editor.isEnabled() || editor.isDisplayable()) {
            editor.setCursor(ExtendedHTMLEditorKit.getAnchorHref(e) == null ? Cursor.getDefaultCursor() : ExtendedHTMLEditorKit.HAND);
         }

      }

      public void mouseExited(MouseEvent e) {
         JEditorPane editor = (JEditorPane)e.getSource();
         if (editor.isEnabled() || editor.isDisplayable()) {
            editor.setCursor(Cursor.getDefaultCursor());
         }

      }
   }

   public static class ExtendedHTMLFactory extends HTMLFactory {
      public View create(Element elem) {
         Tag kind = ExtendedHTMLEditorKit.getTag(elem);
         return (View)(kind == Tag.IMG ? new ExtendedImageView(elem) : super.create(elem));
      }
   }
}
