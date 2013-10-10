package net.minecraft.launcher_.versions;

public enum VersionSource {
   LOCAL,
   REMOTE,
   EXTRA;

   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource;

   public String getDownloadPath() {
      switch($SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource()[this.ordinal()]) {
      case 1:
         return null;
      case 2:
         return "https://s3.amazonaws.com/Minecraft.Download/";
      case 3:
         return "https://dl.dropboxusercontent.com/u/6204017/minecraft/tlauncher/extra/";
      default:
         throw new IllegalStateException("Unknown repo type!");
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource() {
      int[] var10000 = $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[values().length];

         try {
            var0[EXTRA.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[LOCAL.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[REMOTE.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$net$minecraft$launcher_$versions$VersionSource = var0;
         return var0;
      }
   }
}
