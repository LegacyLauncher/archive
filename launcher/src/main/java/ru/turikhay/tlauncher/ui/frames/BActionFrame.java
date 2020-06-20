package ru.turikhay.tlauncher.ui.frames;

import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import java.awt.*;

public class BActionFrame extends ActionFrame {
    private final LocalizableLabel head;
    private final BorderPanel body;
    private final ExtendedPanel footer;

    public BActionFrame(Insets insets) {
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        setType(Type.UTILITY);

        head = new LocalizableLabel();
        body = new BorderPanel();
        body.setHgap(SwingUtil.magnify(5));
        body.setVgap(SwingUtil.magnify(5));
        footer = new ExtendedPanel();

        BorderPanel holder = new BorderPanel();
        holder.setHgap(SwingUtil.magnify(10));
        holder.setVgap(SwingUtil.magnify(10));

        holder.setNorth(head);
        holder.setCenter(body);
        holder.setSouth(footer);

        ExtendedPanel centerPanel = new ExtendedPanel();
        centerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = SwingUtil.magnify(new Insets(15, 15, 15, 15));
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;

        centerPanel.add(holder, c);
        add(centerPanel);

        pack();
    }

    public BActionFrame() {
        this(null);
    }

    @Override
    public LocalizableLabel getHead() {
        return head;
    }

    @Override
    public BorderPanel getBody() {
        return body;
    }

    @Override
    public ExtendedPanel getFooter() {
        return footer;
    }
}
