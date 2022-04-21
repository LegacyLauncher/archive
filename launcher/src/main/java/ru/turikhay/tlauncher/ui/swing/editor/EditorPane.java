package ru.turikhay.tlauncher.ui.swing.editor;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
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

    public EditorPane(String text, int width) {
        this("text/html", text);
        setPreferredSize(new Dimension(width, SwingUtil.getPrefHeight(this, width)));
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);

        if (html != null) {
            Color color = Theme.getTheme().getForeground();
            final String textColor = String.format(java.util.Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

            font = getFont();

            StyleSheet css = new StyleSheet();
            css.importStyleSheet(getClass().getResource("styles.css"));
            css.addRule("body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; color: " + textColor + "; } " + "a { color: " + textColor + "; text-decoration: underline; }");

            html.setStyleSheet(css);
            setEditorKit(html);
        }
    }
}
