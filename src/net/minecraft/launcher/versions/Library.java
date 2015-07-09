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
import java.util.Collections;
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
   protected static final String FORGE_LIB_SUFFIX = ".pack.xz";
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

   static {
      HashMap map = new HashMap();
      map.put("platform", OS.CURRENT.getName());
      map.put("arch", OS.Arch.CURRENT.asString());
      SUBSTITUTOR = new StrSubstitutor(map);
   }

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

   public List getRules() {
      return this.rules == null ? null : Collections.unmodifiableList(this.rules);
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

         if (lastAction == Rule.Action.ALLOW) {
            return true;
         } else {
            return false;
         }
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
         String nativePath = this.natives != null && this.appliesToCurrentEnvironment() ? (String)this.natives.get(os) : null;
         path = this.getArtifactPath(nativePath) + (isForge ? ".pack.xz" : "");
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
         File tempFile = new File(file.getAbsolutePath() + ".pack.xz");
         return new Library.ForgeLibDownloadable(path, tempFile, file);
      } else {
         return repo == null ? new Library.LibraryDownloadable(path, file, (Library.LibraryDownloadable)null, (Library.LibraryDownloadable)null) : new Library.LibraryDownloadable(repo, path, file, (Library.LibraryDownloadable)null);
      }
   }

   private static synchronized void unpackLibrary(File library, File output, boolean retryOnOutOfMemory) throws IOException {
      forgeLibLog("Synchronized unpacking:", library);
      output.delete();
      InputStream in = null;
      JarOutputStream jos = null;

      label61: {
         try {
            InputStream in = new FileInputStream(library);
            in = new XZInputStream(in);
            forgeLibLog("Decompressing...");
            byte[] decompressed = readFully(in);
            forgeLibLog("Decompressed successfully");
            String end = new String(decompressed, decompressed.length - 4, 4);
            if (!end.equals("SIGN")) {
               throw new RetryDownloadException("signature missing");
            }

            forgeLibLog("Signature matches!");
            int x = decompressed.length;
            int len = decompressed[x - 8] & 255 | (decompressed[x - 7] & 255) << 8 | (decompressed[x - 6] & 255) << 16 | (decompressed[x - 5] & 255) << 24;
            forgeLibLog("Now getting checksums...");
            byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);
            FileUtil.createFile(output);
            FileOutputStream jarBytes = new FileOutputStream(output);
            jos = new JarOutputStream(jarBytes);
            forgeLibLog("Now unpacking...");
            Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);
            forgeLibLog("Unpacked successfully");
            forgeLibLog("Now trying to write checksums...");
            jos.putNextEntry(new JarEntry("checksums.sha1"));
            jos.write(checksums);
            jos.closeEntry();
            forgeLibLog("Now finishing...");
            break label61;
         } catch (OutOfMemoryError var15) {
            forgeLibLog("Out of memory, oops", var15);
            U.gc();
            if (!retryOnOutOfMemory) {
               throw var15;
            }

            forgeLibLog("Retrying...");
            close(in, jos);
            FileUtil.deleteFile(library);
            unpackLibrary(library, output, false);
         } catch (IOException var16) {
            output.delete();
            throw var16;
         } finally {
            close(in, jos);
            FileUtil.deleteFile(library);
         }

         return;
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

   public class ForgeLibDownloadable extends Library.LibraryDownloadable {
      private final File unpacked;

      public ForgeLibDownloadable(String url, File packedLib, File unpackedLib) {
         super((String)url, (File)packedLib, (Library.LibraryDownloadable)null);
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

      public Library getLibrary() {
         return Library.this;
      }

      // $FF: synthetic method
      LibraryDownloadable(String var2, File var3, Library.LibraryDownloadable var4) {
         this(var2, var3);
      }

      // $FF: synthetic method
      LibraryDownloadable(String var2, File var3, Library.LibraryDownloadable var4, Library.LibraryDownloadable var5) {
         this(var2, var3);
      }

      // $FF: synthetic method
      LibraryDownloadable(Repository var2, String var3, File var4, Library.LibraryDownloadable var5) {
         this((Repository)var2, (String)var3, (File)var4);
      }
   }
}
