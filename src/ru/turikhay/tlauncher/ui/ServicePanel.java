package ru.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class ServicePanel extends ExtendedPanel implements ResizeableComponent {
	private static final long serialVersionUID = -3973551999471811629L;

	private final MainPane pane;

	private final Image helper;
	private int width, height, y;
	private float opacity;

	private ServicePanelThread thread;
	private boolean mouseIn;

	ServicePanel(MainPane pane) {
		this.pane = pane;
		this.helper = ImageCache.getImage("helper.png", false);

		if(helper == null)
			return;

		this.width = helper.getWidth(null);
		this.height = helper.getHeight(null);

		pane.add(this);
		setSize(width, height);

		opacity = 0.1F;
		y = 0;
		this.thread = new ServicePanelThread();

		pane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				onResize();
			}
		});

		this.addMouseListenerOriginally(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				mouseIn = true;
				thread.iterate();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseIn = false;
			}
		});
	}

	@Override
	public void paint(Graphics g0) {
		if(!thread.isIterating()) return;

		Graphics2D g = (Graphics2D) g0;

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));		
		g.drawImage(helper, getWidth() / 2 - width / 2, getHeight() - y, null);
	}

	@Override
	public void onResize() {
		setLocation(pane.getWidth() - getWidth(), pane.getHeight() - getHeight());
	}

	class ServicePanelThread extends LoopedThread {
		private static final int PIXEL_STEP = 5, TIMEFRAME = 25;
		private static final float OPACITY_STEP = .05f;

		ServicePanelThread() {
			super("ServicePanel");
			this.startAndWait();
		}

		@Override
		protected void iterateOnce() {
			int timeout = 10;

			while(--timeout > 0) {
				if(!mouseIn) return;
				U.sleepFor(1000);
			}

			y = 1;

			while(y > 0) {
				while(mouseIn) {
					onIn();
				}

				while(!mouseIn) {
					onOut();

					if(y == 0)
						return;
				}
			}
		}

		private void onIn() {
			if(y < getHeight())
				y += PIXEL_STEP;

			if(y > getHeight())
				y = getHeight();

			if(opacity < .9)
				opacity += OPACITY_STEP;

			if(opacity > 1)
				opacity = 1;

			repaintSleep();
		}

		private void onOut() {
			if(y > 0)
				y -= PIXEL_STEP;

			if(y < 0)
				y = 0;

			if(opacity > .0)
				opacity -= OPACITY_STEP;

			if(opacity < 0)
				opacity = 0;

			repaintSleep();
		}

		private void repaintSleep() {
			repaint();
			U.sleepFor(TIMEFRAME);
		}
	}

}
