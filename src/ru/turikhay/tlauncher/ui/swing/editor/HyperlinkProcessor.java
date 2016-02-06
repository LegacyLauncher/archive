package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.OS;

public abstract class HyperlinkProcessor {
   public static final HyperlinkProcessor defaultProcessor = new HyperlinkProcessor() {
      protected JPopupMenu popup;

      public JPopupMenu process(String link) {
         if (link == null) {
            return null;
         } else if (link.startsWith("[") && link.endsWith("]")) {
            if (this.popup == null) {
               this.popup = new JPopupMenu();
            } else {
               this.popup.removeAll();
            }

            String[] links = StringUtils.split(link.substring(1, link.length() - 1), ';');
            if (links.length == 0) {
               return null;
            } else {
               this.popup.add(this.newItem(links[0], "<html><b>" + links[0] + "</b></html>"));
               this.popup.addSeparator();

               for(int i = 1; i < links.length; ++i) {
                  this.popup.add(this.newItem(links[i]));
               }

               return this.popup;
            }
         } else {
            URI uri;
            try {
               uri = new URI(link);
               uri.toURL();
            } catch (Exception var6) {
               try {
                  uri = new URI("http://" + link);
               } catch (URISyntaxException var5) {
                  Alert.showLocError("browser.hyperlink.create.error", var6);
                  return null;
               }
            }

            OS.openLink(uri);
            return null;
         }
      }

      protected final JMenuItem newItem(String link) {
         return this.newItem(link, (String)null);
      }

      protected JMenuItem newItem(String link, String name) {
         if (name == null) {
            if (link.length() > 30) {
               name = link.substring(0, 30) + "...";
            } else {
               name = link;
            }
         }

         JMenuItem item = new JMenuItem(name);

         final URI _uri;
         try {
            _uri = new URI(link);
            _uri.toURL();
         } catch (Exception var8) {
            try {
               _uri = new URI("http://" + link);
            } catch (URISyntaxException var7) {
               _uri = null;
            }
         }

         item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               if (_uri == null) {
                  Alert.showLocError("browser.hyperlink.create.error");
               }

               OS.openLink(_uri);
            }
         });
         return item;
      }
   };

   public abstract JPopupMenu process(String var1);
}
