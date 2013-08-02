package net.minecraft.launcher_.updater;

import com.google.gson.JsonSyntaxException;
import com.turikhay.tlauncher.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Set;

import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.versions.CompleteVersion;
import net.minecraft.launcher_.versions.ReleaseType;
import net.minecraft.launcher_.versions.Version;

public class LocalVersionList extends FileBasedVersionList
{
  private final File baseDirectory;
  private final File baseVersionsDir;

  public LocalVersionList(File baseDirectory)
  {
    if ((baseDirectory == null) || (!baseDirectory.isDirectory())) throw new IllegalArgumentException("Base directory is not a folder!");

    this.baseDirectory = baseDirectory;
    this.baseVersionsDir = new File(this.baseDirectory, "versions");
    if (!this.baseVersionsDir.isDirectory()) this.baseVersionsDir.mkdirs(); 
  }

  protected InputStream getFileInputStream(String uri)
    throws FileNotFoundException
  {
    return new FileInputStream(new File(this.baseDirectory, uri));
  }

  public void refreshVersions() throws IOException
  {
    clearCache();

    File[] files = this.baseVersionsDir.listFiles();
    if (files == null) return;

    for (File directory : files) {
      String id = directory.getName();
      File jsonFile = new File(directory, id + ".json");

      if ((directory.isDirectory()) && (jsonFile.exists())) {
        try {
          CompleteVersion version = (CompleteVersion)this.gson.fromJson(getUrl("versions/" + id + "/" + id + ".json"), CompleteVersion.class);
          addVersion(version);
        } catch (JsonSyntaxException ex) {
            throw new JsonSyntaxException("Loading file: " + jsonFile.toString(), ex);
        }
      }
    }

    for (Version version : getVersions()) {
      ReleaseType type = version.getType();

      if ((getLatestVersion(type) == null) || (getLatestVersion(type).getUpdatedTime().before(version.getUpdatedTime())))
        setLatestVersion(version);
    }
  }

  public void saveVersionList() throws IOException
  {
    String text = serializeVersionList();
    PrintWriter writer = new PrintWriter(new File(this.baseVersionsDir, "versions.json"));
    writer.print(text);
    writer.close();
  }

  public void saveVersion_(CompleteVersion version) throws IOException {
    String text = serializeVersion(version);
    File target = new File(this.baseVersionsDir, version.getId() + "/" + version.getId() + ".json");
    
    FileUtil.saveFile(target, text);
  }
  
  public void saveVersion(CompleteVersion version) {
	  try{ saveVersion_(version); }catch(IOException e){ e.printStackTrace(); }
  }

  public File getBaseDirectory() {
    return this.baseDirectory;
  }

  public boolean hasAllFiles(CompleteVersion version, OperatingSystem os)
  {
    Set<String> files = version.getRequiredFiles(os);

    for (String file : files) {
      if (!new File(this.baseDirectory, file).isFile()) {
        return false;
      }
    }

    return true;
  }
}
