package com.turikhay.tlauncher.ui.images;

import java.awt.Image;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.turikhay.tlauncher.exceptions.TLauncherException;

public class ImageCache {
	private final static Map<String, Image> imageCache = Collections.synchronizedMap(new HashMap<String, Image>());
	
	public static Image getImage(String uri){
		return getImage(uri, true);
	}
	
	public static Image getImage(String uri, boolean throwIfError){
		if(uri == null)
			throw new NullPointerException("URL is NULL");
		
		if(imageCache.containsKey(uri))
			return imageCache.get(uri);
		
		try{
			Image image = ImageIO.read(getRes(uri));
			imageCache.put(uri, image);
			
			return image;
		} catch(Exception e) {
			if(throwIfError)
				throw new TLauncherException("Cannot load required image:" + uri, e);
			else
				e.printStackTrace();
		}
		
		return null;
	}
	
	public static URL getRes(String uri){
		return ImageCache.class.getResource(uri);
	}
}
