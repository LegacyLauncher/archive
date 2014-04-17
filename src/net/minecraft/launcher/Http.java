package net.minecraft.launcher;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import com.turikhay.tlauncher.handlers.SimpleHostnameVerifier;
import com.turikhay.util.U;

public class Http {
	private static String buildQuery(Map<String, Object> query) {
		StringBuilder builder = new StringBuilder();

		for (Entry<String, Object> entry : query.entrySet()) {
			if (builder.length() > 0) {
				builder.append('&');
			}
			try {
				builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				U.log("Unexpected exception building query", e);
			}

			if (entry.getValue() != null) {
				builder.append('=');
				try {
					builder.append(URLEncoder.encode(entry.getValue()
							.toString(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					U.log("Unexpected exception building query", e);
				}
			}
		}

		return builder.toString();
	}

	public static String performPost(URL url, Map<String, Object> query)
			throws IOException {
		return performPost(url, buildQuery(query),
				"application/x-www-form-urlencoded");
	}

	public static String performGet(URL url, int connTimeout, int readTimeout)
			throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		connection.setConnectTimeout(connTimeout);
		connection.setReadTimeout(readTimeout);
		connection.setRequestMethod("GET");
		
		HttpsURLConnection securedConnection = U.getAs(connection, HttpsURLConnection.class);
		
		if(securedConnection != null)
			securedConnection.setHostnameVerifier(SimpleHostnameVerifier.getInstance());

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));

		StringBuilder response = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}

		reader.close();
		return response.toString();
	}

	public static String performGet(String url) throws IOException {
		return performGet(constantURL(url), U.getConnectionTimeout(),
				U.getReadTimeout());
	}

	public static String performGet(URL url) throws IOException {
		return performGet(url, U.getConnectionTimeout(), U.getReadTimeout());
	}

	public static URL concatenateURL(URL url, String args)
			throws MalformedURLException {
		if ((url.getQuery() != null) && (url.getQuery().length() > 0)) {
			return new URL(url.getProtocol(), url.getHost(), url.getFile()
					+ "?" + args);
		}
		return new URL(url.getProtocol(), url.getHost(), url.getFile() + "&"
				+ args);
	}

	public static String performPost(URL url, String parameters,
			String contentType) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		byte[] paramAsBytes = parameters.getBytes(Charset.forName("UTF-8"));

		connection.setConnectTimeout(U.getConnectionTimeout());
		connection.setReadTimeout(U.getReadTimeout());
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", contentType
				+ "; charset=utf-8");

		connection.setRequestProperty("Content-Length", ""
				+ paramAsBytes.length);
		connection.setRequestProperty("Content-Language", "en-US");

		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		
		HttpsURLConnection securedConnection = U.getAs(connection, HttpsURLConnection.class);
		
		if(securedConnection != null)
			securedConnection.setHostnameVerifier(SimpleHostnameVerifier.getInstance());

		BufferedReader reader;
		DataOutputStream writer = new DataOutputStream(
				connection.getOutputStream());
		writer.write(paramAsBytes);
		writer.flush();
		writer.close();

		reader = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));

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
			return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20")
					.replaceAll("\\%3A", ":").replaceAll("\\%2F", "/")
					.replaceAll("\\%21", "!").replaceAll("\\%27", "'")
					.replaceAll("\\%28", "(").replaceAll("\\%29", ")")
					.replaceAll("\\%7E", "~");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not supported.", e);
		}
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 is not supported.", e);
		}
	}
}