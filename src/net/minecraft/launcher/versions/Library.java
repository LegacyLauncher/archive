package net.minecraft.launcher.versions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.tukaani.xz.XZInputStream;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class Library {
   protected static final StrSubstitutor SUBSTITUTOR;
   protected String name;
   protected List rules;
   protected Map natives;
   protected ExtractRules extract;
   protected String url;
   protected String exact_url;
   protected String packed;
   protected String checksum;
   protected List deleteEntries;

   public boolean equals(Object o) {
      if (o != null && o instanceof Library) {
         Library lib = (Library)o;
         return this.name == null ? lib.name == null : this.name.equalsIgnoreCase(lib.name);
      } else {
         return false;
      }
   }

   public String getName() {
      return this.name;
   }

   public String getPlainName() {
      String[] split = this.name.split(":", 3);
      return split[0] + "." + split[1];
   }

   public boolean appliesToCurrentEnvironment() {
      if (this.rules == null) {
         return true;
      } else {
         Rule.Action lastAction = Rule.Action.DISALLOW;
         Iterator var3 = this.rules.iterator();

         while(var3.hasNext()) {
            Rule rule = (Rule)var3.next();
            Rule.Action action = rule.getAppliedAction();
            if (action != null) {
               lastAction = action;
            }
         }

         return lastAction == Rule.Action.ALLOW;
      }
   }

   public Map getNatives() {
      return this.natives;
   }

   public ExtractRules getExtractRules() {
      return this.extract;
   }

   public String getChecksum() {
      return this.checksum;
   }

   public List getDeleteEntriesList() {
      return this.deleteEntries;
   }

   String getArtifactBaseDir() {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         return String.format("%s/%s/%s", parts[0].replaceAll("\\.", "/"), parts[1], parts[2]);
      }
   }

   public String getArtifactPath() {
      return this.getArtifactPath((String)null);
   }

   public String getArtifactPath(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
      } else {
         return String.format("%s/%s", this.getArtifactBaseDir(), this.getArtifactFilename(classifier));
      }
   }

   String getArtifactFilename(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         String result;
         if (classifier == null) {
            result = String.format("%s-%s.jar", parts[1], parts[2]);
         } else {
            result = String.format("%s-%s%s.jar", parts[1], parts[2], "-" + classifier);
         }

         return SUBSTITUTOR.replace(result);
      }
   }

   public String toString() {
      return "Library{name='" + this.name + '\'' + ", rules=" + this.rules + ", natives=" + this.natives + ", extract=" + this.extract + ", packed='" + this.packed + "'}";
   }

   public Downloadable getDownloadable(Repository versionSource, File file, OS os) {
      Repository repo = null;
      boolean isForge = "forge".equals(this.packed);
      String path;
      if (this.exact_url == null) {
         String tempFile = this.natives != null && this.appliesToCurrentEnvironment() ? (String)this.natives.get(os) : null;
         path = this.getArtifactPath(tempFile) + (isForge ? ".pack.xz" : "");
         if (this.url == null) {
            repo = Repository.LIBRARY_REPO;
         } else if (this.url.startsWith("/")) {
            repo = versionSource;
            path = this.url.substring(1) + path;
         } else {
            path = this.url + path;
         }
      } else {
         path = this.exact_url;
      }

      if (isForge) {
         File tempFile1 = new File(file.getAbsolutePath() + ".pack.xz");
         return new Library.ForgeLibDownloadable(path, tempFile1, file);
      } else {
         return repo == null ? new Library.LibraryDownloadable(path, file) : new Library.LibraryDownloadable(repo, path, file);
      }
   }

   private static synchronized void unpackLibrary(File library, File output, boolean retryOnOutOfMemory) throws IOException {
      forgeLibLog("Synchronized unpacking:", library);
      output.delete();
      XZInputStream in = null;
      JarOutputStream jos = null;

      try {
         FileInputStream in1 = new FileInputStream(library);
         in = new XZInputStream(in1);
         forgeLibLog("Decompressing...");
         byte[] e = readFully(in);
         forgeLibLog("Decompressed successfully");
         String end = new String(e, e.length - 4, 4);
         if (!end.equals("SIGN")) {
            throw new RetryDownloadException("signature missing");
         }

         forgeLibLog("Signature matches!");
         int x = e.length;
         int len = e[x - 8] & 255 | (e[x - 7] & 255) << 8 | (e[x - 6] & 255) << 16 | (e[x - 5] & 255) << 24;
         forgeLibLog("Now getting checksums...");
         byte[] checksums = Arrays.copyOfRange(e, e.length - len - 8, e.length - 8);
         FileUtil.createFile(output);
         FileOutputStream jarBytes = new FileOutputStream(output);
         jos = new JarOutputStream(jarBytes);
         forgeLibLog("Now unpacking...");
         Pack200.newUnpacker().unpack(new ByteArrayInputStream(e), jos);
         forgeLibLog("Unpacked successfully");
         forgeLibLog("Now trying to write checksums...");
         jos.putNextEntry(new JarEntry("checksums.sha1"));
         jos.write(checksums);
         jos.closeEntry();
         forgeLibLog("Now finishing...");
      } catch (OutOfMemoryError var16) {
         forgeLibLog("Out of memory, oops", var16);
         U.gc();
         if (retryOnOutOfMemory) {
            forgeLibLog("Retrying...");
            close(in, jos);
            FileUtil.deleteFile(library);
            unpackLibrary(library, output, false);
            return;
         }

         throw var16;
      } catch (IOException var17) {
         output.delete();
         throw var17;
      } finally {
         close(in, jos);
         FileUtil.deleteFile(library);
      }

      forgeLibLog("Done:", output);
   }

   private static synchronized void unpackLibrary(File library, File output) throws IOException {
      unpackLibrary(library, output, true);
   }

   private static void close(Closeable... closeables) {
      Closeable[] var4 = closeables;
      int var3 = closeables.length;

      for(int var2 = 0; var2 < var3; ++var2) {
         Closeable c = var4[var2];

         try {
            c.close();
         } catch (Exception var6) {
         }
      }

   }

   private static byte[] readFully(InputStream stream) throws IOException {
      byte[] data = new byte[4096];
      ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();

      int len;
      do {
         len = stream.read(data);
         if (len > 0) {
            entryBuffer.write(data, 0, len);
         }
      } while(len != -1);

      return entryBuffer.toByteArray();
   }

   private static void forgeLibLog(Object... o) {
      U.log("[ForgeLibDownloadable]", o);
   }

   static {
      HashMap map = new HashMap();
      map.put("platform", OS.CURRENT.getName());
      map.put("arch", OS.Arch.CURRENT.asString());
      SUBSTITUTOR = new StrSubstitutor(map);
   }

   public class LibraryDownloadable extends Downloadable {
      private LibraryDownloadable(Repository repo, String path, File file) {
         super(repo, path, file);
      }

      private LibraryDownloadable(String path, File file) {
         super(path, file);
      }

      public Library getDownloadableLibrary() {
         return Library.this;
      }

      protected void onComplete() throws RetryDownloadException {
         Library lib = this.getDownloadableLibrary();
         if (lib.getChecksum() != null) {
            String fileHash = FileUtil.getChecksum(this.getDestination(), "sha1");
            if (fileHash != null && !fileHash.equals(lib.getChecksum())) {
               throw new RetryDownloadException("illegal library hash. got: " + fileHash + "; expected: " + lib.getChecksum());
            }
         }

      }

      // $FF: synthetic method
      LibraryDownloadable(String x1, File x2, Object x3) {
         this(x1, x2);
      }

      // $FF: synthetic method
      LibraryDownloadable(Repository x1, String x2, File x3, Object x4) {
         this((Repository)x1, (String)x2, (File)x3);
      }
   }

   public class ForgeLibDownloadable extends Library.LibraryDownloadable {
      private final File unpacked;

      public ForgeLibDownloadable(String url, File packedLib, File unpackedLib) {
         super((String)url, (File)packedLib, (<undefinedtype>)null);
         this.unpacked = unpackedLib;
      }

      protected void onComplete() throws RetryDownloadException {
         super.onComplete();

         try {
            Library.unpackLibrary(this.getDestination(), this.unpacked);
         } catch (Throwable var2) {
            throw new RetryDownloadException("cannot unpack forge library", var2);
         }
      }
   }
}
