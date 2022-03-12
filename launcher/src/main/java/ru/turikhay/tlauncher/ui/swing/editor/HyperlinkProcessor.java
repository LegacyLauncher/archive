package ru.turikhay.tlauncher.ui.swing.editor;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.OS;

import javax.swing.*;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class HyperlinkProcessor {
    public static final HyperlinkProcessor defaultProcessor = new HyperlinkProcessor() {
        private JPopupMenu popup;

        public JPopupMenu process(String link) {
            if (link == null) {
                return null;
            }

            if (link.startsWith("[") && link.endsWith("]")) {
                if (popup == null) {
                    popup = new JPopupMenu();
                } else {
                    popup.removeAll();
                }

                String[] links = StringUtils.split(link.substring(1, link.length() - 1), ';');

                if (links.length == 0)
                    return null;

                popup.add(newItem(links[0], "<html><b>" + links[0] + "</b></html>"));
                popup.addSeparator();

                for (int i = 1; i < links.length; i++) {
                    popup.add(newItem(links[i]));
                }

                return popup;
            }

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

        private JMenuItem newItem(final String link) {
            return newItem(link, null);
        }

        private JMenuItem newItem(final String link, String name) {
            if (name == null) {
                if (link.length() > 30) {
                    name = link.substring(0, 30) + "...";
                } else {
                    name = link;
                }
            }
            JMenuItem item = new JMenuItem(name);

            URI _uri;

            try {
                _uri = new URI(link);
                _uri.toURL();
            } catch (Exception var6) {
                try {
                    _uri = new URI("http://" + link);
                } catch (URISyntaxException var5) {
                    _uri = null;
                }
            }

            final URI uri = _uri;

            item.addActionListener(e -> {
                if (uri == null) {
                    Alert.showLocError("browser.hyperlink.create.error");
                }
                OS.openLink(uri);
            });

            return item;
        }
    };

    public abstract JPopupMenu process(String link);
}
