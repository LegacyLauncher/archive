package com.turikhay.tlauncher.timer;

import com.turikhay.tlauncher.TLauncherException;
import com.turikhay.tlauncher.util.U;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Timer extends Thread {
   private Map tasks = new HashMap();
   private List remove = new ArrayList();
   private boolean available = true;
   private boolean tasks_ = true;

   public void run() {
      long tick = 0L;

      while(this.available) {
         ++tick;
         this.tasks_ = false;
         Iterator var4 = this.tasks.entrySet().iterator();

         while(var4.hasNext()) {
            Entry entry = (Entry)var4.next();
            String name = (String)entry.getKey();
            TimerTask task = (TimerTask)entry.getValue();
            if (tick % (long)task.getTicks() == 0L) {
               if (!task.isRepeating()) {
                  this.remove.add(name);
               }

               try {
                  task.run();
               } catch (Exception var9) {
                  throw new TLauncherException("Exception in task \"" + name + "\"", var9);
               }
            }
         }

         var4 = this.remove.iterator();

         while(var4.hasNext()) {
            String cr = (String)var4.next();
            U.log("[TIMER] Removing " + cr);
            this.tasks.remove(cr);
         }

         this.remove.clear();
         this.tasks_ = true;

         try {
            Thread.sleep(1000L);
         } catch (InterruptedException var8) {
            throw new TLauncherException("Timer cannot sleep.", var8);
         }
      }

      this.run();
   }

   public void add(String name, TimerTask task) {
      while(!this.tasks_) {
         this.sleepFor(10L);
      }

      this.tasks_ = false;
      if (this.tasks.containsKey(name)) {
         throw new TLauncherException("Tried to add task with the same name");
      } else {
         this.tasks.put(name, task);
         this.tasks_ = true;
      }
   }

   public void remove(String name) {
      while(!this.tasks_) {
         this.sleepFor(10L);
      }

      this.tasks_ = false;
      this.tasks.remove(name);
      this.tasks_ = true;
   }

   private void sleepFor(long millis) {
      try {
         Thread.sleep(millis);
      } catch (Exception var4) {
      }

   }
}
