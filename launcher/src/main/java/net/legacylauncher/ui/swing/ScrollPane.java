package net.legacylauncher.ui.swing;

import javax.swing.*;
import java.awt.*;

public class ScrollPane extends JScrollPane {
    private static final boolean DEFAULT_BORDER = false;

    public ScrollPane(Component view, ScrollPane.ScrollBarPolicy vertical, ScrollPane.ScrollBarPolicy horizontal, boolean border) {
        super(view);
        setOpaque(false);
        getViewport().setOpaque(false);
        if (!border) {
            setBorder(null);
        }

        setVBPolicy(vertical);
        setHBPolicy(horizontal);
    }

    public ScrollPane(Component view, ScrollPane.ScrollBarPolicy vertical, ScrollPane.ScrollBarPolicy horizontal) {
        this(view, vertical, horizontal, false);
    }

    public ScrollPane(Component view, ScrollPane.ScrollBarPolicy generalPolicy, boolean border) {
        this(view, generalPolicy, generalPolicy, border);
    }

    public ScrollPane(Component view, ScrollPane.ScrollBarPolicy generalPolicy) {
        this(view, generalPolicy, generalPolicy);
    }

    public ScrollPane(Component view, boolean border) {
        this(view, ScrollPane.ScrollBarPolicy.AS_NEEDED, border);
    }

    public ScrollPane(Component view) {
        this(view, ScrollPane.ScrollBarPolicy.AS_NEEDED);
    }

    public void setVerticalScrollBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        int i_policy;
        switch (policy) {
            case ALWAYS:
                i_policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
                break;
            case AS_NEEDED:
                i_policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
                break;
            case NEVER:
                i_policy = ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER;
                break;
            default:
                throw new IllegalArgumentException();
        }

        setVerticalScrollBarPolicy(i_policy);
    }

    public void setHorizontalScrollBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        int i_policy;
        switch (policy) {
            case ALWAYS:
                i_policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS;
                break;
            case AS_NEEDED:
                i_policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
                break;
            case NEVER:
                i_policy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
                break;
            default:
                throw new IllegalArgumentException();
        }

        setHorizontalScrollBarPolicy(i_policy);
    }

    public void setVBPolicy(ScrollPane.ScrollBarPolicy policy) {
        setVerticalScrollBarPolicy(policy);
    }

    public void setHBPolicy(ScrollPane.ScrollBarPolicy policy) {
        setHorizontalScrollBarPolicy(policy);
    }

    public enum ScrollBarPolicy {
        ALWAYS,
        AS_NEEDED,
        NEVER
    }
}
