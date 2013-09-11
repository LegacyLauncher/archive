package net.minecraft.launcher_;

import com.turikhay.tlauncher.util.U;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Http {
   public static String buildQuery(Map query) {
      StringBuilder builder = new StringBuilder();
      Iterator var3 = query.entrySet().iterator();

      while(var3.hasNext()) {
         Entry entry = (Entry)var3.next();
         if (builder.length() > 0) {
            builder.append('&');
         }

         try {
            builder.append(URLEncoder.encode((String)entry.getKey(), "UTF-8"));
         } catch (UnsupportedEncodingException var6) {
            U.log("Unexpected exception building query", var6);
         }

         if (entry.getValue() != null) {
            builder.append('=');

            try {
               builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException var5) {
               U.log("Unexpected exception building query", var5);
            }
         }
      }

      return builder.toString();
   }

   public static String performPost(URL url, Map query, Proxy proxy) throws IOException {
      return performPost(url, buildQuery(query), proxy, "application/x-www-form-urlencoded", false);
   }

   public static String performGet(URL url, Proxy proxy) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
      connection.setRequestMethod("GET");
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      StringBuilder response = new StringBuilder();

      String line;
      while((line = reader.readLine()) != null) {
         response.append(line);
         response.append('\r');
      }

      reader.close();
      return response.toString();
   }

   public static URL concatenateURL(URL url, String args) throws MalformedURLException {
      return url.getQuery() != null && url.getQuery().length() > 0 ? new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + args) : new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + args);
   }

   public static String performPost(URL url, String parameters, Proxy proxy, String contentType, boolean returnErrorPage) throws IOException {
      HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
      byte[] paramAsBytes = parameters.getBytes(Charset.forName("UTF-8"));
      connection.setConnectTimeout(15000);
      connection.setReadTimeout(15000);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
      connection.setRequestProperty("Content-Length", "" + paramAsBytes.length);
      connection.setRequestProperty("Content-Language", "en-US");
      connection.setUseCaches(false);
      connection.setDoInput(true);
      connection.setDoOutput(true);
      DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
      writer.write(paramAsBytes);
      writer.flush();
      writer.close();

      BufferedReader reader;
      try {
         reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      } catch (IOException var11) {
         if (!returnErrorPage) {
            throw var11;
         }

         InputStream stream = connection.getErrorStream();
         if (stream == null) {
            throw var11;
         }

         reader = new BufferedReader(new InputStreamReader(stream));
      }

      StringBuilder response = new StringBuilder();

      String line;
      while((line = reader.readLine()) != null) {
         response.append(line);
         response.append('\r');
      }

      reader.close();
      return response.toString();
   }

   public static URL constantURL(String input) {
      try {
         return new URL(input);
      } catch (MalformedURLException var2) {
         throw new Error(var2);
      }
   }
}
