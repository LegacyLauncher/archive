package ru.turikhay.util.async;

public abstract class AsyncObject<E> extends ExtendedThread {

	private boolean gotValue;
	private E value;
	private AsyncObjectGotErrorException error;

	protected AsyncObject() {
		super("AsyncObject");
	}

	@Override
	public void run() {
		try {
			this.value = this.execute();
		} catch (Throwable e) {
			this.error = new AsyncObjectGotErrorException(this, e);
			return;
		}

		this.gotValue = true;
	}

	public E getValue() throws AsyncObjectNotReadyException,
	AsyncObjectGotErrorException {
		if (error != null)
			throw error;
		if (!gotValue)
			throw new AsyncObjectNotReadyException();

		return value;
	}

	public AsyncObjectGotErrorException getError() {
		return error;
	}

	protected abstract E execute();
}
