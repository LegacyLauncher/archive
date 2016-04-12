package ru.turikhay.tlauncher.ui.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.U;

public class VersionManagerUIListener implements VersionManagerListener {
   private final TLauncher tl;
   private final Configuration settings;
   private final Gson gson;
   private boolean firstUpdate = true;
   private File listFile;
   private VersionManagerUIListener.SimpleVersionList list;
   private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

   public VersionManagerUIListener(TLauncher tl) {
      this.tl = tl;
      this.settings = tl.getSettings();
      this.gson = (new GsonBuilder()).registerTypeAdapter(Date.class, new DateTypeAdapter()).registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory()).setPrettyPrinting().create();
      tl.getVersionManager().addListener(this);
   }

   public void onVersionsRefreshing(VersionManager vm) {
   }

   public void onVersionsRefreshingFailed(VersionManager vm) {
   }

   public void onVersionsRefreshed(VersionManager vm) {
      this.listFile = new File(MinecraftUtil.getWorkingDirectory(), "versions/versions.json");
      boolean isFirstUpdate = this.firstUpdate;
      this.firstUpdate = false;
      boolean enabled = false;
      if (this.settings.getBoolean("minecraft.versions.sub.remote")) {
         ReleaseType[] var4 = ReleaseType.values();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            ReleaseType type = var4[var6];
            enabled |= this.settings.getBoolean("gui.alerton." + type);
         }
      }

      if (!enabled) {
         if (this.listFile.isFile()) {
            try {
               FileUtil.deleteFile(this.listFile);
            } catch (Exception var18) {
               this.log("Could not delete version index file:", this.listFile, var18);
            }
         }

         this.log("Version list comparing disabled.");
      } else {
         this.log("Version list comparing enabled.");
         VersionManagerUIListener.SimpleVersionList oldList = this.list == null ? this.fetchListFromFile() : this.list;
         if (oldList == null) {
            this.log("Old list is empty, saving current one for the next time.");
            this.saveList(this.list = this.fetchListFromManager(vm));
         } else {
            VersionManagerUIListener.SimpleVersionList newList = this.fetchListFromManager(vm);
            TreeMap newVersions = new TreeMap();
            VersionManagerUIListener.SimpleVersion lastVersion = null;
            int i = 0;
            Iterator var9 = newList.versions.iterator();

            while(var9.hasNext()) {
               VersionManagerUIListener.SimpleVersion version = (VersionManagerUIListener.SimpleVersion)var9.next();
               if (!oldList.versions.contains(version)) {
                  ++i;
                  lastVersion = version;
                  if (!this.settings.getBoolean("gui.alerton." + version.type)) {
                     this.log(version, "is not interesting, ignored");
                  } else {
                     switch(version.type) {
                     default:
                        version.type = ReleaseType.UNKNOWN;
                     case RELEASE:
                     case SNAPSHOT:
                        Object subVersionList;
                        if (newVersions.containsKey(version.type)) {
                           subVersionList = (List)newVersions.get(version.type);
                        } else {
                           subVersionList = new ArrayList();
                           newVersions.put(version.type, subVersionList);
                        }

                        ((List)subVersionList).add(version);
                     }
                  }
               }
            }

            if (newVersions.isEmpty()) {
               this.log("Nothing interesting.");
            } else {
               StringBuilder text = (new StringBuilder(Localizable.get("version.manager.alert.header.found" + (isFirstUpdate ? ".welcome" : "")))).append(" ");
               if (i == 1) {
                  text.append(Localizable.get("version.manager.alert.header.single." + lastVersion.type)).append("\n");
                  this.add(text, lastVersion);
               } else {
                  text.append(Localizable.get("version.manager.alert.header.multiple")).append("\n");
                  List unknownNew = (List)newVersions.get(ReleaseType.UNKNOWN);
                  Iterator var25;
                  if (newVersions.size() == 1 && unknownNew != null) {
                     var25 = ((List)newVersions.get(ReleaseType.UNKNOWN)).iterator();

                     while(var25.hasNext()) {
                        VersionManagerUIListener.SimpleVersion version = (VersionManagerUIListener.SimpleVersion)var25.next();
                        this.add(text, version);
                     }
                  } else {
                     var25 = newVersions.entrySet().iterator();

                     label94:
                     while(true) {
                        while(true) {
                           ReleaseType type;
                           List versionList;
                           do {
                              if (!var25.hasNext()) {
                                 break label94;
                              }

                              Entry entry = (Entry)var25.next();
                              type = (ReleaseType)entry.getKey();
                              versionList = (List)entry.getValue();
                           } while(versionList.isEmpty());

                           text.append('\n').append(Localizable.get("version.manager.alert." + type + "." + (versionList.size() == 1 ? "single" : "multiple"))).append('\n');
                           int k = 0;
                           Iterator var16 = versionList.iterator();

                           while(var16.hasNext()) {
                              VersionManagerUIListener.SimpleVersion version = (VersionManagerUIListener.SimpleVersion)var16.next();
                              this.log("New version:", version);
                              ++k;
                              if (k == 5) {
                                 this.log("...");
                                 text.append(Localizable.get("version.manager.alert.more", versionList.size() - k + 1)).append('\n');
                                 break;
                              }

                              this.add(text, version);
                           }
                        }
                     }
                  }
               }

               Alert.showMessage(Localizable.get("version.manager.alert.title"), text.toString());
               this.list = newList;
               this.saveList(newList);
            }
         }
      }
   }

   private boolean saveList(VersionManagerUIListener.SimpleVersionList versionList) {
      FileWriter writer = null;

      boolean var3;
      try {
         FileUtil.createFile(this.listFile);
         writer = new FileWriter(this.listFile);
         this.gson.toJson((Object)versionList, (Appendable)writer);
         var3 = true;
      } catch (Exception var7) {
         this.log("Could not write version list file", this.listFile, var7);
         throw new RuntimeException(var7);
      } finally {
         U.close(writer);
      }

      return var3;
   }

   private VersionManagerUIListener.SimpleVersionList fetchListFromFile() {
      FileReader reader = null;

      Object var3;
      try {
         reader = new FileReader(this.listFile);
         VersionManagerUIListener.SimpleVersionList var2 = (VersionManagerUIListener.SimpleVersionList)this.gson.fromJson((Reader)reader, (Class)VersionManagerUIListener.SimpleVersionList.class);
         return var2;
      } catch (Exception var7) {
         this.log("Could not read version list from file", this.listFile, var7);
         var3 = null;
      } finally {
         U.close(reader);
      }

      return (VersionManagerUIListener.SimpleVersionList)var3;
   }

   private VersionManagerUIListener.SimpleVersionList fetchListFromManager(VersionManager vm) {
      try {
         VersionManagerUIListener.SimpleVersionList versionList = new VersionManagerUIListener.SimpleVersionList();
         Iterator var3 = vm.getVersions(new VersionFilter(), false).iterator();

         while(var3.hasNext()) {
            VersionSyncInfo syncInfo = (VersionSyncInfo)var3.next();
            versionList.versions.add(new VersionManagerUIListener.SimpleVersion(syncInfo));
         }

         return versionList;
      } catch (Exception var5) {
         this.log("Could not fetch list from manager", var5);
         throw new RuntimeException(var5);
      }
   }

   private StringBuilder add(StringBuilder b, VersionManagerUIListener.SimpleVersion version) {
      return b.append("– ").append(version.id).append(" (").append(this.getTimeDifference(version.time)).append(")").append('\n');
   }

   private static Calendar cal() {
      return Calendar.getInstance(UTC);
   }

   private static Calendar cal(Date date) {
      Calendar calendar = cal();
      calendar.setTime(date);
      return calendar;
   }

   private static Calendar cal(long time) {
      Calendar calendar = cal();
      calendar.setTimeInMillis(time);
      return calendar;
   }

   private String getTimeDifference(Date time) {
      if (time == null) {
         return Localizable.get("version.manager.alert.time.unknown");
      } else {
         Calendar currentTime = cal();
         Calendar releaseTime = cal(time);
         Calendar difference = cal(currentTime.getTimeInMillis() - releaseTime.getTimeInMillis());
         String path = "version.manager.alert.time.";
         int field = -1;
         if (difference.get(1) > 1970) {
            path = path + "longtimeago";
         } else if (difference.get(3) > 2) {
            path = path + "week";
            field = difference.get(3) - 1;
         } else if (difference.get(6) > 1) {
            path = path + "day";
            field = difference.get(6);
         } else if (difference.get(11) > 1) {
            path = path + "hour";
            field = difference.get(11);
         } else if (difference.get(12) > 1) {
            path = path + "minute";
            field = difference.get(12);
         } else {
            path = path + "justnow";
         }

         return field == -1 ? Localizable.get(path) : Localizable.get(path, field);
      }
   }

   private void log(Object... o) {
      U.log("[VersionManagerUI]", o);
   }

   private static class SimpleVersion {
      private String id;
      private ReleaseType type;
      private Date releaseTime;
      private Date time;

      SimpleVersion(VersionSyncInfo syncInfo) {
         this.id = syncInfo.getID();
         Version version = syncInfo.getAvailableVersion();
         this.type = version.getReleaseType();
         this.releaseTime = version.getReleaseTime();
         this.time = version.getUpdatedTime();
      }

      public boolean equals(Object o) {
         if (o != null && o instanceof VersionManagerUIListener.SimpleVersion) {
            VersionManagerUIListener.SimpleVersion v = (VersionManagerUIListener.SimpleVersion)o;
            return this.id.equals(v.id);
         } else {
            return false;
         }
      }

      public String toString() {
         return "SimpleVersion{id=" + this.id + ",type=" + this.type + ",releaseTime=" + this.releaseTime + ",time=" + this.time + "}";
      }
   }

   private static class SimpleVersionList {
      String _notice;
      List versions;

      private SimpleVersionList() {
         this._notice = "Pretend that you are not reading this. And this file does not exist. It does not affect anything important. Just for indexing. Hvae fnu!";
         this.versions = new ArrayList();
      }

      public String toString() {
         return "SimpleVersionList[" + this.versions + "]";
      }

      // $FF: synthetic method
      SimpleVersionList(Object x0) {
         this();
      }
   }
}
