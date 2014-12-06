package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;

public class ServicePanel extends ExtendedPanel implements ResizeableComponent {

	ServicePanel(MainPane pane) {

	}

	@Override
	public void onResize() {
		// TODO Auto-generated method stub

	}

	/*private static final int TIMER = 5;

	private final MainPane pane;

	private boolean mouseIn;

	private URL url;
	private Slide slide;

	ServicePanel(MainPane pane) {
		this.pane = pane;

		this.url = SlideBackground.class.getResource("clown.jpg");

		U.log(url);

		if(url == null) {
			U.log("Cannot find clown image :C");
			return;
		}

		final ServicePanelThread thread = new ServicePanelThread();

		addMouseListenerOriginally(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				mouseIn = true;

				if(thread.isThreadLocked())
					thread.unlockThread("locked");
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseIn = false;
			}
		});

		pane.add(this);
	}

	@Override
	public void paint(Graphics g0) {
		g0.drawRect(0, 0, getWidth()-1, getHeight()-1);
	}

	@Override
	public void onResize() {
		setSize(pane.getWidth(), pane.getHeight());
	}

	private class ServicePanelThread extends ExtendedThread {

		ServicePanelThread() {
			super("ServicePanel");
			this.startAndWait();
		}

		@Override
		public void run() {
			lockThread("locked");
			threadLog("unlocked");

			while(true) {

				while(!mouseIn) {
					U.sleepFor(1000);
					threadLog("!mouseIn");
				}

				int timer = 0;

				while(mouseIn && timer < TIMER) {
					timer++;
					threadLog("timer:", timer);
					U.sleepFor(1000);
				}

				if(timer < TIMER) {
					threadLog("timer <",TIMER);
					continue;
				}

				if(slide == null)
					slide = new Slide(url);

				SlideBackgroundThread thread = pane.background.SLIDE_BACKGROUND.getThread();

				Slide oldSlide = thread.getSlide();
				thread.setSlide(slide, true);

				while(mouseIn) {
					U.sleepFor(100);
					threadLog("mouseIn");
				}

				thread.setSlide(oldSlide, true);
			}
		}

	}*/

	/*	private final Image helper;
	private int width, height, y;
	private float opacity;

	private ServicePanelThread thread;
	private boolean mouseIn;

	private Clip clip;
	private long lastCall;

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
			public void mouseClicked(MouseEvent e) {
				playSound();
			}
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

		if(AudioSystem.getAudioFileTypes().length == 0) {
			U.log("No audio file type supported.");
			return;
		}

		loadSound();
	}

	private boolean loadSound() {
		URL sound = getClass().getResource("surprise.wav");

		try {
			AudioInputStream audio = AudioSystem.getAudioInputStream(sound);

			clip = AudioSystem.getClip();
			clip.open(audio);

		} catch (Exception e) {
			U.log("Cannot open audio file", e);
			return false;
		}

		return true;
	}

	private void playSound() {
		if(opacity < .5)
			return;

		if(!loadSound())
			return;

		if(lastCall - System.currentTimeMillis() > -1000)
			return;

		clip.start();
		lastCall = System.currentTimeMillis();
	}

	@Override
	public void paint(Graphics g0) {
		g0.drawRect(0, 0, getWidth()-1, getHeight()-1);

		if(!thread.isIterating()) return;

		Graphics2D g = (Graphics2D) g0;

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		g.drawImage(helper, getWidth() / 2 - width / 2, getHeight() - y, null);
	}

	@Override
	public void onResize() {
		setLocation(0, pane.getHeight() - getHeight());
	}

	class ServicePanelThread extends LoopedThread {
		private static final int PIXEL_STEP = 5, TIMEFRAME = 15;
		private static final float OPACITY_STEP = .05f;

		ServicePanelThread() {
			super("ServicePanel");
			this.startAndWait();
		}

		@Override
		protected void iterateOnce() {
			int timeout = 15;

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
	}*/

}
