package ru.turikhay.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtil {
	public final static String DEFAULT_CHARSET = "UTF-8";

	public static Charset getCharset() {
		try {
			return Charset.forName(DEFAULT_CHARSET);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void writeFile(File file, String text) throws IOException {
		createFile(file);

		BufferedOutputStream os = new BufferedOutputStream(
				new FileOutputStream(file));
		OutputStreamWriter ow = new OutputStreamWriter(os, DEFAULT_CHARSET);

		ow.write(text);
		ow.close();
		os.close();
	}

	private static String readFile(File file, String charset)
			throws IOException {
		if (file == null)
			throw new NullPointerException("File is NULL!");

		if (!file.exists())
			return null;

		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
				file));
		InputStreamReader reader = new InputStreamReader(bis, charset);

		StringBuilder b = new StringBuilder();

		while (reader.ready())
			b.append((char) reader.read());

		reader.close();
		bis.close();

		return b.toString();
	}

	public static String readFile(File file) throws IOException {
		return readFile(file, DEFAULT_CHARSET);
	}

	public static String getFilename(String path) {
		String[] folders = path.split("/");
		int size = folders.length;
		if (size == 0)
			return "";
		return folders[size - 1];
	}

	public static String getFilename(URL url) {
		return getFilename(url.getPath());
	}

	public static String getDigest(File file, String algorithm, int hashLength) {
		DigestInputStream stream = null;
		int read;

		try {
			stream = new DigestInputStream(new FileInputStream(file),
					MessageDigest.getInstance(algorithm));
			byte[] buffer = new byte[65536];

			do {
				read = stream.read(buffer);
			} while (read > 0);
		} catch (Exception ignored) {
			return null;
		} finally {
			close(stream);
		}

		return String.format("%1$0" + hashLength + "x",
				new Object[] { new BigInteger(1, stream.getMessageDigest()
						.digest()) });
	}

	public static String copyAndDigest(InputStream inputStream,
			OutputStream outputStream, String algorithm, int hashLength)
					throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Missing Digest. " + algorithm, e);
		}

		byte[] buffer = new byte[65536];
		try {
			int read = inputStream.read(buffer);
			while (read >= 1) {
				digest.update(buffer, 0, read);
				outputStream.write(buffer, 0, read);
				read = inputStream.read(buffer);
			}
		} finally {
			close(inputStream);
			close(outputStream);
		}
		return String.format("%1$0" + hashLength + "x",
				new Object[] { new BigInteger(1, digest.digest()) });
	}

	private static byte[] createChecksum(File file, String algorithm) {
		InputStream fis = null;
		try {
			fis = new BufferedInputStream(new FileInputStream(file));

			byte[] buffer = new byte[1024];
			MessageDigest complete = MessageDigest.getInstance(algorithm);
			int numRead;

			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			return complete.digest();
		} catch (Exception e) {
			return null;
		} finally {
			close(fis);
		}
	}

	public static String getChecksum(File file, String algorithm) {
		if (file == null)
			return null;
		if (!file.exists())
			return null;

		byte[] b = createChecksum(file, algorithm);
		if (b == null)
			return null;

		StringBuilder result = new StringBuilder();
		for (byte cb : b)
			result.append(Integer.toString((cb & 0xff) + 0x100, 16)
					.substring(1));
		return result.toString();
	}

	private static void close(Closeable a) {
		try {
			a.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getRunningJar() {
		try {
			return new File(URLDecoder.decode(FileUtil.class
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Cannot get running file!", e);
		}
	}

	public static void copyFile(File source, File dest, boolean replace)
			throws IOException {
		if (dest.isFile()) {

			if (!replace)
				return;

		} else {
			FileUtil.createFile(dest);
		}

		InputStream is = null;
		OutputStream os = null;

		try {
			is = new BufferedInputStream(new FileInputStream(source));
			os = new BufferedOutputStream(new FileOutputStream(dest));
			byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		}
	}

	public static void deleteFile(File file) {
		boolean onExit = !file.delete();

		if (onExit) {
			file.deleteOnExit();
			return;
		}

		File parent = file.getParentFile();
		if(parent == null) return;

		File[] list = parent.listFiles();
		if(list == null || list.length > 0) return;

		deleteFile(parent); // Cleaning up empty directory
	}

	public static void deleteFile(String path) {
		deleteFile(new File(path));
	}

	public static void deleteDirectory(File dir) {
		if(!dir.isDirectory())
			throw new IllegalArgumentException("Specified path is not a directory: "+dir.getAbsolutePath());

		for(File file : dir.listFiles())
			if(file.isDirectory())
				FileUtil.deleteDirectory(file);
			else
				FileUtil.deleteFile(file);

		FileUtil.deleteFile(dir);
	}

	public static File makeTemp(File file) throws IOException {
		createFile(file);
		file.deleteOnExit();

		return file;
	}

	public byte[] getFile(File archive, String requestedFile)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipInputStream in = null;
		try {
			in = new ZipInputStream(new FileInputStream(archive));
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				if (entry.getName().equals(requestedFile)) {

					byte[] buf = new byte[1024];
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
			}
		} finally {
			if (in != null)
				in.close();
			out.close();
		}
		return out.toByteArray();
	}

	public static boolean createFolder(File dir) throws IOException {
		if (dir == null)
			throw new NullPointerException();

		if (dir.isDirectory())
			return false;

		if (!dir.mkdirs())
			throw new IOException("Cannot create folders: "
					+ dir.getAbsolutePath());

		if (!dir.canWrite())
			throw new IOException("Ceated directory is not accessible: "
					+ dir.getAbsolutePath());

		return true;
	}

	public static boolean createFolder(String dir) throws IOException {
		if (dir == null)
			return false;
		return createFolder(new File(dir));
	}

	public static boolean folderExists(String path) {
		if (path == null)
			return false;

		File folder = new File(path);
		return folder.isDirectory();
	}

	public static boolean fileExists(String path) {
		if (path == null)
			return false;

		File file = new File(path);
		return file.isFile();
	}

	public static void createFile(File file) throws IOException {
		if (file.isFile())
			return;

		if (file.getParentFile() != null)
			file.getParentFile().mkdirs();

		if(!file.createNewFile())
			throw new IOException("Cannot create file, or it was created during runtime: "+ file.getAbsolutePath());
	}

	public static void createFile(String file) throws IOException {
		createFile(new File(file));
	}

	public static void unZip(File zip, File folder, boolean replace)
			throws IOException {
		createFolder(folder);

		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
				new FileInputStream(zip)));
		ZipEntry ze;

		byte[] buffer = new byte[1024];

		while ((ze = zis.getNextEntry()) != null) {
			String fileName = ze.getName();
			File newFile = new File(folder, fileName);

			if (!replace && newFile.isFile()) {
				U.log("[UnZip] File exists:", newFile.getAbsoluteFile());
				continue;
			}

			U.log("[UnZip]", newFile.getAbsoluteFile());
			FileUtil.createFile(newFile);

			OutputStream fos = new BufferedOutputStream(new FileOutputStream(
					newFile));
			int len;

			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

			fos.close();
		}

		zis.closeEntry();
		zis.close();
	}

	public static void removeFromZip(File zipFile, List<String> files)
			throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();
		tempFile.deleteOnExit();

		boolean renameOk = zipFile.renameTo(tempFile);
		if (!renameOk)
			throw new IOException("Could not rename the file "
					+ zipFile.getAbsolutePath() + " to "
					+ tempFile.getAbsolutePath());

		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(
				new FileInputStream(tempFile)));
		ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(zipFile)));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();

			if (!files.contains(name)) {
				zout.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0) {
					zout.write(buf, 0, len);
				}
			}

			entry = zin.getNextEntry();
		}

		zin.close();
		zout.close();
		tempFile.delete();
	}

	public static String getResource(URL resource, String charset)
			throws IOException {
		InputStream is = new BufferedInputStream(resource.openStream());

		InputStreamReader reader = new InputStreamReader(is, charset);

		StringBuilder b = new StringBuilder();
		while (reader.ready()) {
			b.append((char) reader.read());
		}
		reader.close();

		return b.toString();
	}

	public static String getResource(URL resource) throws IOException {
		return getResource(resource, DEFAULT_CHARSET);
	}

	public static String getFolder(URL url, String separator) {
		String[] folders = url.toString().split(separator);
		String s = "";

		for (int i = 0; i < folders.length - 1; i++) {
			s = s + folders[i] + separator;
		}
		return s;
	}

	public static String getFolder(URL url) {
		return getFolder(url, "/");
	}

	private static File getNeighborFile(File file, String filename) {
		File parent = file.getParentFile();
		if (parent == null)
			parent = new File("/");

		return new File(parent, filename);
	}

	public static File getNeighborFile(String filename) {
		return getNeighborFile(getRunningJar(), filename);
	}

	/**
	 * Grabbed from <a href=
	 * "http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html"
	 * >JFileChooser</a> tutorial.
	 */
	public static String getExtension(File f) {
		if (!f.isFile() && f.isDirectory())
			return null;

		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}

		return ext;
	}
}
