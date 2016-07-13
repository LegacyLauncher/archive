package net.minecraft.launcher.versions;

import java.util.Date;
import net.minecraft.launcher.updater.VersionList;
import ru.turikhay.tlauncher.repository.Repository;

public class PartialVersion implements Version {
   private String id;
   private String url;
   private Date time;
   private Date releaseTime;
   private ReleaseType type;
   private Repository source;
   private VersionList list;

   public String getID() {
      return this.id;
   }

   public String getUrl() {
      return this.url;
   }

   public ReleaseType getReleaseType() {
      return this.type;
   }

   public Repository getSource() {
      return this.source;
   }

   public void setSource(Repository repository) {
      if (repository == null) {
         throw new NullPointerException();
      } else {
         this.source = repository;
      }
   }

   public Date getUpdatedTime() {
      return this.time;
   }

   public Date getReleaseTime() {
      return this.releaseTime;
   }

   public VersionList getVersionList() {
      return this.list;
   }

   public void setVersionList(VersionList list) {
      if (list == null) {
         throw new NullPointerException();
      } else {
         this.list = list;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o == null) {
         return false;
      } else if (this.hashCode() == o.hashCode()) {
         return true;
      } else if (!(o instanceof Version)) {
         return false;
      } else {
         Version compare = (Version)o;
         return compare.getID() == null ? false : compare.getID().equals(this.id);
      }
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{id='" + this.id + "', time=" + this.time + ", url='" + this.url + "',release=" + this.releaseTime + ", type=" + this.type + ", source=" + this.source + ", list=" + this.list + "}";
   }
}
