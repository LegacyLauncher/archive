package com.turikhay.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
   private static final String DEFAULT_CHARSET = "UTF-8";

   public static Charset getCharset() {
      try {
         return Charset.forName("UTF-8");
      } catch (Exception var1) {
         var1.printStackTrace();
         return null;
      }
   }

   public static void writeFile(File file, String text) throws IOException {
      createFile(file);
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
      OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
      ow.write(text);
      ow.close();
      os.close();
   }

   private static String readFile(File file, String charset) throws IOException {
      if (file == null) {
         throw new NullPointerException("File is NULL!");
      } else if (!file.exists()) {
         return null;
      } else {
         BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
         InputStreamReader reader = new InputStreamReader(bis, charset);
         StringBuilder b = new StringBuilder();

         while(reader.ready()) {
            b.append((char)reader.read());
         }

         reader.close();
         bis.close();
         return b.toString();
      }
   }

   public static String readFile(File file) throws IOException {
      return readFile(file, "UTF-8");
   }

   public static String getFilename(String path) {
      String[] folders = path.split("/");
      int size = folders.length;
      return size == 0 ? "" : folders[size - 1];
   }

   public static String getFilename(URL url) {
      return getFilename(url.getPath());
   }

   public static String getDigest(File file, String algorithm, int hashLength) {
      DigestInputStream stream = null;

      try {
         stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
         byte[] buffer = new byte[65536];

         int read;
         do {
            read = stream.read(buffer);
         } while(read > 0);

         return String.format("%1$0" + hashLength + "x", new BigInteger(1, stream.getMessageDigest().digest()));
      } catch (Exception var9) {
      } finally {
         close(stream);
      }

      return null;
   }

   public static String copyAndDigest(InputStream inputStream, OutputStream outputStream, String algorithm, int hashLength) throws IOException {
      MessageDigest digest;
      try {
         digest = MessageDigest.getInstance(algorithm);
      } catch (NoSuchAlgorithmException var10) {
         throw new RuntimeException("Missing Digest. " + algorithm, var10);
      }

      byte[] buffer = new byte[65536];

      try {
         for(int read = inputStream.read(buffer); read >= 1; read = inputStream.read(buffer)) {
            digest.update(buffer, 0, read);
            outputStream.write(buffer, 0, read);
         }
      } finally {
         close(inputStream);
         close(outputStream);
      }

      return String.format("%1$0" + hashLength + "x", new BigInteger(1, digest.digest()));
   }

   private static byte[] createChecksum(File file, String algorithm) {
      BufferedInputStream fis = null;

      try {
         fis = new BufferedInputStream(new FileInputStream(file));
         byte[] buffer = new byte[1024];
         MessageDigest complete = MessageDigest.getInstance(algorithm);

         int numRead;
         do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
               complete.update(buffer, 0, numRead);
            }
         } while(numRead != -1);

         byte[] var7 = complete.digest();
         return var7;
      } catch (Exception var10) {
      } finally {
         close(fis);
      }

      return null;
   }

   public static String getChecksum(File file, String algorithm) {
      if (file == null) {
         return null;
      } else if (!file.exists()) {
         return null;
      } else {
         byte[] b = createChecksum(file, algorithm);
         if (b == null) {
            return null;
         } else {
            StringBuilder result = new StringBuilder();
            byte[] var7 = b;
            int var6 = b.length;

            for(int var5 = 0; var5 < var6; ++var5) {
               byte cb = var7[var5];
               result.append(Integer.toString((cb & 255) + 256, 16).substring(1));
            }

            return result.toString();
         }
      }
   }

   private static void close(Closeable a) {
      try {
         a.close();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }

   public static File getRunningJar() {
      try {
         return new File(URLDecoder.decode(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
      } catch (UnsupportedEncodingException var1) {
         throw new RuntimeException("Cannot get running file!", var1);
      }
   }

   public static void copyFile(File source, File dest, boolean replace) throws IOException {
      if (dest.exists()) {
         if (!replace) {
            return;
         }
      } else {
         dest.createNewFile();
      }

      InputStream is = null;
      BufferedOutputStream os = null;

      try {
         is = new BufferedInputStream(new FileInputStream(source));
         os = new BufferedOutputStream(new FileOutputStream(dest));
         byte[] buffer = new byte[1024];

         int length;
         while((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
         }
      } finally {
         if (is != null) {
            is.close();
         }

         if (os != null) {
            os.close();
         }

      }

   }

   public static void deleteFile(File file) {
      if (!file.delete()) {
         file.deleteOnExit();
      }

   }

   public static void deleteFile(String path) {
      deleteFile(new File(path));
   }

   public static File makeTemp(File file) throws IOException {
      createFile(file);
      file.deleteOnExit();
      return file;
   }

   public byte[] getFile(File archive, String requestedFile) throws IOException {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ZipInputStream in = null;

      try {
         in = new ZipInputStream(new FileInputStream(archive));

         while(true) {
            ZipEntry entry;
            do {
               if ((entry = in.getNextEntry()) == null) {
                  return out.toByteArray();
               }
            } while(!entry.getName().equals(requestedFile));

            byte[] buf = new byte[1024];

            int len;
            while((len = in.read(buf)) > 0) {
               out.write(buf, 0, len);
            }
         }
      } finally {
         if (in != null) {
            in.close();
         }

         out.close();
      }
   }

   public static boolean createFolder(File dir) throws IOException {
      if (dir == null) {
         throw new NullPointerException();
      } else if (dir.isDirectory()) {
         return false;
      } else if (!dir.mkdirs()) {
         throw new IOException("Cannot create folders: " + dir.getAbsolutePath());
      } else if (!dir.canWrite()) {
         throw new IOException("Ceated directory is not accessible: " + dir.getAbsolutePath());
      } else {
         return true;
      }
   }

   public static boolean createFolder(String dir) throws IOException {
      return dir == null ? false : createFolder(new File(dir));
   }

   public static boolean folderExists(String path) {
      if (path == null) {
         return false;
      } else {
         File folder = new File(path);
         return folder.isDirectory();
      }
   }

   public static boolean fileExists(String path) {
      if (path == null) {
         return false;
      } else {
         File file = new File(path);
         return file.isFile();
      }
   }

   private static boolean createFile(File file) throws IOException {
      if (file.isFile()) {
         return false;
      } else {
         if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
         }

         return file.createNewFile();
      }
   }

   public static boolean createFile(String file) throws IOException {
      return createFile(new File(file));
   }

   public static void unZip(File zip, File folder, boolean replace) throws IOException {
      createFolder(folder);
      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
      byte[] buffer = new byte[1024];

      while(true) {
         ZipEntry ze;
         while((ze = zis.getNextEntry()) != null) {
            String fileName = ze.getName();
            File newFile = new File(folder, fileName);
            if (!replace && newFile.isFile()) {
               U.log("[UnZip] File exists:", newFile.getAbsoluteFile());
            } else {
               U.log("[UnZip]", newFile.getAbsoluteFile());
               createFile(newFile);
               BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(newFile));

               int len;
               while((len = zis.read(buffer)) > 0) {
                  fos.write(buffer, 0, len);
               }

               fos.close();
            }
         }

         zis.closeEntry();
         zis.close();
         return;
      }
   }

   public static void removeFromZip(File zipFile, List files) throws IOException {
      File tempFile = File.createTempFile(zipFile.getName(), (String)null);
      tempFile.delete();
      tempFile.deleteOnExit();
      boolean renameOk = zipFile.renameTo(tempFile);
      if (!renameOk) {
         throw new IOException("Could not rename the file " + zipFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
      } else {
         byte[] buf = new byte[1024];
         ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
         ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));

         for(ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
            String name = entry.getName();
            if (!files.contains(name)) {
               zout.putNextEntry(new ZipEntry(name));

               int len;
               while((len = zin.read(buf)) > 0) {
                  zout.write(buf, 0, len);
               }
            }
         }

         zin.close();
         zout.close();
         tempFile.delete();
      }
   }

   private static String getResource(URL resource, String charset) throws IOException {
      InputStream is = new BufferedInputStream(resource.openStream());
      InputStreamReader reader = new InputStreamReader(is, charset);
      StringBuilder b = new StringBuilder();

      while(reader.ready()) {
         b.append((char)reader.read());
      }

      reader.close();
      return b.toString();
   }

   public static String getResource(URL resource) throws IOException {
      return getResource(resource, "UTF-8");
   }

   private static String getFolder(URL url, String separator) {
      String[] folders = url.toString().split(separator);
      String s = "";

      for(int i = 0; i < folders.length - 1; ++i) {
         s = s + folders[i] + separator;
      }

      return s;
   }

   public static String getFolder(URL url) {
      return getFolder(url, "/");
   }

   private static File getNeighborFile(File file, String filename) {
      File parent = file.getParentFile();
      if (parent == null) {
         parent = new File("/");
      }

      return new File(parent, filename);
   }

   public static File getNeighborFile(String filename) {
      return getNeighborFile(getRunningJar(), filename);
   }
}
