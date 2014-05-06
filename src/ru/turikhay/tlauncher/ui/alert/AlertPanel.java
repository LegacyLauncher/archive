package ru.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.swing.extended.EditorPane;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

class AlertPanel extends JPanel {
	private static final long serialVersionUID = -8032765825488193573L;
	private static final int MAX_CHARS_ON_LINE = 70, MAX_WIDTH = 500,
			MAX_HEIGHT = 300;

	AlertPanel(String message, Object textarea) {
		LayoutManager lm = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(lm);

		String textareaContent = textarea == null? null : U.w(U.toLog(textarea), MAX_CHARS_ON_LINE, true);
		String messageContent = message == null? null : "<html>" + U.w(message, MAX_CHARS_ON_LINE).replace("\n", "<br/>") + "<br/></html>";

		Dimension maxSize = new Dimension(MAX_WIDTH, MAX_HEIGHT);

		EditorPane label = new EditorPane("text/html", messageContent);
		label.setAlignmentX(LEFT_ALIGNMENT);
		add(label);

		if (textareaContent == null)
			return;

		JTextArea area = new JTextArea(textareaContent);
		area.setAlignmentX(LEFT_ALIGNMENT);
		area.setMaximumSize(maxSize);
		area.addMouseListener(new TextPopup());
		area.setFont(getFont());
		area.setEditable(false);

		JScrollPane scroll = new JScrollPane(area);
		scroll.setAlignmentX(LEFT_ALIGNMENT);
		scroll.setMaximumSize(maxSize);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		int textAreaHeight = StringUtil.countLines(textareaContent)
				* getFontMetrics(getFont()).getHeight();

		if (textAreaHeight > MAX_HEIGHT)
			scroll.setPreferredSize(maxSize);

		add(scroll);
	}
}
