package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import com.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;
import com.turikhay.util.U;
import com.turikhay.util.async.LoopedThread;

/**
 * <code>ExtendedComponentListener</code> gives ability to track the start and end
 * moments of resizing/moving
 * 
 * @author turikhay
 * @see #onComponentMoved(ComponentEvent)
 * @see #onComponentResized(ComponentEvent)
 * 
 */
public abstract class ExtendedComponentListener implements ComponentListener {
	private final Component comp;
	private final QuickParameterListenerThread resizeListener, moveListener;
	private ComponentEvent lastResizeEvent, lastMoveEvent;
	
	public ExtendedComponentListener(Component component) {
		if(component == null)
			throw new NullPointerException();
		
		this.comp = component;
		
		this.resizeListener =
			new QuickParameterListenerThread(new IntegerArrayGetter() {
				@Override
				public int[] getIntegerArray() {
					return new int[]{ comp.getWidth(), comp.getHeight() };
				}
			}, new Runnable() {
				@Override
				public void run() {
					onComponentResized(lastResizeEvent);
				}
			});
		
		this.moveListener =
			new QuickParameterListenerThread(new IntegerArrayGetter() {
				@Override
				public int[] getIntegerArray() {
					Point location = comp.getLocation();
					return new int[]{ location.x, location.y };
				}
			}, new Runnable() {
				@Override
				public void run() {
					onComponentMoved(lastMoveEvent);
				}
			});
	}	
	
	@Override
	public final void componentResized(ComponentEvent e) {
		onComponentResizing(e);
		resizeListener.startListening();
	}

	@Override
	public final void componentMoved(ComponentEvent e) {
		onComponentMoving(e);
		moveListener.startListening();
	}
	
	//
	
	public abstract void onComponentResizing(ComponentEvent e);
	/**
	 * Tells the listener that component is not resizing at least for <code>TICK</code> ms.
	 */
	public abstract void onComponentResized(ComponentEvent e);
	
	public abstract void onComponentMoving(ComponentEvent e);
	/**
	 * Tells the listener that component is not moving at least for <code>TICK</code> ms.
	 */
	public abstract void onComponentMoved(ComponentEvent e);
	
	//
	
	private class QuickParameterListenerThread extends LoopedThread {
		private final static int TICK = 500;
		
		private final IntegerArrayGetter paramGetter;		
		private final Runnable runnable;
		
		QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run) {
			super("QuickParameterListenerThread");
			
			this.paramGetter = getter;
			
			this.runnable = run;
			
			this.startAndWait();
		}
		
		void startListening() {
			iterate();
		}

		@Override
		protected void iterateOnce() {
			int[] initial = paramGetter.getIntegerArray(), newvalue;
			int i = 0; boolean equal;
			
			while(true) {
				sleep(); // Sleep waiting new value
				
				newvalue = paramGetter.getIntegerArray();				
				equal = true;
				
				for(i=0;i<initial.length;i++)
					if(initial[i] != newvalue[i])
						equal = false;
				
				// Make current value initial for next iteration
				initial = newvalue;
				
				if(!equal)
					continue; // Value is still changing
				
				break; // All integers are equal, value hasn't been changed while we've been sleeping.
			}
			
			runnable.run(); // Can notify listener that value has been changed.
		}
		
		private void sleep() {
			U.sleepFor(TICK);
		}
	}
}
