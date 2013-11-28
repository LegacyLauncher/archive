package com.turikhay.tlauncher.util;

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
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
   public static final String DEFAULT_CHARSET = "UTF-8";

   public static void writeFile(File file, String text) throws IOException {
      createFile(file);
      FileOutputStream os = new FileOutputStream(file);
      OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
      ow.write(text);
      ow.close();
      os.close();
   }

   public static String readFile(File file, String charset) throws IOException {
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

   public static String getFilename(URL url) {
      String inServer = url.getPath();
      String[] folders = inServer.split("/");
      int size = folders.length;
      return size == 0 ? "" : folders[size - 1];
   }

   public static byte[] createChecksum(File file) {
      BufferedInputStream fis = null;

      try {
         fis = new BufferedInputStream(new FileInputStream(file));
         byte[] buffer = new byte[1024];
         MessageDigest complete = MessageDigest.getInstance("MD5");

         int numRead;
         do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
               complete.update(buffer, 0, numRead);
            }
         } while(numRead != -1);

         byte[] var6 = complete.digest();
         return var6;
      } catch (Exception var9) {
      } finally {
         close(fis);
      }

      return null;
   }

   public static String getMD5Checksum(File file) {
      if (file == null) {
         return null;
      } else if (!file.exists()) {
         return null;
      } else {
         byte[] b = createChecksum(file);
         if (b == null) {
            return null;
         } else {
            String result = "";
            byte[] var6 = b;
            int var5 = b.length;

            for(int var4 = 0; var4 < var5; ++var4) {
               byte cb = var6[var4];
               result = result + Integer.toString((cb & 255) + 256, 16).substring(1);
            }

            return result;
         }
      }
   }

   public static void close(Closeable a) {
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
         var1.printStackTrace();
         return null;
      }
   }

   public static void copyFile(File source, File dest, boolean replace) throws IOException {
      if (dest.exists()) {
         if (replace) {
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
      } else {
         if (!dir.isDirectory()) {
            dir.mkdirs();
         }

         if (!dir.isDirectory()) {
            throw new IOException("Cannot create folder!");
         } else {
            return true;
         }
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

   public static boolean createFile(File file) throws IOException {
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
      ZipInputStream zis = new ZipInputStream(new FileInputStream(zip));
      byte[] buffer = new byte[1024];

      ZipEntry ze;
      while((ze = zis.getNextEntry()) != null) {
         String fileName = ze.getName();
         File newFile = new File(folder, fileName);
         if (!replace && newFile.isFile()) {
            U.log("[UnZip] File exists:", newFile.getAbsoluteFile());
            break;
         }

         U.log("[UnZip]", newFile.getAbsoluteFile());
         createFile(newFile);
         FileOutputStream fos = new FileOutputStream(newFile);

         int len;
         while((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
         }

         fos.close();
      }

      zis.closeEntry();
      zis.close();
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
         ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
         ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zipFile));

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

   public static String getResource(URL resource, String charset) throws IOException {
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

   public static String getFolder(URL url, char separator) {
      String[] folders = url.toString().split(String.valueOf(separator));
      String s = "";

      for(int i = 0; i < folders.length - 1; ++i) {
         s = s + folders[i] + separator;
      }

      return s;
   }

   public static String getFolder(URL url) {
      return getFolder(url, File.separatorChar);
   }
}
