package com.turikhay.tlauncher.ui.block;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Blocker {
   private static final Map blockMap = Collections.synchronizedMap(new HashMap());
   public static final Object UNIVERSAL_UNBLOCK = "lol, nigga";
   public static final Object WEAK_BLOCK = "weak";

   public static void add(Blockable blockable) {
      if (blockable == null) {
         throw new NullPointerException();
      } else if (blockMap.containsKey(blockable)) {
         throw new IllegalArgumentException("Blockable is already added: " + blockable);
      } else {
         blockMap.put(blockable, Collections.synchronizedList(new ArrayList()));
      }
   }

   public static void remove(Blockable blockable) {
      if (blockable == null) {
         throw new NullPointerException();
      } else if (!blockMap.containsKey(blockable)) {
         throw new IllegalArgumentException("Blockable is not added: " + blockable);
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

   public static void block(Blockable blockable, Object reason) {
      if (blockable != null) {
         if (reason == null) {
            throw new NullPointerException("Reason is NULL!");
         } else if (!blockMap.containsKey(blockable)) {
            throw new IllegalArgumentException("Blockable is not added: " + blockable);
         } else {
            List reasons = (List)blockMap.get(blockable);
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
         Blockable[] var5 = blockables;
         int var4 = blockables.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Blockable blockable = var5[var3];
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
      } else {
         List reasons = (List)blockMap.get(blockable);
         reasons.remove(reason);
         if (reason.equals(UNIVERSAL_UNBLOCK)) {
            reasons.clear();
         }

         if (reasons.contains(WEAK_BLOCK)) {
            reasons.remove(WEAK_BLOCK);
         }

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
         Blockable[] var5 = blockables;
         int var4 = blockables.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Blockable blockable = var5[var3];
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

   public static boolean isBlocked(Blockable blockable) {
      if (blockable == null) {
         throw new NullPointerException();
      } else if (!blockMap.containsKey(blockable)) {
         throw new IllegalArgumentException("Blockable is not added: " + blockable);
      } else {
         return !((List)blockMap.get(blockable)).isEmpty();
      }
   }

   public static List getBlockList(Blockable blockable) {
      if (blockable == null) {
         throw new NullPointerException();
      } else if (!blockMap.containsKey(blockable)) {
         throw new IllegalArgumentException("Blockable is not added: " + blockable);
      } else {
         return Collections.unmodifiableList((List)blockMap.get(blockable));
      }
   }

   public static void blockComponents(Container container, Object reason) {
      if (container == null) {
         throw new NullPointerException("Container is NULL!");
      } else if (reason == null) {
         throw new NullPointerException("Reason is NULL!");
      } else {
         Component[] var5;
         int var4 = (var5 = container.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component component = var5[var3];
            if (component instanceof Blockable) {
               block((Blockable)component, reason);
            } else {
               component.setEnabled(false);
               if (component instanceof Container) {
                  blockComponents((Container)component, reason);
               }
            }
         }

      }
   }

   public static void unblockComponents(Container container, Object reason) {
      if (container == null) {
         throw new NullPointerException("Container is NULL!");
      } else if (reason == null) {
         throw new NullPointerException("Reason is NULL!");
      } else {
         Component[] var5;
         int var4 = (var5 = container.getComponents()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Component component = var5[var3];
            if (component instanceof Blockable) {
               unblock((Blockable)component, reason);
            } else {
               component.setEnabled(true);
               if (component instanceof Container) {
                  unblockComponents((Container)component, reason);
               }
            }
         }

      }
   }
}
