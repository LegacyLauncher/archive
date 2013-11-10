package com.turikhay.tlauncher.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
   public static void saveFile(File file, String text) throws IOException {
      FileWriter fstream = new FileWriter(file);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(text);
      out.close();
   }

   public static String readFile(File file) throws IOException {
      StringBuilder toret = new StringBuilder();
      boolean first = true;
      FileReader file_r = new FileReader(file);
      BufferedReader buff = new BufferedReader(file_r);
      boolean eof = false;

      while(!eof) {
         String line = buff.readLine();
         if (line == null) {
            eof = true;
         } else if (!first) {
            toret.append("\n" + line);
         } else {
            toret.append(line);
            first = false;
         }
      }

      buff.close();
      return toret.toString();
   }

   public static String getFilename(URL url) {
      String inServer = url.getPath();
      String[] folders = inServer.split("/");
      int size = folders.length;
      return size == 0 ? "" : folders[size - 1];
   }

   public static byte[] createChecksum(File file) {
      FileInputStream fis = null;

      try {
         fis = new FileInputStream(file);
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
      FileOutputStream os = null;

      try {
         is = new FileInputStream(source);
         os = new FileOutputStream(dest);
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
      if (file == null) {
         return false;
      } else {
         if (!file.isFile()) {
            if (file.getParentFile() != null) {
               file.getParentFile().mkdirs();
            }

            file.createNewFile();
         }

         return true;
      }
   }

   public static boolean createFile(String file) throws IOException {
      return file == null ? false : createFile(new File(file));
   }
}
