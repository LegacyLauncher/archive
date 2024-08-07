package net.legacylauncher.ui.swing.editor;

import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.util.SwingUtil;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ExtendedHTMLEditorKit extends HTMLEditorKit {
    public static final ExtendedHTMLEditorKit.ExtendedHTMLFactory extendedFactory = new ExtendedHTMLEditorKit.ExtendedHTMLFactory();
    public final ExtendedHTMLEditorKit.ExtendedLinkController linkController = new ExtendedHTMLEditorKit.ExtendedLinkController();
    private HyperlinkProcessor hlProc;
    private boolean processPopup;
    private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private String popupHref;
    private final JPopupMenu popup;

    public ExtendedHTMLEditorKit() {
        hlProc = HyperlinkProcessor.defaultProcessor;
        processPopup = true;
        popup = new JPopupMenu();
        LocalizableMenuItem copy = new LocalizableMenuItem("browser.hyperlink.popup.copy");
        copy.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(popupHref), null));
        popup.add(copy);
        LocalizableMenuItem show = new LocalizableMenuItem("browser.hyperlink.popup.show");
        show.addActionListener(e -> Alert.showMessage(Localizable.get("browser.hyperlink.popup.show.alert.title"), "", popupHref));
        popup.add(show);
    }

    public ViewFactory getViewFactory() {
        return extendedFactory;
    }

    public void install(JEditorPane pane) {
        super.install(pane);

        for (MouseListener listener : pane.getMouseListeners()) {
            if (listener instanceof LinkController || listener instanceof ExtendedLinkController) {
                pane.removeMouseListener(listener);
                pane.removeMouseMotionListener((MouseMotionListener) listener);
            }
        }

        pane.addMouseListener(linkController);
        pane.addMouseMotionListener(linkController);
    }

    @Override
    public void deinstall(JEditorPane c) {
        c.removeMouseListener(linkController);
        c.removeMouseMotionListener(linkController);
    }

    public final HyperlinkProcessor getHyperlinkProcessor() {
        return hlProc;
    }

    public final void setHyperlinkProcessor(HyperlinkProcessor processor) {
        hlProc = processor == null ? HyperlinkProcessor.defaultProcessor : processor;
    }

    public final boolean getProcessPopup() {
        return processPopup;
    }

    public final void setProcessPopup(boolean process) {
        processPopup = process;
    }

    public void updateUI() {
        SwingUtil.updateUIContainer(popup);
    }

    private static Tag getTag(Element elem) {
        AttributeSet attrs = elem.getAttributes();
        Object elementName = attrs.getAttribute("$ename");
        Object o = elementName != null ? null : attrs.getAttribute(StyleConstants.NameAttribute);
        return o instanceof Tag ? (Tag) o : null;
    }

    private static String getAnchorHref(MouseEvent e) {
        JEditorPane editor = (JEditorPane) e.getSource();
        if (editor.getDocument() instanceof HTMLDocument) {
            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element elem = doc.getCharacterElement(editor.viewToModel(e.getPoint()));
            Tag tag = getTag(elem);
            if (tag == Tag.CONTENT) {
                Object anchorAttr = elem.getAttributes().getAttribute(Tag.A);
                if (anchorAttr instanceof AttributeSet) {
                    AttributeSet anchor = (AttributeSet) anchorAttr;
                    Object hrefObject = anchor.getAttribute(Attribute.HREF);
                    if (hrefObject instanceof String) {
                        return (String) hrefObject;
                    }
                }
            }
        }
        return null;
    }

    public static class ExtendedHTMLFactory extends HTMLFactory {
        public View create(Element elem) {
            Tag kind = ExtendedHTMLEditorKit.getTag(elem);
            return kind == Tag.IMG ? new ExtendedImageView(elem) : super.create(elem);
        }
    }

    public class ExtendedLinkController extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (editor.isEnabled() || editor.isDisplayable()) {
                String href = ExtendedHTMLEditorKit.getAnchorHref(e);
                if (href != null) {
                    JPopupMenu menu = null;
                    switch (e.getButton()) {
                        case 3:
                            if (processPopup) {
                                popupHref = href;
                                menu = popup;
                            }
                            break;
                        default:
                            menu = hlProc.process(href);
                    }

                    if (menu != null)
                        menu.show(editor, e.getX(), e.getY());
                }
            }
        }

        public void mouseMoved(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (editor.isEnabled() || editor.isDisplayable()) {
                editor.setCursor(ExtendedHTMLEditorKit.getAnchorHref(e) == null ? Cursor.getDefaultCursor() : ExtendedHTMLEditorKit.HAND);
            }
        }

        public void mouseExited(MouseEvent e) {
            JEditorPane editor = (JEditorPane) e.getSource();
            if (editor.isEnabled() || editor.isDisplayable()) {
                editor.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
}
