package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;

@ComponentDependence({VersionManager.class, VersionLists.class})
public class AssetsManager extends LauncherComponent {
   private final Gson gson = TLauncher.getGson();
   private final Object assetsFlushLock = new Object();

   public AssetsManager(ComponentManager manager) throws Exception {
      super(manager);
   }

   public DownloadableContainer downloadResources(CompleteVersion version, List list, boolean force) {
      File baseDirectory = this.manager.getLauncher().getVersionManager().getLocalList().getBaseDirectory();
      DownloadableContainer container = new DownloadableContainer();
      container.addAll((Collection)getResourceFiles(version, baseDirectory, list));
      return container;
   }

   private static Set getResourceFiles(CompleteVersion version, File baseDirectory, List list) {
      Set result = new HashSet();
      File objectsFolder = new File(baseDirectory, "assets/objects");
      Iterator var6 = list.iterator();

      while(var6.hasNext()) {
         AssetIndex.AssetObject object = (AssetIndex.AssetObject)var6.next();
         String filename = object.getFilename();
         Downloadable d = new Downloadable(Repository.ASSETS_REPO, filename, new File(objectsFolder, filename), false, true);
         result.add(d);
      }

      return result;
   }

   List getResourceFiles(CompleteVersion version, File baseDirectory, boolean local) {
      List list = null;
      if (!local) {
         try {
            list = this.getRemoteResourceFilesList(version, baseDirectory, true);
         } catch (Exception var7) {
            this.log(new Object[]{"Cannot get remote assets list. Trying to use the local one.", var7});
         }
      }

      if (list == null) {
         list = this.getLocalResourceFilesList(version, baseDirectory);
      }

      if (list == null) {
         try {
            list = this.getRemoteResourceFilesList(version, baseDirectory, true);
         } catch (Exception var6) {
            this.log(new Object[]{"Gave up trying to get assets list.", var6});
         }
      }

      return list;
   }

   private List getLocalResourceFilesList(CompleteVersion version, File baseDirectory) {
      List result = new ArrayList();
      String indexName = version.getAssets();
      File indexesFolder = new File(baseDirectory, "assets/indexes/");
      File indexFile = new File(indexesFolder, indexName + ".json");
      this.log(new Object[]{"Reading indexes from file", indexFile});

      String json;
      try {
         json = FileUtil.readFile(indexFile);
      } catch (Exception var12) {
         this.log(new Object[]{"Cannot read local resource files list for index:", indexName, var12});
         return null;
      }

      AssetIndex index = null;

      try {
         index = (AssetIndex)this.gson.fromJson(json, AssetIndex.class);
      } catch (JsonSyntaxException var11) {
         this.log(new Object[]{"JSON file is invalid", var11});
      }

      if (index == null) {
         this.log(new Object[]{"Cannot read data from JSON file."});
         return null;
      } else {
         Iterator var10 = index.getUniqueObjects().iterator();

         while(var10.hasNext()) {
            AssetIndex.AssetObject object = (AssetIndex.AssetObject)var10.next();
            result.add(object);
         }

         return result;
      }
   }

   private List getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {
      List result = new ArrayList();
      String indexName = version.getAssets();
      if (indexName == null) {
         indexName = "legacy";
      }

      File assets = new File(baseDirectory, "assets");
      File indexesFolder = new File(assets, "indexes");
      File indexFile = new File(indexesFolder, indexName + ".json");
      this.log(new Object[]{"Reading from repository..."});
      String json = Repository.OFFICIAL_VERSION_REPO.getUrl("indexes/" + indexName + ".json");
      if (save) {
         synchronized(this.assetsFlushLock) {
            FileUtil.writeFile(indexFile, json);
         }
      }

      AssetIndex index = (AssetIndex)this.gson.fromJson(json, AssetIndex.class);
      Iterator var12 = index.getUniqueObjects().iterator();

      while(var12.hasNext()) {
         AssetIndex.AssetObject object = (AssetIndex.AssetObject)var12.next();
         result.add(object);
      }

      return result;
   }

   List checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast) {
      this.log(new Object[]{"Checking resources..."});
      List r = new ArrayList();
      List list;
      if (local) {
         list = this.getLocalResourceFilesList(version, baseDirectory);
      } else {
         list = this.getResourceFiles(version, baseDirectory, true);
      }

      if (list == null) {
         this.log(new Object[]{"Cannot get assets list. Aborting."});
         return r;
      } else {
         this.log(new Object[]{"Fast comparing:", fast});
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            AssetIndex.AssetObject resource = (AssetIndex.AssetObject)var8.next();
            if (!checkResource(baseDirectory, resource, fast)) {
               r.add(resource);
            }
         }

         return r;
      }
   }

   public List checkResources(CompleteVersion version, boolean fast) {
      return this.checkResources(version, ((VersionLists)this.manager.getComponent(VersionLists.class)).getLocal().getBaseDirectory(), false, fast);
   }

   private static boolean checkResource(File baseDirectory, AssetIndex.AssetObject local, boolean fast) {
      String path = local.getFilename();
      File file = new File(baseDirectory, "assets/objects/" + path);
      long size = file.length();
      if (file.isFile() && size != 0L) {
         if (fast) {
            return true;
         } else if (local.getSize() != size) {
            return false;
         } else {
            return local.getHash() == null ? true : local.getHash().equals(FileUtil.getChecksum(file, "SHA-1"));
         }
      } else {
         return false;
      }
   }
}
