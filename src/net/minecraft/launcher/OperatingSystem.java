package net.minecraft.launcher;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public enum OperatingSystem
{
  LINUX("linux", new String[] { "linux", "unix" }), 
  WINDOWS("windows", new String[] { "win" }), 
  OSX("osx", new String[] { "mac" }),
  SOLARIS("solaris", new String[] { "solaris", "sunos" }),
  UNKNOWN("unknown", new String[0]);

  private final String name;
  private final String[] aliases;
  private final String arch = System.getProperty("sun.arch.data.model");

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

  public String getJavaDir(boolean appendFile) {
    String separator = System.getProperty("file.separator");
    String path = System.getProperty("java.home") + separator;

    if(appendFile){
    	path += "bin" + separator;
    	
    	if(getCurrentPlatform() == WINDOWS)
    		return path + "javaw.exe";
    	else
    		return path + "java";
    }
    
    return path;
  }
  
  public String getJavaDir() {
	  return getJavaDir(true);
  }
  
  public boolean doesJavaExist(){
	  return (this == OperatingSystem.WINDOWS)? FileUtil.fileExists(getJavaDir()) : FileUtil.folderExists(getJavaDir());
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
  
  public boolean is32Bit(){
	  return arch.equals("32");
  }
  
  public boolean is64Bit(){
	  return arch.equals("64");
  }
  
  public String getArch(){
	  return arch;
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
  
  public int getRecommendedMemory(){
	  return is32Bit()? 512 : 1024;
  }
  
  public static String getCurrentInfo(){
	  return
	     System.getProperty("os.name") + " " + System.getProperty("os.version") + ", " +
	     "Java " + System.getProperty("java.version");
  }
}