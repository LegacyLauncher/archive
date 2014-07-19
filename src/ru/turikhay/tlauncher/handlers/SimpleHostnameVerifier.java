package ru.turikhay.tlauncher.handlers;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class SimpleHostnameVerifier implements HostnameVerifier {
	private static final SimpleHostnameVerifier instance = new SimpleHostnameVerifier();
	
	public static SimpleHostnameVerifier getInstance() {
		return instance;
	}
	
	private SimpleHostnameVerifier() {}

	@Override
	public boolean verify(String hostname, SSLSession session) {
		return true;
	}

}
