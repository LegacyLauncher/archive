package com.turikhay.tlauncher.component.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.updater.AssetIndex.AssetObject;
import net.minecraft.launcher.versions.CompleteVersion;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.component.LauncherComponent;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.DownloadableContainer;
import com.turikhay.tlauncher.minecraft.repository.AssetsRepository;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class AssetsManager implements LauncherComponent {
	private final ComponentManager manager;
	
	private final Gson gson;
	private final Object assetsFlushLock;
	
	public AssetsManager(ComponentManager manager){
		this.manager = manager;
		
		this.gson = new Gson();
		this.assetsFlushLock = new Object();
	}
	
	public DownloadableContainer downloadResources(DownloadableContainer job, CompleteVersion version, List<AssetObject> list, boolean force) throws IOException {
		File baseDirectory = manager.getVersionManager().getLocalList().getBaseDirectory();

		job.addAll(getResourceFiles(version, baseDirectory, list));

		return job;
	}
	
	private Set<Downloadable> getResourceFiles(CompleteVersion version, File baseDirectory, List<AssetObject> list) {
		Set<Downloadable> result = new HashSet<Downloadable>();
		    
		File objectsFolder = new File(baseDirectory, "assets/objects");
		    
		for(AssetObject object : list){
			// TODO set repo
			String filename = object.getFilename(), url = AssetsRepository.DEFAULT.getSelectedRepo() + filename;
			File file = new File(objectsFolder, filename);
		        
			try {
				Downloadable d = new Downloadable(url, file, false);
				d.setFast(true);
				  
				result.add(d);
			}
			catch(IOException e){ log("Cannot create downloadable resource from URL:", url, e); }
		}
		  
		return result;
	}
	
	
	
	public List<AssetObject> getResourceFiles(CompleteVersion version, File baseDirectory, boolean local) {	  
		List<AssetObject> list = null;
		  
		if(!local)
			try{
				list = getRemoteResourceFilesList(version, baseDirectory, true);
			} catch(Exception e) { log("Cannot get remote assets list. Trying to use the local one.", e); }
		  
		  if(list == null)
			  list = getLocalResourceFilesList(version, baseDirectory);
		  
		  if(list == null)
			  try{
				  list = getRemoteResourceFilesList(version, baseDirectory, true);
			  } catch(Exception e) { log("Gave up trying to get assets list.", e); }
		  
		  return list;
	}
	
	private List<AssetObject> getLocalResourceFilesList(CompleteVersion version, File baseDirectory) {
		List<AssetObject> result = new ArrayList<AssetObject>();
		  
		String indexName = version.getAssets();
		  
		File indexesFolder = new File(baseDirectory, "assets/indexes/");
		File indexFile = new File(indexesFolder, indexName + ".json");
		  
		log("Reading indexes from file", indexFile);
		  
		String json;
		try { json = FileUtil.readFile(indexFile); } catch (Exception e) {
			log("Cannot read local resource files list for index:", indexName, e);
			return null;
		}
		  
		AssetIndex index = null;
		  
		try{ index = this.gson.fromJson(json, AssetIndex.class); }
		catch(JsonSyntaxException e){ log("JSON file is invalid", e); }
		  
		if(index == null) {
			log("Cannot read data from JSON file.");
			return null;
		}
		  
		for(AssetObject object : index.getUniqueObjects())
			result.add(object);
	      
		return result;
	}
	
	private List<AssetObject> getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {
		List<AssetObject> result = new ArrayList<AssetObject>();
		  
		String indexName = version.getAssets();
		if(indexName == null) indexName = "legacy";
		  
		File assets = new File(baseDirectory, "assets");
		File indexesFolder = new File(assets, "indexes");
		  
		File indexFile = new File(indexesFolder, indexName + ".json");
	    
		URL indexUrl = new URL("https://s3.amazonaws.com/Minecraft.Download/indexes/" + indexName + ".json");
		InputStream inputStream = indexUrl.openStream();
		  
		log("Reading indexes from URL", indexUrl);
			  
		String json = IOUtils.toString(inputStream);
		if(save)
			synchronized(assetsFlushLock) {
				FileUtil.writeFile(indexFile, json);
			}
		  
		AssetIndex index = this.gson.fromJson(json, AssetIndex.class);
		
		for (AssetObject object : index.getUniqueObjects())
			result.add(object);
	      
		return result;
	}
	
	public List<AssetObject> checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast){
		log("Checking resources...");
		  
		List<AssetObject> list, r = new ArrayList<AssetObject>();
		  
		if(local) list = getLocalResourceFilesList(version, baseDirectory);
		else list = getResourceFiles(version, baseDirectory, true);
		  
		if(list == null){
			log("Cannot get assets list. Aborting.");
			return r;
		}
		  
		log("Fast comparing:", fast);
		  
		for(AssetObject resource : list)
			if(!checkResource(baseDirectory, resource, fast))
				r.add(resource);
		  
		return r;
	}
	public List<AssetObject> checkResources(CompleteVersion version, boolean fast){ return checkResources(version, manager.getVersionLists().getLocal().getBaseDirectory(), false, fast); }
	  
	private boolean checkResource(File baseDirectory, AssetObject local, boolean fast){
		String path = local.getFilename();
		  
		File file = new File(baseDirectory, "assets/objects/" + path);
		long size = file.length();
		
		if(!file.isFile() || size == 0L) return false;
		  
		if(fast) return true;
		
		// Checking size
		if(local.getSize() != size) return false;
		
		// Checking hash
		if(local.getHash() == null) return true;
		return local.getHash().equals(FileUtil.getChecksum(file, "SHA-1"));
	}
	
	protected void log(Object... w){ U.log("["+getClass().getSimpleName()+"]", w); }
}
