package net.minecraft.launcher_.versions;

import java.util.Date;


public abstract interface Version
{
  public abstract String getId();

  public abstract ReleaseType getType();

  public abstract void setType(ReleaseType paramReleaseType);

  public abstract Date getUpdatedTime();

  public abstract void setUpdatedTime(Date paramDate);

  public abstract Date getReleaseTime();

  public abstract void setReleaseTime(Date paramDate);
}
