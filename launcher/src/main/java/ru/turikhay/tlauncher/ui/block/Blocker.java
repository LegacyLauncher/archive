package ru.turikhay.tlauncher.ui.block;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Blocker {
    private static final Map<Blockable, List<Object>> blockMap = new Hashtable<>();
    public static final Object UNIVERSAL_UNBLOCK = "lol, man";
    public static final Object WEAK_BLOCK = "weak";

    private static void add(Blockable blockable) {
        if (blockable == null) {
            throw new NullPointerException();
        } else {
            blockMap.put(blockable, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    public static void cleanUp(Blockable blockable) {
        if (blockable == null) {
            throw new NullPointerException();
        } else {
            blockMap.remove(blockable);
        }
    }

    public static boolean contains(Blockable blockable) {
        if (blockable == null) {
            throw new NullPointerException();
        } else {
            return blockMap.containsKey(blockable);
        }
    }

    public static void toggle(Blockable blockable, Object reason) {
        setBlocked(blockable, reason, !getBlockList(blockable).contains(reason));
    }

    public static void block(Blockable blockable, Object reason) {
        if (blockable != null) {
            if (reason == null) {
                throw new NullPointerException("Reason is NULL!");
            } else {
                if (!blockMap.containsKey(blockable)) {
                    add(blockable);
                }

                List<Object> reasons = blockMap.get(blockable);
                if (!reasons.contains(reason)) {
                    boolean blocked = !reasons.isEmpty();
                    reasons.add(reason);
                    if (!blocked) {
                        blockable.block(reason);
                    }
                }
            }
        }
    }

    public static void block(Object reason, Blockable... blockables) {
        if (blockables != null && reason != null) {

            for (Blockable blockable : blockables) {
                block(blockable, reason);
            }

        } else {
            throw new NullPointerException("Blockables are NULL: " + (blockables == null) + ", reason is NULL: " + (reason == null));
        }
    }

    public static boolean unblock(Blockable blockable, Object reason) {
        if (blockable == null) {
            return false;
        } else if (reason == null) {
            throw new NullPointerException("Reason is NULL!");
        } else if (!blockMap.containsKey(blockable)) {
            return true;
        } else {
            List<Object> reasons = blockMap.get(blockable);
            reasons.remove(reason);
            if (reason.equals(UNIVERSAL_UNBLOCK)) {
                reasons.clear();
            }

            reasons.remove(WEAK_BLOCK);

            if (!reasons.isEmpty()) {
                return false;
            } else {
                blockable.unblock(reason);
                return true;
            }
        }
    }

    public static void unblock(Object reason, Blockable... blockables) {
        if (blockables != null && reason != null) {

            for (Blockable blockable : blockables) {
                unblock(blockable, reason);
            }

        } else {
            throw new NullPointerException("Blockables are NULL: " + (blockables == null) + ", reason is NULL: " + (reason == null));
        }
    }

    public static void setBlocked(Blockable blockable, Object reason, boolean blocked) {
        if (blocked) {
            block(blockable, reason);
        } else {
            unblock(blockable, reason);
        }
    }

    public static void setBlocked(Object reason, boolean blocked, Blockable... blockables) {
        for (Blockable blockable : blockables) {
            if (blocked) {
                block(blockable, reason);
            } else {
                unblock(blockable, reason);
            }
        }
    }

    public static boolean isBlocked(Blockable blockable) {
        if (blockable == null) {
            throw new NullPointerException();
        } else {
            return blockMap.containsKey(blockable) && !blockMap.get(blockable).isEmpty();
        }
    }

    public static List<Object> getBlockList(Blockable blockable) {
        if (blockable == null) {
            throw new NullPointerException();
        } else {
            if (!blockMap.containsKey(blockable)) {
                add(blockable);
            }

            return Collections.unmodifiableList(blockMap.get(blockable));
        }
    }

    public static void blockComponents(Object reason, Component... components) {
        if (components == null) {
            throw new NullPointerException("Components is NULL!");
        } else if (reason == null) {
            throw new NullPointerException("Reason is NULL!");
        } else {
            for (Component component : components) {
                if (component instanceof Blockable) {
                    block((Blockable) component, reason);
                } else if (!(component instanceof Unblockable)) {
                    component.setEnabled(false);
                    if (component instanceof Container) {
                        blockComponents((Container) component, reason);
                    }
                }
            }
        }
    }

    public static void blockComponents(Container container, Object reason) {
        blockComponents(reason, container.getComponents());
    }

    public static void unblockComponents(Object reason, Component... components) {
        if (components == null) {
            throw new NullPointerException("Components is NULL!");
        } else if (reason == null) {
            throw new NullPointerException("Reason is NULL!");
        } else {
            for (Component component : components) {
                if (component instanceof Blockable) {
                    unblock((Blockable) component, reason);
                } else if (!(component instanceof Unblockable)) {
                    component.setEnabled(true);
                    if (component instanceof Container) {
                        unblockComponents((Container) component, reason);
                    }
                }
            }
        }
    }

    public static void unblockComponents(Container container, Object reason) {
        unblockComponents(reason, container.getComponents());
    }
}
