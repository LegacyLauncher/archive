package ru.turikhay.tlauncher.ui.swing;

import javax.swing.*;
import java.awt.*;

public class ScrollPane extends JScrollPane {
    private static final boolean DEFAULT_BORDER = false;
    // $FF: synthetic field
    private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy;

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
        byte i_policy;
        switch ($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy()[policy.ordinal()]) {
            case 1:
                i_policy = 22;
                break;
            case 2:
                i_policy = 20;
                break;
            case 3:
                i_policy = 21;
                break;
            default:
                throw new IllegalArgumentException();
        }

        super.setVerticalScrollBarPolicy(i_policy);
    }

    public void setHorizontalScrollBarPolicy(ScrollPane.ScrollBarPolicy policy) {
        byte i_policy;
        switch ($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy()[policy.ordinal()]) {
            case 1:
                i_policy = 32;
                break;
            case 2:
                i_policy = 30;
                break;
            case 3:
                i_policy = 31;
                break;
            default:
                throw new IllegalArgumentException();
        }

        super.setHorizontalScrollBarPolicy(i_policy);
    }

    public void setVBPolicy(ScrollPane.ScrollBarPolicy policy) {
        setVerticalScrollBarPolicy(policy);
    }

    public void setHBPolicy(ScrollPane.ScrollBarPolicy policy) {
        setHorizontalScrollBarPolicy(policy);
    }

    // $FF: synthetic method
    static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy() {
        int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy;
        if ($SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy != null) {
            return var10000;
        } else {
            int[] var0 = new int[ScrollPane.ScrollBarPolicy.values().length];

            try {
                var0[ScrollPane.ScrollBarPolicy.ALWAYS.ordinal()] = 1;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[ScrollPane.ScrollBarPolicy.AS_NEEDED.ordinal()] = 2;
            } catch (NoSuchFieldError ignored) {
            }

            try {
                var0[ScrollPane.ScrollBarPolicy.NEVER.ordinal()] = 3;
            } catch (NoSuchFieldError ignored) {
            }

            $SWITCH_TABLE$ru$turikhay$tlauncher$ui$swing$ScrollPane$ScrollBarPolicy = var0;
            return var0;
        }
    }

    public enum ScrollBarPolicy {
        ALWAYS,
        AS_NEEDED,
        NEVER
    }
}
