package ru.turikhay.util;

public enum Direction {
	TOP_LEFT, TOP, TOP_RIGHT,
	CENTER_LEFT, CENTER, CENTER_RIGHT,
	BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT;

	private final String lower;

	Direction() {
		this.lower = name().toLowerCase();
	}

	@Override
	public String toString() {
		return lower;
	}
}
