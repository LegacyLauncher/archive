package ru.turikhay.tlauncher.bootstrap.ui.swing;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class HTMLPane extends JEditorPane {
    public HTMLPane() {
        getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);

        setMargin(new Insets(0, 0, 0, 0));
        addMouseListener(new HTMLKitLinkListener());
        setEditorKit(new HTMLEditorKit());
        setEditable(false);
        setOpaque(false);

        setContentType("text/html");
    }
}
