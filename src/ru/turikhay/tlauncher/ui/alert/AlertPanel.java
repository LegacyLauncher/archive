package ru.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.ScrollPane.ScrollBarPolicy;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

class AlertPanel extends JPanel {
	private static final int MAX_CHARS = 80, MAX_WIDTH = 500, MAX_HEIGHT = 300;
	private static final Dimension MAX_SIZE = new Dimension(MAX_WIDTH, MAX_HEIGHT);

	AlertPanel(String rawMessage, Object rawTextarea) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		String message;

		if(rawMessage == null)
			message = null;
		else
			message = StringUtil.wrap(new StringBuilder()
			.append("<html>")
			.append(rawMessage)
			.append("</html>").toString(), MAX_CHARS);

		EditorPane label = new EditorPane("text/html", message);
		label.setAlignmentX(LEFT_ALIGNMENT);
		label.setFocusable(false);
		add(label);

		if (rawTextarea == null)
			return;

		String textarea = U.toLog(rawTextarea);

		JTextArea area = new JTextArea(textarea);
		area.addMouseListener(new TextPopup());
		area.setFont(getFont());
		area.setEditable(false);

		ScrollPane scroll = new ScrollPane(area, true);
		scroll.setAlignmentX(LEFT_ALIGNMENT);
		scroll.setVBPolicy(ScrollBarPolicy.AS_NEEDED);

		int textAreaHeight = StringUtil.countLines(textarea)
				* getFontMetrics(getFont()).getHeight();

		if (textAreaHeight > MAX_HEIGHT)
			scroll.setPreferredSize(MAX_SIZE);

		add(scroll);
	}
}
