package net.minecraft.launcher.updater;

import java.io.File;
import java.io.IOException;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.managers.AssetsManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class AssetDownloadable extends Downloadable {
   private final AssetIndex.AssetObject asset;
   private final File folder;
   private final String prefix;

   public AssetDownloadable(AssetIndex.AssetObject obj, File objectsFolder) {
      this.asset = obj;
      this.folder = objectsFolder;
      String path = AssetIndex.getPath(this.asset.hash());
      this.setURL(Repository.ASSETS_REPO, path);
      this.setDestination(new File(this.folder, path));
      if (this.asset.isCompressed()) {
         this.prefix = "[Asset_c:" + this.asset.getCompressedHash() + "]";
      } else {
         this.prefix = "[Asset:" + this.asset.getHash() + "]";
      }

      this.log("URL:", this.getURL(), "; destination:", this.getDestination());
   }

   public void onComplete() throws RetryDownloadException {
      String expectHash = this.asset.hash();
      String gotHash = FileUtil.getDigest(this.getDestination(), "SHA", 40);
      if (expectHash.equals(gotHash)) {
         this.log("Hash is correct:", gotHash);
         if (this.asset.isCompressed()) {
            try {
               AssetsManager.decompress(this.getDestination(), new File(this.folder, this.asset.getHash()), this.asset.getHash());
            } catch (RetryDownloadException var4) {
               throw var4;
            } catch (IOException var5) {
               throw new RetryDownloadException("could not decompress " + gotHash, var5);
            }
         }

      } else {
         this.log("Invalid hash:", gotHash, "; expected:", expectHash);
         throw new RetryDownloadException(gotHash + ';' + expectHash);
      }
   }

   private void log(Object... o) {
      U.log(this.prefix, o);
   }
}
