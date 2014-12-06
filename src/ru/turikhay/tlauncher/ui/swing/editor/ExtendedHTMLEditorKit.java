package ru.turikhay.tlauncher.ui.swing.editor;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JPopupMenu;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.OS;

public class ExtendedHTMLEditorKit extends HTMLEditorKit {
	protected final static ExtendedHTMLFactory extendedFactory = new ExtendedHTMLFactory();

	public final static HyperlinkProcessor defaultHyperlinkProcessor = new HyperlinkProcessor() {
		@Override
		public void process(String link) {
			if(link == null)
				return;

			URI uri;

			try {
				uri = new URI(link);
			} catch (URISyntaxException e) {
				Alert.showLocError("browser.hyperlink.create.error", e);
				return;
			}

			OS.openLink(uri);
		}
	};

	@Override
	public ViewFactory getViewFactory() {
		return extendedFactory;
	}

	public static class ExtendedHTMLFactory extends HTMLFactory {
		@Override
		public View create(Element elem) {
			HTML.Tag kind = getTag(elem);

			if(kind == HTML.Tag.IMG) {
				return new ExtendedImageView(elem);
			}

			return super.create(elem);
		}
	}

	protected final ExtendedLinkController linkController = new ExtendedLinkController();

	@Override
	public void install(JEditorPane pane) {
		super.install(pane);

		for(MouseListener listener : pane.getMouseListeners())
			if(listener instanceof LinkController) {
				pane.removeMouseListener(listener);
				pane.removeMouseMotionListener((MouseMotionListener) listener);

				pane.addMouseListener(linkController);
				pane.addMouseMotionListener(linkController);
			}
	}

	private HyperlinkProcessor hlProc = defaultHyperlinkProcessor;

	public final HyperlinkProcessor getHyperlinkProcessor() {
		return hlProc;
	}

	public final void setHyperlinkProcessor(HyperlinkProcessor processor) {
		this.hlProc = (processor == null)? defaultHyperlinkProcessor : processor;
	}

	private boolean processPopup = true;

	public final boolean getProcessPopup() {
		return processPopup;
	}

	public final void setProcessPopup(boolean process) {
		this.processPopup = process;
	}

	private static final Cursor HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	private String popupHref;
	private final JPopupMenu popup = new JPopupMenu(); {
		LocalizableMenuItem open, copy, show;

		open = new LocalizableMenuItem("browser.hyperlink.popup.open");
		open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hlProc.process(popupHref);
			}
		});
		popup.add(open);

		copy = new LocalizableMenuItem("browser.hyperlink.popup.copy");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(popupHref), null);
			}
		});
		popup.add(copy);

		show = new LocalizableMenuItem("browser.hyperlink.popup.show");
		show.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Alert.showLocMessage("browser.hyperlink.popup.show.alert", popupHref);
			}
		});
		popup.add(show);
	}

	public class ExtendedLinkController extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			JEditorPane editor = (JEditorPane) e.getSource();

			if(!(editor.isEnabled() || editor.isDisplayable()))
				return;

			String href = getAnchorHref(e);

			if(href == null)
				return;

			switch(e.getButton()) {
			case MouseEvent.BUTTON3:

				if(processPopup) {
					popupHref = href;
					popup.show(editor, e.getX(), e.getY());
				}

				break;
			default: hlProc.process(href);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			JEditorPane editor = (JEditorPane) e.getSource();

			if(!(editor.isEnabled() || editor.isDisplayable()))
				return;

			editor.setCursor(getAnchorHref(e) == null? Cursor.getDefaultCursor() : HAND);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			JEditorPane editor = (JEditorPane) e.getSource();

			if(!(editor.isEnabled() || editor.isDisplayable()))
				return;

			editor.setCursor(Cursor.getDefaultCursor());
		}
	}

	private static HTML.Tag getTag(Element elem) {
		AttributeSet attrs = elem.getAttributes();
		Object elementName = attrs.getAttribute(AbstractDocument.ElementNameAttribute);
		Object o = (elementName != null) ? null : attrs.getAttribute(StyleConstants.NameAttribute);

		return (o instanceof HTML.Tag)? (HTML.Tag) o : null;
	}

	private static String getAnchorHref(MouseEvent e) {
		JEditorPane editor = (JEditorPane) e.getSource();

		if(!(editor.getDocument() instanceof HTMLDocument))
			return null;

		HTMLDocument hdoc = (HTMLDocument) editor.getDocument();
		Element elem = hdoc.getCharacterElement(editor.viewToModel(e.getPoint()));

		HTML.Tag tag = getTag(elem);

		if(tag == HTML.Tag.CONTENT) {
			Object anchorAttr = elem.getAttributes().getAttribute(HTML.Tag.A);

			if(anchorAttr != null && anchorAttr instanceof AttributeSet) {
				AttributeSet anchor = (AttributeSet) anchorAttr;
				Object hrefObject = anchor.getAttribute(HTML.Attribute.HREF);

				if(hrefObject != null && hrefObject instanceof String)
					return (String) hrefObject;
			}
		}

		return null;
	}
}
