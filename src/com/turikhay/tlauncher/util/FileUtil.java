package com.turikhay.tlauncher.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;

public class FileUtil {
   public static void saveFile(File file, String text) throws IOException {
      FileWriter fstream = new FileWriter(file);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(text);
      out.close();
   }

   public static String readFile(File file) throws IOException {
      String toret = "";
      FileReader file_r = new FileReader(file);
      BufferedReader buff = new BufferedReader(file_r);
      boolean eof = false;

      while(!eof) {
         String line = buff.readLine();
         if (line == null) {
            eof = true;
         } else {
            toret = toret + "\n" + line;
         }
      }

      buff.close();
      if (toret.length() > 0) {
         toret = toret.substring(1);
      }

      return toret;
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

            for(int i = 0; i < b.length; ++i) {
               result = result + Integer.toString((b[i] & 255) + 256, 16).substring(1);
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
}
