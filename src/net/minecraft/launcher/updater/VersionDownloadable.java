package net.minecraft.launcher.updater;

import java.io.File;
import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;

public class VersionDownloadable extends Downloadable {
   private final CompleteVersion version;

   public VersionDownloadable(CompleteVersion version, File destination, Repository repository) {
      this.version = version;
      this.setDestination(destination);
      this.addAdditionalDestination(new File(destination.getAbsolutePath() + ".bak"));
      if (version.getDownloadURL(DownloadType.CLIENT) != null) {
         DownloadInfo downloadInfo = version.getDownloadURL(DownloadType.CLIENT);
         this.setURL(downloadInfo.getUrl().toString());
      } else {
         String id;
         Repository repo;
         if (version.getJar() == null) {
            repo = repository;
            id = version.getID();
         } else {
            repo = Repository.OFFICIAL_VERSION_REPO;
            id = version.getJar();
         }

         String path = "versions/" + id + "/" + id + ".jar";
         this.setURL(repo, path);
      }

   }

   public void onComplete() throws RetryDownloadException {
      DownloadInfo downloadInfo = this.version.getDownloadURL(DownloadType.CLIENT);
      if (downloadInfo != null) {
         File destination = this.getDestination();
         if (destination.length() != (long)downloadInfo.getSize()) {
            throw new RetryDownloadException("file size mismatch");
         } else {
            String hash = FileUtil.getSHA(destination);
            if (!downloadInfo.getSha1().equals(hash)) {
               throw new RetryDownloadException("hash mismatch, got: " + hash + ", expected: " + downloadInfo.getSha1());
            }
         }
      }
   }
}
