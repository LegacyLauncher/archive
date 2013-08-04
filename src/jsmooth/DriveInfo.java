package jsmooth;

public class DriveInfo {
   public static final int DRIVE_REMOVABLE = 2;
   public static final int DRIVE_FIXED = 3;
   public static final int DRIVE_REMOTE = 4;
   public static final int DRIVE_CDROM = 5;
   public static final int DRIVE_RAMDISK = 6;
   public static final int DRIVE_UNKNOWN = 0;
   public static final int DRIVE_NO_ROOT_DIR = 1;
   private int m_driveType = 0;
   private long m_totalBytes;
   private long m_freeBytesForUser;
   private long m_totalFreeBytes;
   private int m_serialNumber;
   private int m_maxComponentSize;
   private int m_systemFlags;
   private String m_volumeName;
   private String m_fileSystemName;
   public static final int FILE_CASE_SENSITIVE_SEARCH = 1;
   public static final int FILE_CASE_PRESERVED_NAMES = 2;
   public static final int FILE_UNICODE_ON_DISK = 4;
   public static final int FILE_PERSISTENT_ACLS = 8;
   public static final int FILE_FILE_COMPRESSION = 16;
   public static final int FILE_VOLUME_QUOTAS = 32;
   public static final int FILE_SUPPORTS_SPARSE_FILES = 64;
   public static final int FILE_SUPPORTS_REPARSE_POINTS = 128;
   public static final int FILE_SUPPORTS_REMOTE_STORAGE = 256;
   public static final int FILE_VOLUME_IS_COMPRESSED = 32768;
   public static final int FILE_SUPPORTS_OBJECT_IDS = 65536;
   public static final int FILE_SUPPORTS_ENCRYPTION = 131072;
   public static final int FILE_NAMED_STREAMS = 262144;
   public static final int FILE_READ_ONLY_VOLUME = 524288;

   public int getDriveType() {
      return this.m_driveType;
   }

   public long getFreeSpace() {
      return this.m_totalFreeBytes;
   }

   public long getFreeSpaceForUser() {
      return this.m_freeBytesForUser;
   }

   public long getTotalSpace() {
      return this.m_totalBytes;
   }

   public int getSerialNumber() {
      return this.m_serialNumber;
   }

   public int getMaximumComponentSize() {
      return this.m_maxComponentSize;
   }

   public int getSystemFlags() {
      return this.m_systemFlags;
   }

   public String getVolumeName() {
      return this.m_volumeName != null ? this.m_volumeName : "";
   }

   public String getFileSystemName() {
      return this.m_fileSystemName != null ? this.m_fileSystemName : "";
   }

   public String toString() {
      String res = "[";
      switch(this.m_driveType) {
      case 0:
         res = res + "DRIVE_UNKNOWN";
         break;
      case 1:
      default:
         res = res + "DRIVE_NO_ROOT_DIR";
         break;
      case 2:
         res = res + "DRIVE_REMOVABLE";
         break;
      case 3:
         res = res + "DRIVE_FIXED";
         break;
      case 4:
         res = res + "DRIVE_REMOTE";
         break;
      case 5:
         res = res + "DRIVE_CDROM";
         break;
      case 6:
         res = res + "DRIVE_RAMDISK";
      }

      res = res + ":" + this.m_freeBytesForUser + ":" + this.m_totalFreeBytes + "/" + this.m_totalBytes;
      res = res + "," + this.m_serialNumber + "," + this.m_maxComponentSize + "," + this.m_systemFlags + "," + this.m_volumeName + "," + this.m_fileSystemName;
      res = res + "]";
      return res;
   }
}
