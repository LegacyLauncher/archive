package net.minecraft.launcher.process;

import java.lang.reflect.Array;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LimitedCapacityList {
   private final Object[] items;
   private final Class clazz;
   private final ReadWriteLock locks = new ReentrantReadWriteLock();
   private int size;
   private int head;

   public LimitedCapacityList(Class clazz, int maxSize) {
      this.clazz = clazz;
      this.items = (Object[])((Object[])Array.newInstance(clazz, maxSize));
   }

   public Object add(Object value) {
      this.locks.writeLock().lock();
      this.items[this.head] = value;
      this.head = (this.head + 1) % this.getMaxSize();
      if (this.size < this.getMaxSize()) {
         ++this.size;
      }

      this.locks.writeLock().unlock();
      return value;
   }

   int getMaxSize() {
      this.locks.readLock().lock();
      int result = this.items.length;
      this.locks.readLock().unlock();
      return result;
   }
}
