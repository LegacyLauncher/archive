package net.legacylauncher.ui.swing.editor;

import net.legacylauncher.ui.loc.LocalizableLabel;
import net.legacylauncher.ui.swing.extended.ExtendedLabel;
import net.legacylauncher.ui.theme.Theme;

import javax.swing.*;
import javax.swing.text.html.StyleSheet;
import java.awt.*;

public class EditorPane extends JEditorPane {
    private final boolean ready;
    private ExtendedHTMLEditorKit html;

    public EditorPane(Font font) {
        if (font == null) {
            super.setFont(new ExtendedLabel().getFont());
        } else {
            super.setFont(font);
        }
        getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        setMargin(new Insets(0, 0, 0, 0));
        setEditable(false);
        setOpaque(false);
        updateHtml();
        this.ready = true;
    }

    public EditorPane() {
        this((new LocalizableLabel()).getFont());
    }

    public EditorPane(String type, String text) {
        this();
        setContentType(type);
        setText(text);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        updateHtml();
    }

    private String cachedText;

    @Override
    public void setText(String text) {
        super.setText(text);
        this.cachedText = text;
    }

    @Override
    public void updateUI() {
        if (ready) {
            updateHtml();
            if (html != null) {
                html.updateUI();
            }
        }
        super.updateUI();
    }

    private void updateHtml() {
        Color color = Theme.getTheme().getForeground();
        final String textColor = String.format(java.util.Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());

        Font font = getFont();

        StyleSheet css = new StyleSheet();
        css.importStyleSheet(getClass().getResource("styles.css"));
        css.addRule("body {" +
                "font-family: " + font.getFamily() + ";" +
                "font-size: " + font.getSize() + "pt;" +
                "color: " + textColor + ";" +
                "} " +
                "a {" +
                "color: " + textColor + ";" +
                "text-decoration: underline;" +
                "}"
        );

        html = new ExtendedHTMLEditorKit();
        html.setStyleSheet(css);
        setEditorKit(html);
        if (cachedText != null) {
            setText(cachedText);
        }
    }
}
