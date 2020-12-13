package ru.turikhay.tlauncher.bootstrap.ui.swing;

import ru.turikhay.tlauncher.bootstrap.util.OS;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

public class HTMLKitLinkListener extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {
        JEditorPane editor = (JEditorPane) e.getSource();
        if (editor.isEnabled() || editor.isDisplayable()) {
            String href = getAnchorHref(e);
            if (href != null && e.getButton() == MouseEvent.BUTTON1) {
                openLink(href);
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        JEditorPane editor = (JEditorPane) e.getSource();
        if (editor.isEnabled() || editor.isDisplayable()) {
            editor.setCursor(getAnchorHref(e) == null ? Cursor.getDefaultCursor() : HAND);
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        JEditorPane editor = (JEditorPane) e.getSource();
        if (editor.isEnabled() || editor.isDisplayable()) {
            editor.setCursor(Cursor.getDefaultCursor());
        }
    }

    private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    private static void openLink(String href) {
        URL url;
        try {
            url = new URL(href);
        } catch (MalformedURLException ignored) {
            return;
        }
        OS.openUrl(url);
    }

    private static String getAnchorHref(MouseEvent e) {
        JEditorPane editor = (JEditorPane) e.getSource();
        if (editor.getDocument() instanceof HTMLDocument) {
            HTMLDocument htmlDocument = (HTMLDocument) editor.getDocument();
            Element elem = htmlDocument.getCharacterElement(editor.viewToModel(e.getPoint()));
            HTML.Tag tag = getTag(elem);
            if (tag == HTML.Tag.CONTENT) {
                Object anchorAttr = elem.getAttributes().getAttribute(HTML.Tag.A);
                if (anchorAttr instanceof AttributeSet) {
                    AttributeSet anchor = (AttributeSet) anchorAttr;
                    Object hrefObject = anchor.getAttribute(HTML.Attribute.HREF);
                    if (hrefObject instanceof String) {
                        return (String) hrefObject;
                    }
                }
            }
        }
        return null;
    }

    private static HTML.Tag getTag(Element elem) {
        AttributeSet attrs = elem.getAttributes();
        Object elementName = attrs.getAttribute("$ename");
        Object o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
        return o instanceof HTML.Tag ? (HTML.Tag) o : null;
    }
}
