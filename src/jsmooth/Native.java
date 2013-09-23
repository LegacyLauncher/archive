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

import java.io.File;

/**
 * The Native class is the main hub for accessing the native functions
 * provided by JSmooth at runtime. All the methods provided are
 * static, but it is important that you check the availability of the
 * methods by calling first the <tt>isAvailable()</tt> method.
 * <p>
 * 
 * When JSmooth loads the JVM, and before calling the application's
 * main class, it registers all the native methods provided in this
 * class with their matching C functions. If the registration process
 * is successful, it enables the binding by setting to <tt>true</tt>
 * the result of <tt>isAvailable()</tt>.
 *
 */

public class Native
{
    private static boolean s_bound = false;

    /**
     * Return true if this class is correctly bound to the native
     * methods.
     *
     * @return true if you can use this class, false otherwise.
     */
    static public boolean isAvailable()
    {
    	return s_bound;
    }

    /**
     * Return the directory where is located the executable used to
     * launch the current application. The result is a string
     * containing a native file descriptor of the directory.
     * <br/> For instance: <tt>c:\my program\here\</tt>
     *
     * @return A java.lang.String containing the path of this executable
     */
    static public native String getExecutablePath();

    /**
     * Return the file name of the executable used to launch the
     * current application. It only contains the file part, not the
     * directory.
     *  <br/> For instance: <tt>myprogram.exe</tt>
     *
     * @return A java.lang.String containing the name of this executable
     */
    static public native String getExecutableName();

    /**
     * Flag a file so that Windows shall delete it during the next
     * startup sequence of the operating system. The file is not
     * deleted nor touched before the next reboot. If the file is
     * already deleted, nothing happens.
     *
     * @param filename the name of the file to delete
     */
    static public native boolean deleteFileOnReboot(String filename);


    static public final int EXITWINDOWS_FORCE = 4;
    static public final int EXITWINDOWS_LOGOFF = 0;
    static public final int EXITWINDOWS_POWEROFF = 8;
    static public final int EXITWINDOWS_REBOOT = 2;
    static public final int EXITWINDOWS_SHUTDOWN = 1;

    /**
     * Request Windows to shutdown/reboot the computer, or log off the
     * current user/session. There is no dialog asking the user to
     * confirm.
     *
     * @param shutdownFlags a combination of EXITWINDOWS_* flags
     */
    static public native boolean exitWindows(int shutdownFlags);


    static public final String SHELLEXECUTE_OPEN = "open";
    static public final String SHELLEXECUTE_PRINT = "print";
    static public final String SHELLEXECUTE_EXPLORE = "explore";
    static public final String SHELLEXECUTE_FIND = "find";
    static public final String SHELLEXECUTE_EDIT = "edit";

    static public final int SW_HIDE=0;
    static public final int SW_NORMAL=1;
    static public final int SW_SHOWNORMAL=1;
    static public final int SW_SHOWMINIMIZED=2;
    static public final int SW_MAXIMIZE=3;
    static public final int SW_SHOWMAXIMIZED=3;
    static public final int SW_SHOWNOACTIVATE=4;
    static public final int SW_SHOW=5;
    static public final int SW_MINIMIZE=6;
    static public final int SW_SHOWMINNOACTIVE=7;
    static public final int SW_SHOWNA=8;
    static public final int SW_RESTORE=9;
    static public final int SW_SHOWDEFAULT=10;
    static public final int SW_FORCEMINIMIZE=11;
    static public final int SW_MAX=11;

    /**
     * Maps the ShellExecute Windows function, which allows the
     * application to either run a new process, or to start a standard
     * operation on any document file, such as opening, editing,
     * printing, etc.
     * <p>
     * For instance, to start the default application for PDF reading:
     * <tt>shellExecute(Native.SHELLEXECUTE_OPEN, "c:/somewhere/mydocument.pdf", null, null, Native.SW_NORMAL);</tt>
     * <br/>
     * To print an html page: <tt>shellExecute(Native.SHELLEXECUTE_PRINT, "mydocument.html", null, null, Native.SW_NORMAL);</tt>
     *
     * @param action specify the operation to perform, can be any of the SHELLEXECUTE_* constant
     * @param file the file to run or for which to start the default application
     * @param parameters if file is an executable, this variable specifies the parameters to be passed. Otherwise, just set it to null.
     * @param directory the working directory for the action executed. If set to <tt>null</tt>, the default working directory of the java application is used.
     * @param showCmd a flag that specifies the state of the launched application display
     *
     * @return true if the action was successfully started, false if an error occurred.
     */
    static public native boolean shellExecute(String action, String file, String parameters, String directory, int showCmd);


    /**
     * a DriveInfo info object provides platform-specific information
     * relative to the disk volume where the File object parameter is
     * located. This method determines the disk drive from the File
     * object provided, or from the disk drive of the working
     * directory if the File object does not represent an absolute
     * path.
     *
     * <p>
     *
     * For instance <tt>Native.getDriveInfo(new File("."))</tt>
     * provides a DriveInfo object for the volume on which the current
     * working directory is located.
     * <br/>
     * 
     * <tt>Native.getDriveInfo(new File("D:/my/dir/or/file"))</tt> is
     * the same as <tt>Native.getDriveInfo(new File("D:"))</tt>.
     *
     * @return an DriveInfo object
     */
    static public native DriveInfo getDriveInfo(File f);
}