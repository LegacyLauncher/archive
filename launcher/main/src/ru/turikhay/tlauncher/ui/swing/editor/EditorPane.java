package ru.turikhay.tlauncher.ui.swing.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class EditorPane extends JEditorPane {
    private final ExtendedHTMLEditorKit html;

    public EditorPane(Font font) {
        html = new ExtendedHTMLEditorKit();

        if (font == null) {
            setFont(new ExtendedLabel().getFont());
        } else {
            setFont(font);
        }

        getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        setMargin(new Insets(0, 0, 0, 0));

        setEditorKit(html);
        setEditable(false);
        setOpaque(false);

        addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(EventType.ACTIVATED)) {
                    URL url = e.getURL();
                    if (url != null) {
                        OS.openLink(url);
                    }
                }
            }
        });
    }

    public EditorPane() {
        this((new LocalizableLabel()).getFont());
    }

    public EditorPane(URL initialPage) throws IOException {
        this();
        setPage(initialPage);
    }

    public EditorPane(String url) throws IOException {
        this();
        setPage(url);
    }

    public EditorPane(String type, String text) {
        this();
        setContentType(type);
        setText(text);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);

        if(html != null) {
            Color color = Theme.getTheme().getForeground();
            final String textColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

            font = getFont();

            StyleSheet css = new StyleSheet();
            css.importStyleSheet(getClass().getResource("styles.css"));
            css.addRule("body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: " + textColor + "; } " + "a { color: " + textColor + "; text-decoration: underline; }");

            html.setStyleSheet(css);
            setEditorKit(html);
        }
    }
}
