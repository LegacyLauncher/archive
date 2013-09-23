package net.minecraft.launcher_;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import com.turikhay.tlauncher.util.U;

public enum OperatingSystem
{
  LINUX("linux", new String[] { "linux", "unix" }), 
  WINDOWS("windows", new String[] { "win" }), 
  OSX("osx", new String[] { "mac" }),
  SOLARIS("solaris", new String[] { "solaris", "sunos" }),
  UNKNOWN("unknown", new String[0]);

  private final String name;
  private final String[] aliases;
  private final static String arch = System.getProperty("sun.arch.data.model");

  private OperatingSystem(String name, String[] aliases) {
	  this.name = name;
	  this.aliases = (aliases == null ? new String[0] : aliases);
  }

  public String getName()
  {
    return this.name;
  }

  public String[] getAliases() {
    return this.aliases;
  }

  public boolean isSupported() {
    return this != UNKNOWN;
  }

  public String getJavaDir() {
    String separator = System.getProperty("file.separator");
    String path = System.getProperty("java.home") + separator + "bin" + separator;

    if ((getCurrentPlatform() == WINDOWS) && 
      (new File(path + "javaw.exe").isFile())) {
      return path + "javaw.exe";
    }

    return path + "java";
  }

  public static OperatingSystem getCurrentPlatform() {
    String osName = System.getProperty("os.name").toLowerCase();

    for (OperatingSystem os : values()) {
      for (String alias : os.getAliases()) {
        if (osName.contains(alias)) return os;
      }
    }

    return UNKNOWN;
  }
  
  public static boolean is32Bit(){
	  return arch.equals("32");
  }
  
  public static boolean is64Bit(){
	  return arch.equals("64");
  }

  public static boolean openLink(URI link) {
	  if(!Desktop.isDesktopSupported()) return false;
	  Desktop desktop = Desktop.getDesktop();
	  
	  try{ desktop.browse(link); }catch(Exception e){
		  U.log("Failed to open link: "+link);
		  return false;
	  }
	  
	  return true;
  }
  
  public static boolean openFile(File file){
	  if(!Desktop.isDesktopSupported()) return false;
	  Desktop desktop = Desktop.getDesktop();
	  
	  try{ desktop.open(file); }catch(Exception e){
		  U.log("Failed to open file: "+file);
		  return false;
	  }
	  
	  return true;
  }
  
  public static int getRecommendedMemory(){
	  return is32Bit()? 512 : 1024;
  }
}