package ru.turikhay.tlauncher.repository;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.exceptions.IOExceptionList;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

public class RepoList {
   private static final AtomicLong GLOBAL_BUFFER = new AtomicLong();
   private final String name;
   private final Object sync = new Object();
   private final ArrayList list = new ArrayList();
   private RepoList.RelevantRepoList relevant = new RepoList.RelevantRepoList();
   private final String prefix;

   public RepoList(String name) {
      if (StringUtils.isEmpty(name)) {
         throw new IllegalArgumentException("name");
      } else {
         this.name = name;
         this.prefix = "[" + name + "]";
      }
   }

   public String toString() {
      return this.name;
   }

   public final RepoList.RelevantRepoList getRelevant() {
      return this.relevant;
   }

   public InputStream read(String path, Proxy proxy, int attempts) throws IOExceptionList {
      List exList = new ArrayList();
      List l = this.getRelevant().getList();
      int timeout = U.getConnectionTimeout();
      int attempt = 0;
      Object total = new Object();
      Time.start(total);

      while(attempt < attempts) {
         ++attempt;
         this.log(String.format("Fetching: \"%s\", timeout: %d, proxy: %s", path, timeout * attempt / 1000, proxy));
         Iterator var9 = l.iterator();

         while(var9.hasNext()) {
            IRepo repo = (IRepo)var9.next();
            Object current = new Object();
            Time.start(current);

            try {
               InputStream result = this.read(repo.get(path, timeout * attempt, proxy));
               long[] deltas = Time.stop(total, current);
               this.log(String.format("Fetched successfully: \"%s\"; attempt: %d ms; total: %d ms", path, deltas[1], deltas[0]));
               return result;
            } catch (IOException var14) {
               this.log(String.format("Failed to fetch \"%s\": %s", path, var14));
               exList.add(var14);
            }
         }
      }

      throw new IOExceptionList(exList);
   }

   public final InputStream read(String path) throws IOExceptionList {
      return this.read(path, U.getProxy(), TLauncher.getInstance().getSettings().getConnectionQuality().getMaxTries());
   }

   protected InputStream read(URLConnection connection) throws IOException {
      int size = connection instanceof HttpURLConnection ? connection.getContentLength() : -1;
      InputStream in = connection.getInputStream();
      return this.read(in, size);
   }

   private InputStream read(InputStream in, int size) throws IOException {
      try {
         return this.readIntoBuffer(in, size);
      } catch (IOException var5) {
         this.log("Could not read into buffer", var5);

         try {
            return this.readIntoFile(in);
         } catch (IOException var4) {
            this.log("Could not read into file", var4);
            return in;
         }
      }
   }

   private InputStream readIntoBuffer(InputStream in, final int size) throws IOException {
      if (size < 1) {
         throw new IOException("input too small");
      } else if (GLOBAL_BUFFER.addAndGet((long)size) > 10485760L) {
         throw new IOException("buffer is full");
      } else {
         byte[] buffer = new byte[size];
         IOUtils.read(in, buffer);
         return new FilterInputStream(new ByteArrayInputStream(buffer)) {
            public void close() throws IOException {
               RepoList.GLOBAL_BUFFER.addAndGet((long)(-size));
               super.close();
            }
         };
      }
   }

   private InputStream readIntoFile(InputStream in) throws IOException {
      if (GLOBAL_BUFFER.addAndGet(8192L) > 10485760L) {
         throw new IOException("buffer is full");
      } else {
         final File temp = File.createTempFile("tlauncher-repo", (String)null);
         temp.deleteOnExit();
         FileOutputStream out = null;

         try {
            out = new FileOutputStream(temp);
            IOUtils.copy((InputStream)(new BufferedInputStream(in, 8192)), (OutputStream)out);
         } finally {
            GLOBAL_BUFFER.addAndGet(-8192L);
            U.close(out);
         }

         return new FilterInputStream(new FileInputStream(temp)) {
            public void close() throws IOException {
               super.close();
               temp.delete();
            }
         };
      }
   }

   protected void add(IRepo repo) {
      synchronized(this.sync) {
         if (this.list.contains(repo)) {
            throw new IllegalArgumentException("repo already added");
         } else {
            this.list.add(0, repo);
            this.relevant = this.makeRelevantRepoList();
         }
      }
   }

   protected void addAll(IRepo... repos) {
      synchronized(this.sync) {
         IRepo[] var3 = (IRepo[])U.requireNotNull(repos, "repos");
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            IRepo repo = var3[var5];
            this.add(repo);
         }

      }
   }

   public void markInvalid(IRepo repo) {
      synchronized(this.sync) {
         int index = this.list.indexOf(repo);
         if (index != -1 && index != this.list.size() - 1) {
            this.list.add(repo);
            this.list.remove(repo);
            this.relevant = this.makeRelevantRepoList();
         }
      }
   }

   protected RepoList.RelevantRepoList makeRelevantRepoList() {
      return new RepoList.RelevantRepoList();
   }

   protected final void log(Object... o) {
      U.log(this.prefix, o);
   }

   public class RelevantRepoList {
      private final List repoList;

      protected RelevantRepoList() {
         synchronized(RepoList.this.sync) {
            this.repoList = Collections.unmodifiableList(new ArrayList(RepoList.this.list));
         }
      }

      public List getList() {
         return this.repoList;
      }

      public IRepo getFirst() {
         return this.repoList.isEmpty() ? null : (IRepo)this.repoList.get(0);
      }
   }
}
