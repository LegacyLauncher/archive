package ru.turikhay.tlauncher.bootstrap.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.turikhay.tlauncher.bootstrap.util.SplitArgs.splitArgs;

class SplitArgsTest {

    @Test
    void bothSplitTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{"--bfoo", "--bbar"},
                        new String[]{"--lfoo", "--lbar"}
                ),
                splitArgs(new String[]{"--bfoo", "--bbar", "--", "--lfoo", "--lbar"})
        );
        assertEquals(
                new SplitArgs(
                        new String[]{"--bfoo", "--bbar"},
                        new String[]{"--lfoo", "--lbar", "--lbaz"}
                ),
                splitArgs(new String[]{"--bfoo", "--bbar", "--", "--lfoo", "--lbar", "--lbaz"})
        );
        assertEquals(
                new SplitArgs(
                        new String[]{"--bfoo", "--bbar", "--bbaz"},
                        new String[]{"--lfoo", "--lbar"}
                ),
                splitArgs(new String[]{"--bfoo", "--bbar", "--bbaz", "--", "--lfoo", "--lbar"})
        );
    }

    @Test
    void onlyLauncherTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{},
                        new String[]{"--lfoo", "--lbar"}
                ),
                splitArgs(new String[]{"--lfoo", "--lbar"})
        );
    }

    @Test
    void onlyBootstrapTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{"--bfoo", "--bbar"},
                        new String[]{}
                ),
                splitArgs(new String[]{"--bfoo", "--bbar", "--"})
        );
    }

    @Test
    void splitOnlyTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{},
                        new String[]{}
                ),
                splitArgs(new String[]{"--"})
        );
    }

    @Test
    void emptyArgsTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{},
                        new String[]{}
                ),
                splitArgs(new String[]{})
        );
    }

    @Test
    void multiSplitTest() {
        assertEquals(
                new SplitArgs(
                        new String[]{"--bfoo"},
                        new String[]{"--lfoo", "--", "--foo"}
                ),
                splitArgs(new String[]{"--bfoo", "--", "--lfoo", "--", "--foo"})
        );
    }

}