/*
  JSmooth: a VM wrapper toolkit for Windows
  Copyright (C) 2003 Rodrigo Reyes <reyes@charabia.net>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

package jsmooth;

/**
 *
 */

public class DriveInfo
{
    static public final int DRIVE_REMOVABLE=2;
    static public final int DRIVE_FIXED=3;
    static public final int DRIVE_REMOTE=4;
    static public final int DRIVE_CDROM=5;
    static public final int DRIVE_RAMDISK=6;
    static public final int DRIVE_UNKNOWN=0;
    static public final int DRIVE_NO_ROOT_DIR=1;

    private int m_driveType = DRIVE_UNKNOWN;

    private long m_totalBytes;
    private long m_freeBytesForUser;
    private long m_totalFreeBytes;

    private int m_serialNumber;
    private int m_maxComponentSize;
    private int m_systemFlags;

    private String m_volumeName;
    private String m_fileSystemName;

    public static final int FILE_CASE_SENSITIVE_SEARCH     = 0x00000001;
    public static final int FILE_CASE_PRESERVED_NAMES      = 0x00000002;
    public static final int FILE_UNICODE_ON_DISK           = 0x00000004;
    public static final int FILE_PERSISTENT_ACLS           = 0x00000008;
    public static final int FILE_FILE_COMPRESSION          = 0x00000010;
    public static final int FILE_VOLUME_QUOTAS             = 0x00000020;
    public static final int FILE_SUPPORTS_SPARSE_FILES     = 0x00000040;
    public static final int FILE_SUPPORTS_REPARSE_POINTS   = 0x00000080;
    public static final int FILE_SUPPORTS_REMOTE_STORAGE   = 0x00000100;
    public static final int FILE_VOLUME_IS_COMPRESSED      = 0x00008000;
    public static final int FILE_SUPPORTS_OBJECT_IDS       = 0x00010000;
    public static final int FILE_SUPPORTS_ENCRYPTION       = 0x00020000;
    public static final int FILE_NAMED_STREAMS             = 0x00040000;
    public static final int FILE_READ_ONLY_VOLUME          = 0x00080000;

    public DriveInfo() { }

    /**
     * Return the drive type as one of the DRIVE_* constants.
     *
     * @return one of the DRIVE_* constants
     */
    public int getDriveType()
    {
    	return m_driveType;
    }

    /**
     * Return the total free space of the volume in bytes
     *
     * @return free space in bytes
     */
    public long getFreeSpace()
    {
    	return m_totalFreeBytes;
    }

    /**
     * Return the free space of the volume available to the user. This
     * value can be different from getFreeSpace() when the filesystem
     * implements quotas.
     *
     * @return free space for the user, in bytes
     */
    public long getFreeSpaceForUser()
    {
    	return m_freeBytesForUser;
    }

    /**
     * Return the total space of the volume, including the free and allocated space.
     *
     * @return total space of the volume, in bytes
     */
    public long getTotalSpace()
    {
    	return m_totalBytes;
    }

    /**
     * Return the serial number of the volume, as allocated by the
     * system during the formatting of the volume.
     *
     * @return the serial number of the volume
     */
    public int getSerialNumber()
    {
    	return m_serialNumber;
    }

    /**
     * The maximum size supported by the file system for a single component (directory or filename).
     *
     * @return the maximum number of chars for a component
     */
    public int getMaximumComponentSize()
    {
    	return m_maxComponentSize;
    }

    /**
     * Return a combination of system flags supported by the filesystem.
     *
     * @return a combination of FILE_* constants
     */
    public int getSystemFlags()
    {
    	return m_systemFlags;
    }

    /**
     * If a name has been defined for the volume represented by this
     * object, it is returned. Otherwise, an empty string is returned.
     *
     * @return a java.lang.String of the volume name
     */
    public String getVolumeName()
    {
    	return m_volumeName!=null?m_volumeName:"";
    }

    /**
     * The name of the filesystem installed on this drive
     *
     * @return a java.lang.String of the filesystem name
     */

    public String getFileSystemName()
    {
    	return m_fileSystemName!=null?m_fileSystemName:"";
    }

    public String toString()
    {
    	String res="[";
    	switch(m_driveType)
    	{
    	case DRIVE_REMOVABLE:
    		res += "DRIVE_REMOVABLE";
    		break;
    	case DRIVE_FIXED:
    		res += "DRIVE_FIXED";
    		break;
    	case DRIVE_REMOTE:
    		res += "DRIVE_REMOTE";
    		break;
    	case DRIVE_CDROM:
    		res += "DRIVE_CDROM";
    		break;
    	case DRIVE_RAMDISK:
    		res += "DRIVE_RAMDISK";
    		break;
    	case DRIVE_UNKNOWN:
    		res += "DRIVE_UNKNOWN";
    		break;
    	default:
    		res += "DRIVE_NO_ROOT_DIR";
    		break;
    	}
  		res += ":" + m_freeBytesForUser + ":" + m_totalFreeBytes + "/" + m_totalBytes;
  		res += "," + m_serialNumber +","+m_maxComponentSize + "," + m_systemFlags + "," + m_volumeName + "," + m_fileSystemName;
  		res += "]";
  		return res;
    }
    
}