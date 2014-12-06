package ru.turikhay.tlauncher.ui.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.apache.commons.lang3.StringUtils;

import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class Dragger extends BorderPanel implements LocalizableComponent {
	private static final List<SoftReference<Dragger>> draggers = new ArrayList<SoftReference<Dragger>>();
	private static final Color enabledColor = new Color(0, 0, 0, 32), disabledColor = new Color(0, 0, 0, 16);

	private static Configuration config;
	private static Point maxPoint;

	private static boolean ready;

	private final JComponent parent;
	private final String key;

	private final ExtendedLabel label;
	private final DraggerMouseListener listener;

	private String tooltip;

	public Dragger(JComponent parent, String name) {
		if(parent == null)
			throw new NullPointerException("parent");

		if(name == null)
			throw new NullPointerException("name");

		if(StringUtils.isEmpty(name))
			throw new IllegalArgumentException("name is empty");

		this.parent = parent;
		this.key = "dragger." + name;

		this.listener = new DraggerMouseListener();
		setCursor(SwingUtil.getCursor(Cursor.MOVE_CURSOR));

		label = new ExtendedLabel();
		label.addMouseListener(listener);
		setCenter(label);

		if(!ready)
			draggers.add(new SoftReference<Dragger>(this));

		setEnabled(true);
	}

	@Override
	public void paint(Graphics g0) {
		Graphics2D g = (Graphics2D) g0;

		g.setColor(isEnabled() ? enabledColor : disabledColor);
		g.fillRect(0, 0, getWidth(), getHeight());

		super.paintComponent(g);
	}

	@Override
	public void setEnabled(boolean b) {
		tooltip = b? "dragger.label" : null;
		updateLocale();

		super.setEnabled(b);
	}

	private void dragComponent(int x, int y) {
		if(!ready)
			return;

		if(x + parent.getWidth() > maxPoint.x)
			x = maxPoint.x - parent.getWidth();
		if(x < 0)
			x = 0;

		if(y + parent.getHeight() > maxPoint.y)
			y = maxPoint.y - parent.getHeight();

		if(y < 0)
			y = 0;

		parent.setLocation(x, y);
		config.set(key, new IntegerArray(x, y));

		U.log(x, y);
	}

	public void updateCoords() {
		dragComponent(parent.getX(), parent.getY());
	}

	public void loadCoords() {
		IntegerArray arr;
		try {
			arr = IntegerArray.parseIntegerArray(config.get(key));

			if(arr.size() != 2)
				throw new IllegalArgumentException("illegal size");

		} catch(Exception e) {
			e.printStackTrace();
			return;
		}

		dragComponent(arr.get(0), arr.get(1));
	}

	public class DraggerMouseListener extends MouseAdapter {
		private int[] startPoint = {0, 0};

		@Override
		public void mousePressed(MouseEvent e) {
			if(!isEnabled())
				return;

			startPoint[0] = e.getX();
			startPoint[1] = e.getY();
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(!isEnabled())
				return;

			dragComponent(
					parent.getX() + e.getX() - startPoint[0],
					parent.getY() + e.getY() - startPoint[1]);
		}
	}

	private void ready() {
		updateLocale();
		loadCoords();
	}

	@Override
	public void updateLocale() {
		label.setToolTipText(Localizable.get(tooltip));
	}

	public synchronized static void ready(Configuration config, Point maxPoint) {
		if(ready)
			return;

		if(config == null)
			throw new NullPointerException("config");

		if(maxPoint == null)
			throw new NullPointerException("maxPoint");

		Dragger.config = config;
		Dragger.maxPoint = maxPoint;
		Dragger.ready = true;

		for(SoftReference<Dragger> dragger : draggers)
			if(dragger.get() == null)
				U.log("dragger has been deleted :(");
			else
				dragger.get().ready();
	}

	public synchronized static void update() {
		for(SoftReference<Dragger> dragger : draggers)
			if(dragger.get() == null)
				U.log("dragger has been deleted :(");
			else
				dragger.get().updateCoords();
	}

}
