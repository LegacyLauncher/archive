package net.minecraft.launcher.updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RefreshThread {
   private static List threads = Collections.synchronizedList(new ArrayList());
   private final VersionList list;
   private boolean cancelled;

   RefreshThread(VersionList list) {
      this.list = list;
   }

   public void refreshVersions() throws IOException {
      this.list.refreshVersions();
   }

   public void cancelRefresh() {
      this.cancelled = true;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public static void cancelAll() {
      synchronized(threads) {
         Iterator i = threads.iterator();

         while(i.hasNext()) {
            ((RefreshThread)i.next()).cancelRefresh();
            i.remove();
         }

      }
   }
}
