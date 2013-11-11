package net.minecraft.launcher_.versions;

import java.util.Date;

public interface Version {
   String getId();

   ReleaseType getType();

   boolean isCheat();

   void setType(ReleaseType var1);

   Date getUpdatedTime();

   void setUpdatedTime(Date var1);

   Date getReleaseTime();

   void setReleaseTime(Date var1);
}
