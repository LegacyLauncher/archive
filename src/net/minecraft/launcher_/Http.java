package net.minecraft.launcher_;

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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.util.U;

public class Http
{
  public static String buildQuery(Map<String, Object> query)
  {
    StringBuilder builder = new StringBuilder();

    for (Entry<String, Object> entry : query.entrySet()) {
      if (builder.length() > 0) {
        builder.append('&');
      }
      try
      {
        builder.append(URLEncoder.encode((String)entry.getKey(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        U.log("Unexpected exception building query", e);
      }

      if (entry.getValue() != null) {
        builder.append('=');
        try {
          builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
          U.log("Unexpected exception building query", e);
        }
      }
    }

    return builder.toString();
  }

  public static String performPost(URL url, Map<String, Object> query, Proxy proxy) throws IOException {
    return performPost(url, buildQuery(query), proxy, "application/x-www-form-urlencoded", false);
  }

  public static String performGet(URL url, Proxy proxy) throws IOException {	
    HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
    
    connection.setConnectTimeout(15000);
    connection.setReadTimeout(60000);
    connection.setRequestMethod("GET");

    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

    StringBuilder response = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
      response.append(line);
      response.append('\r');
    }

    reader.close();
    return response.toString();
  }

  public static URL concatenateURL(URL url, String args) throws MalformedURLException {
    if ((url.getQuery() != null) && (url.getQuery().length() > 0)) {
      return new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + args);
    }
    return new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + args);
  }
  
  public static String performPost(URL url, String parameters, Proxy proxy, String contentType, boolean returnErrorPage)
		  throws IOException {
	  HttpURLConnection connection = (HttpURLConnection)url.openConnection(proxy);
	  byte[] paramAsBytes = parameters.getBytes(Charset.forName("UTF-8"));

	  connection.setConnectTimeout(15000);
	  connection.setReadTimeout(60000);
	  connection.setRequestMethod("POST");
	  connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");

	  connection.setRequestProperty("Content-Length", "" + paramAsBytes.length);
	  connection.setRequestProperty("Content-Language", "en-US");

	  connection.setUseCaches(false);
	  connection.setDoInput(true);
	  connection.setDoOutput(true);

	  BufferedReader reader;
	  DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
	  writer.write(paramAsBytes);
	  writer.flush();
	  writer.close();
	  try
	  {
		  reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	  }
	  catch (IOException e)
	  {
		  if (returnErrorPage) {
			  InputStream stream = connection.getErrorStream();

			  if (stream != null)
				  reader = new BufferedReader(new InputStreamReader(stream));
			  else
				  throw e;
		  }
		  else {
			  throw e;
		  }
	  }
	  StringBuilder response = new StringBuilder();
	  String line;
	  while ((line = reader.readLine()) != null) {
		  response.append(line);
		  response.append('\r');
	  }

	  reader.close();
	  return response.toString();
  }

  public static URL constantURL(String input) {
	  try {
		  return new URL(input);
	  } catch (MalformedURLException e) {
		  throw new Error(e);
	  }
  }
		  
  public static String encode(String s) {
	  try {
		  return URLEncoder.encode(s, "UTF-8")
				  .replaceAll("\\+", "%20")
				  .replaceAll("\\%3A", ":")
				  .replaceAll("\\%2F", "/")
				  .replaceAll("\\%21", "!")
				  .replaceAll("\\%27", "'")
				  .replaceAll("\\%28", "(")
				  .replaceAll("\\%29", ")")
				  .replaceAll("\\%7E", "~");
	  } catch (UnsupportedEncodingException e) {}

	  return s;
  }
  
  public static String decode(String s) {
	  try {
		  return URLDecoder.decode(s, "UTF-8");
	  } catch (UnsupportedEncodingException e) {}

	  return s;
  }
}