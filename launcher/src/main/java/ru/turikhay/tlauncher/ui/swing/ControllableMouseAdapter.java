package ru.turikhay.tlauncher.ui.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ControllableMouseAdapter implements MouseListener {
    private ControllableMouseAdapter.MouseEventHandler click;
    private ControllableMouseAdapter.MouseEventHandler press;
    private ControllableMouseAdapter.MouseEventHandler release;
    private ControllableMouseAdapter.MouseEventHandler enter;
    private ControllableMouseAdapter.MouseEventHandler exit;

    public ControllableMouseAdapter.MouseEventHandler getOnClick() {
        return click;
    }

    public ControllableMouseAdapter setOnClick(ControllableMouseAdapter.MouseEventHandler handler) {
        click = handler;
        return this;
    }

    public ControllableMouseAdapter.MouseEventHandler getOnPress() {
        return press;
    }

    public ControllableMouseAdapter setOnPress(ControllableMouseAdapter.MouseEventHandler handler) {
        press = handler;
        return this;
    }

    public ControllableMouseAdapter.MouseEventHandler getOnRelease() {
        return release;
    }

    public ControllableMouseAdapter setOnRelease(ControllableMouseAdapter.MouseEventHandler handler) {
        release = handler;
        return this;
    }

    public ControllableMouseAdapter.MouseEventHandler getOnEnter() {
        return enter;
    }

    public ControllableMouseAdapter setOnEnter(ControllableMouseAdapter.MouseEventHandler handler) {
        enter = handler;
        return this;
    }

    public ControllableMouseAdapter.MouseEventHandler getOnExit() {
        return exit;
    }

    public ControllableMouseAdapter setOnExit(ControllableMouseAdapter.MouseEventHandler handler) {
        exit = handler;
        return this;
    }

    public final void mouseClicked(MouseEvent e) {
        if (click != null) {
            click.handleEvent(e);
        }

    }

    public final void mousePressed(MouseEvent e) {
        if (press != null) {
            press.handleEvent(e);
        }

    }

    public final void mouseReleased(MouseEvent e) {
        if (release != null) {
            release.handleEvent(e);
        }

    }

    public final void mouseEntered(MouseEvent e) {
        if (enter != null) {
            enter.handleEvent(e);
        }

    }

    public final void mouseExited(MouseEvent e) {
        if (exit != null) {
            exit.handleEvent(e);
        }

    }

    public interface MouseEventHandler {
        void handleEvent(MouseEvent var1);
    }
}
