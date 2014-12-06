package ru.turikhay.tlauncher.ui.block;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class Blocker {
	private final static Map<Blockable, List<Object>> blockMap = new Hashtable<Blockable, List<Object>>();
	public final static Object UNIVERSAL_UNBLOCK = "lol, nigga";
	public final static Object WEAK_BLOCK = "weak";

	private static void add(Blockable blockable) {
		if (blockable == null)
			throw new NullPointerException();

		blockMap.put(blockable,
				Collections.synchronizedList(new ArrayList<Object>()));
	}

	public static void cleanUp(Blockable blockable) {
		if (blockable == null)
			throw new NullPointerException();

		blockMap.remove(blockable);
	}

	public static boolean contains(Blockable blockable) {
		if (blockable == null)
			throw new NullPointerException();

		return blockMap.containsKey(blockable);
	}

	public static void block(Blockable blockable, Object reason) {
		if (blockable == null)
			return;

		if (reason == null)
			throw new NullPointerException("Reason is NULL!");

		if (!blockMap.containsKey(blockable))
			add(blockable);

		List<Object> reasons = blockMap.get(blockable);

		if (reasons.contains(reason))
			return;

		boolean blocked = !reasons.isEmpty();

		reasons.add(reason);

		if (blocked)
			return;

		blockable.block(reason);
	}

	public static void block(Object reason, Blockable... blockables) {
		if (blockables == null || reason == null)
			throw new NullPointerException("Blockables are NULL: "
					+ (blockables == null) + ", reason is NULL: "
					+ (reason == null));

		for (Blockable blockable : blockables)
			block(blockable, reason);
	}

	public static boolean unblock(Blockable blockable, Object reason) {
		if (blockable == null)
			return false;

		if (reason == null)
			throw new NullPointerException("Reason is NULL!");

		if (!blockMap.containsKey(blockable))
			return true;

		List<Object> reasons = blockMap.get(blockable);

		reasons.remove(reason);

		if (reason.equals(UNIVERSAL_UNBLOCK))
			reasons.clear();
		if (reasons.contains(WEAK_BLOCK))
			reasons.remove(WEAK_BLOCK);
		if (!reasons.isEmpty())
			return false;

		blockable.unblock(reason);
		return true;
	}

	public static void unblock(Object reason, Blockable... blockables) {
		if (blockables == null || reason == null)
			throw new NullPointerException("Blockables are NULL: "
					+ (blockables == null) + ", reason is NULL: "
					+ (reason == null));

		for (Blockable blockable : blockables)
			unblock(blockable, reason);
	}

	public static void setBlocked(Blockable blockable, Object reason,
			boolean blocked) {
		if (blocked)
			block(blockable, reason);
		else
			unblock(blockable, reason);
	}

	public static boolean isBlocked(Blockable blockable) {
		if (blockable == null)
			throw new NullPointerException();

		if (!blockMap.containsKey(blockable))
			return false;

		return !blockMap.get(blockable).isEmpty();
	}

	public static List<Object> getBlockList(Blockable blockable) {
		if (blockable == null)
			throw new NullPointerException();

		if (!blockMap.containsKey(blockable))
			add(blockable);

		return Collections.unmodifiableList(blockMap.get(blockable));
	}

	public static void blockComponents(Object reason, Component... components) {
		if (components == null)
			throw new NullPointerException("Components is NULL!");

		if (reason == null)
			throw new NullPointerException("Reason is NULL!");

		for (Component component : components)
			if (component instanceof Blockable)
				block((Blockable) component, reason);
			else
				if(!(component instanceof Unblockable)) {
					component.setEnabled(false);
					if (component instanceof Container)
						blockComponents((Container) component, reason);
				}
	}

	public static void blockComponents(Container container, Object reason) {
		blockComponents(reason, container.getComponents());
	}

	public static void unblockComponents(Object reason, Component... components) {
		if (components == null)
			throw new NullPointerException("Components is NULL!");

		if (reason == null)
			throw new NullPointerException("Reason is NULL!");

		for (Component component : components)
			if (component instanceof Blockable)
				unblock((Blockable) component, reason);
			else
				if(!(component instanceof Unblockable)) {
					component.setEnabled(true);
					if (component instanceof Container)
						unblockComponents((Container) component, reason);
				}
	}

	public static void unblockComponents(Container container, Object reason) {
		unblockComponents(reason, container.getComponents());
	}
}
