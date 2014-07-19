package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.Font;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import ru.turikhay.util.OS;

public class EditorPane extends JEditorPane {
	private static final long serialVersionUID = -2857352867725574106L;
	
    public EditorPane() {    	
		Font font = new ExtendedLabel().getFont();

		StyleSheet css = new StyleSheet();
		css.importStyleSheet(getClass().getResource("EditorPane.css"));
		css.addRule("body { font-family: " + font.getFamily() + "; font-size: "
				+ font.getSize() + "pt; }");

		HTMLEditorKit html = new HTMLEditorKit();
		html.setStyleSheet(css);

		getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		setMargin(new Insets(0, 0, 0, 0));
		setEditorKit(html);
		setEditable(false);
		setOpaque(false);

		addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (!e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					return;

				URL url = e.getURL();

				if (url == null)
					return;

				OS.openLink(url);
			}
		});
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

}
