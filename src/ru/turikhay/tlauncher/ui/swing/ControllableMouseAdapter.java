package ru.turikhay.tlauncher.ui.swing;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class ControllableMouseAdapter implements MouseListener {
	private MouseEventHandler
		click, press, release, enter, exit;
	
	public MouseEventHandler getOnClick() {
		return click;
	}
	
	public ControllableMouseAdapter setOnClick(MouseEventHandler handler) {
		this.click = handler;
		return this;
	}
	
	public MouseEventHandler getOnPress() {
		return press;
	}
	
	public ControllableMouseAdapter setOnPress(MouseEventHandler handler) {
		this.press = handler;
		return this;
	}
	
	public MouseEventHandler getOnRelease() {
		return release;
	}
	
	public ControllableMouseAdapter setOnRelease(MouseEventHandler handler) {
		this.release = handler;
		return this;
	}
	
	public MouseEventHandler getOnEnter() {
		return enter;
	}
	
	public ControllableMouseAdapter setOnEnter(MouseEventHandler handler) {
		this.enter = handler;
		return this;
	}
	
	public MouseEventHandler getOnExit() {
		return exit;
	}
	
	public ControllableMouseAdapter setOnExit(MouseEventHandler handler) {
		this.exit = handler;
		return this;
	}
	
	@Override
	public final void mouseClicked(MouseEvent e) {
		if(click != null)
			click.handleEvent(e);
	}

	@Override
	public final void mousePressed(MouseEvent e) {
		if(press != null)
			press.handleEvent(e);
	}

	@Override
	public final void mouseReleased(MouseEvent e) {
		if(release != null)
			release.handleEvent(e);
	}

	@Override
	public final void mouseEntered(MouseEvent e) {
		if(enter != null)
			enter.handleEvent(e);
	}

	@Override
	public final void mouseExited(MouseEvent e) {
		if(exit != null)
			exit.handleEvent(e);
	}
	
	public interface MouseEventHandler {
		void handleEvent(MouseEvent e);
	}
}
