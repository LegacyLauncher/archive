package com.turikhay.tlauncher.minecraft.auth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

import com.turikhay.util.U;

public class AuthenticatorService {
	protected static void log(Object...o){ U.log("[AUTHSERV]", o); }
	protected static void debug(Object...o){ /*log(o);*/ }
	
	protected static HttpURLConnection createUrlConnection(URL url) throws IOException {
		Validate.notNull(url);
		debug("Opening connection to " + url);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(U.getConnectionTimeout());
		connection.setReadTimeout(U.getReadTimeout());
		connection.setUseCaches(false);
		return connection;
	}
			  
	public static String performPostRequest(URL url, String post, String contentType) throws IOException {
		Validate.notNull(url);
		Validate.notNull(post);
		Validate.notNull(contentType);
		
		HttpURLConnection connection = createUrlConnection(url);
		byte[] postAsBytes = post.getBytes(Charsets.UTF_8);
			    
		connection.setRequestProperty("Content-Type", contentType + "; charset=utf-8");
		connection.setRequestProperty("Content-Length", "" + postAsBytes.length);
		connection.setDoOutput(true);
			    
		debug("Writing POST data to " + url + ": " + post);
			    
		OutputStream outputStream = null;
		
		try {
			outputStream = connection.getOutputStream();
			IOUtils.write(postAsBytes, outputStream);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
		debug("Reading data from " + url);
			    
		InputStream inputStream = null;
		try {
			inputStream = connection.getInputStream();
			String result = IOUtils.toString(inputStream, Charsets.UTF_8);
			debug("Successful read, server response was " + connection.getResponseCode());
			debug("Response: " + result);
			return result;
		} catch (IOException e) {
			
			IOUtils.closeQuietly(inputStream);
			inputStream = connection.getErrorStream();
			if (inputStream != null) {
				debug("Reading error page from " + url);
				String result = IOUtils.toString(inputStream, Charsets.UTF_8);
				debug("Successful read, server response was " + connection.getResponseCode());
				debug("Response: " + result);
				return result;
			}
			debug("Request failed", e);
			throw e;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
			  
	public static String performGetRequest(URL url) throws IOException {
		Validate.notNull(url);
		HttpURLConnection connection = createUrlConnection(url);
			    
		debug("Reading data from " + url);
			    
		InputStream inputStream = null;
		try {
			inputStream = connection.getInputStream();
			String result = IOUtils.toString(inputStream, Charsets.UTF_8);
			debug("Successful read, server response was " + connection.getResponseCode());
			debug("Response: " + result);
			return result;
		} catch (IOException e) {
			IOUtils.closeQuietly(inputStream);
			inputStream = connection.getErrorStream();
			
			if (inputStream != null) {
				debug("Reading error page from " + url);
				String result = IOUtils.toString(inputStream, Charsets.UTF_8);
				debug("Successful read, server response was " + connection.getResponseCode());
				debug("Response: " + result);
				return result;
			}
			debug("Request failed", e);
			throw e;
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
			  
	public static URL constantURL(String url) {
		try {
			return new URL(url);
		}
		catch (MalformedURLException ex)
		{
			throw new Error("Couldn't create constant for " + url, ex);
		}
	}
			  
	public static String buildQuery(Map<String, Object> query) {
		if (query == null)
			return "";
		
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Object> entry : query.entrySet()) {
			if (builder.length() > 0) {
				builder.append('&');
			}
			
			try {
				builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			} catch (UnsupportedEncodingException e) { log("Unexpected exception building query", e); }
			
			if (entry.getValue() != null) {
				builder.append('=');
				
				try {
					builder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) { log("Unexpected exception building query", e); }
			}
		}
		return builder.toString();
	}
			  
	public static URL concatenateURL(URL url, String query) {
		try {
			if (url.getQuery() != null && url.getQuery().length() > 0)
				return new URL(url.getProtocol(), url.getHost(), url.getFile() + "&" + query);	
			return new URL(url.getProtocol(), url.getHost(), url.getFile() + "?" + query);
		}
		catch (MalformedURLException ex) {
			throw new IllegalArgumentException("Could not concatenate given URL with GET arguments!", ex);
		}
	}
}
