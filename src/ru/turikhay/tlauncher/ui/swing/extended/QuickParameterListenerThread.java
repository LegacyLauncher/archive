package ru.turikhay.tlauncher.ui.swing.extended;

import ru.turikhay.tlauncher.ui.swing.util.IntegerArrayGetter;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class QuickParameterListenerThread extends LoopedThread {
	public final static int DEFAULT_TICK = 500;

	private final IntegerArrayGetter paramGetter;		
	private final Runnable runnable;

	private final int tick;

	QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run, int tick) {
		super("QuickParameterListenerThread");

		if(getter == null)
			throw new NullPointerException("Getter is NULL!");

		if(run == null)
			throw new NullPointerException("Runnable is NULL!");

		if(tick < 0)
			throw new IllegalArgumentException("Tick must be positive!");

		this.paramGetter = getter;
		this.runnable = run;

		this.tick = tick;

		this.setPriority(MIN_PRIORITY);
		this.startAndWait();
	}

	QuickParameterListenerThread(IntegerArrayGetter getter, Runnable run) {
		this(getter, run, DEFAULT_TICK);
	}

	void startListening() {
		iterate();
	}

	@Override
	protected void iterateOnce() {
		int[] initial = paramGetter.getIntegerArray(), newvalue;
		int i = 0; boolean equal;

		while(true) {
			sleep(); // Wait for a new value

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
		U.sleepFor(tick);
	}
}
