package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import net.minecraft.launcher.updater.AssetDownloadable;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

@ComponentDependence({VersionManager.class, VersionLists.class})
public class AssetsManager extends LauncherComponent {
   private final Gson gson = TLauncher.getGson();
   private final Object assetsFlushLock = new Object();

   public AssetsManager(ComponentManager manager) throws Exception {
      super(manager);
   }

   public DownloadableContainer downloadResources(CompleteVersion version, List list) {
      File baseDirectory = this.manager.getLauncher().getVersionManager().getLocalList().getBaseDirectory();
      DownloadableContainer container = new DownloadableContainer();
      container.addAll(this.getResourceFiles(version, baseDirectory, list));
      return container;
   }

   private Set getResourceFiles(CompleteVersion version, File baseDirectory, List list) {
      File objectsFolder = new File(baseDirectory, "assets/objects");
      HashSet result = new HashSet();
      Iterator var6 = list.iterator();

      while(true) {
         while(var6.hasNext()) {
            AssetIndex.AssetObject object = (AssetIndex.AssetObject)var6.next();
            File destination = new File(objectsFolder, object.getHash());
            if (destination.isFile()) {
               String hash = FileUtil.getDigest(destination, "SHA", 40);
               if (object.getHash().equals(hash)) {
                  this.log(new Object[]{"Skipped existing asset:", hash});
                  continue;
               }
            }

            result.add(new AssetDownloadable(object, objectsFolder));
         }

         return result;
      }
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
      ArrayList result = new ArrayList();
      String indexName = version.getAssetIndex().getId();
      File indexesFolder = new File(baseDirectory, "assets/indexes/");
      File indexFile = new File(indexesFolder, indexName + ".json");
      this.log(new Object[]{"Reading indexes from file", indexFile});
      FileReader reader = null;
      AssetIndex index = null;

      AssetIndex.AssetObject object;
      label55: {
         try {
            index = (AssetIndex)this.gson.fromJson((Reader)(reader = new FileReader(indexFile)), (Class)AssetIndex.class);
            break label55;
         } catch (Exception var14) {
            this.log(new Object[]{"could not read index file", var14});
            object = null;
         } finally {
            U.close(reader);
         }

         return object;
      }

      Iterator var10 = index.getUniqueObjects().iterator();

      while(var10.hasNext()) {
         object = (AssetIndex.AssetObject)var10.next();
         result.add(object);
      }

      return result;
   }

   private List getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {
      ArrayList result = new ArrayList();
      String indexName = version.getAssetIndex().getId();
      if (indexName == null) {
         indexName = "legacy";
      }

      File assets = new File(baseDirectory, "assets");
      File indexesFolder = new File(assets, "indexes");
      File indexFile = new File(indexesFolder, indexName + ".json");
      Object json;
      if (version.getAssetIndex().getUrl() == null) {
         this.log(new Object[]{"Reading from repository..."});
         json = Repository.OFFICIAL_VERSION_REPO.read("indexes/" + indexName + ".json");
      } else {
         this.log(new Object[]{"Reading from index:", version.getAssetIndex().getUrl()});
         json = IOUtils.toBufferedReader(new InputStreamReader(version.getAssetIndex().getUrl().openStream(), "UTF-8"));
      }

      File tempIndexFile = null;
      if (save) {
         FileOutputStream out = null;
         tempIndexFile = File.createTempFile("tlauncher-assets", (String)null);
         tempIndexFile.deleteOnExit();

         try {
            IOUtils.copy((Reader)json, (OutputStream)(out = new FileOutputStream(tempIndexFile)));
         } finally {
            U.close(out);
         }

         json = new FileReader(tempIndexFile);
      }

      AssetIndex index = (AssetIndex)this.gson.fromJson((Reader)json, (Class)AssetIndex.class);
      Iterator var12 = index.getUniqueObjects().iterator();

      while(var12.hasNext()) {
         AssetIndex.AssetObject object = (AssetIndex.AssetObject)var12.next();
         result.add(object);
      }

      if (save) {
         synchronized(this.assetsFlushLock) {
            FileInputStream in = null;
            FileOutputStream out = null;

            try {
               IOUtils.copy((InputStream)(in = new FileInputStream(tempIndexFile)), (OutputStream)(out = new FileOutputStream(indexFile)));
            } finally {
               U.close(in, out);
               tempIndexFile.delete();
            }
         }

         this.log(new Object[]{"Assets index has been saved into file:", indexFile});
      }

      return result;
   }

   public AssetsManager.ResourceChecker checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast) {
      this.log(new Object[]{"Checking resources..."});
      new ArrayList();
      List list;
      if (local) {
         list = this.getLocalResourceFilesList(version, baseDirectory);
      } else {
         list = this.getResourceFiles(version, baseDirectory, false);
      }

      if (list == null) {
         this.log(new Object[]{"Cannot get assets list. Aborting."});
         return null;
      } else {
         this.log(new Object[]{"Fast comparing:", fast});
         return new AssetsManager.ResourceChecker(baseDirectory, list, fast);
      }
   }

   public AssetsManager.ResourceChecker checkResources(CompleteVersion version, boolean fast) {
      return this.checkResources(version, ((VersionLists)this.manager.getComponent(VersionLists.class)).getLocal().getBaseDirectory(), false, fast);
   }

   private static boolean checkResource(File baseDirectory, AssetIndex.AssetObject local, boolean fast) {
      File assetFile = new File(baseDirectory, "assets/objects/" + AssetIndex.getPath(local.getHash()));
      if (fast) {
         return checkFile(assetFile, local.size());
      } else if (checkFile(assetFile, local.size(), local.getHash())) {
         return true;
      } else if (!local.isCompressed()) {
         return false;
      } else {
         FileUtil.deleteFile(assetFile);
         File compressedAssetFile = new File(baseDirectory, "assets/objects/" + AssetIndex.getPath(local.getCompressedHash()));
         if (checkFile(assetFile, local.getCompressedSize(), local.getCompressedHash())) {
            try {
               decompress(compressedAssetFile, assetFile, local.getHash());
               return true;
            } catch (IOException var6) {
               U.log("[AssetsManager]", var6);
               return false;
            }
         } else {
            return false;
         }
      }
   }

   private static boolean checkFile(File assetFile, long size) {
      return assetFile.isFile() && assetFile.length() == size;
   }

   private static boolean checkFile(File assetFile, long size, String hash) {
      return checkFile(assetFile, size) && hash.equals(AssetIndex.getHash(assetFile));
   }

   public static void decompress(File compressedInput, File uncompressedOutput, String expectHash) throws IOException {
      InputStream in = null;
      FileOutputStream out = null;

      String hash;
      try {
         in = new GZIPInputStream(new FileInputStream(compressedInput));
         out = new FileOutputStream(uncompressedOutput);
         hash = FileUtil.copyAndDigest(in, out, "SHA", 40);
      } finally {
         U.close(in);
         U.close(out);
      }

      if (!expectHash.equals(hash)) {
         throw new IOException("could not decompress asset got: " + hash + ", expected: " + expectHash);
      }
   }

   public final class ResourceChecker extends ExtendedThread {
      final File baseDirectory;
      final boolean fast;
      final List objectList;
      private volatile List result;
      private volatile AssetIndex.AssetObject current;
      private long delta;
      private Exception e;

      ResourceChecker(File baseDirectory, List objectList, boolean fast) {
         this.baseDirectory = baseDirectory;
         this.objectList = objectList;
         this.fast = fast;
         this.startAndWait();
         this.unlockThread("start");
      }

      public boolean checkWorking() throws InterruptedException {
         if (this.isInterrupted()) {
            throw new InterruptedException();
         } else {
            return this.result == null && this.isAlive();
         }
      }

      public AssetIndex.AssetObject getCurrent() {
         return this.current;
      }

      public List getAssetList() {
         return this.result;
      }

      public long getDelta() {
         return this.delta;
      }

      public Exception getError() {
         return this.e;
      }

      public void run() {
         this.checkCurrent();
         this.lockThread("start");

         try {
            this.check();
         } catch (Exception var2) {
            this.e = var2;
         }

      }

      private void check() {
         List result = new ArrayList();
         Time.start();
         Iterator var2 = this.objectList.iterator();

         while(var2.hasNext()) {
            AssetIndex.AssetObject object = (AssetIndex.AssetObject)var2.next();
            this.current = object;
            if (!AssetsManager.checkResource(this.baseDirectory, object, this.fast)) {
               if (Thread.interrupted()) {
                  throw new RuntimeException("interrupted");
               }

               result.add(object);
            }
         }

         this.current = null;
         this.delta = Time.stop();
         this.result = result;
      }
   }
}
