package ru.turikhay.util;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static Charset getCharset() {
        try {
            return Charset.forName(DEFAULT_CHARSET);
        } catch (Exception var1) {
            throw new Error(DEFAULT_CHARSET + " is not supported", var1);
        }
    }

    public static void writeFile(File file, String text) throws IOException {
        createFile(file);
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
        ow.write(text);
        ow.close();
        os.close();
    }

    private static String readFile(File file, String charset) throws IOException {
        if (file == null) {
            throw new NullPointerException("File is NULL!");
        } else if (!file.exists()) {
            return null;
        } else {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            InputStreamReader reader = new InputStreamReader(bis, charset);
            StringBuilder b = new StringBuilder();

            while (reader.ready()) {
                b.append((char) reader.read());
            }

            reader.close();
            bis.close();
            return b.toString();
        }
    }

    public static String readFile(File file) throws IOException {
        return readFile(file, "UTF-8");
    }

    public static String getFilename(String path) {
        String[] folders = path.split("/");
        int size = folders.length;
        return size == 0 ? "" : folders[size - 1];
    }

    public static String getFilename(URL url) {
        return getFilename(url.getPath());
    }

    public static String getDigest(File file, String algorithm, int hashLength) {
        DigestInputStream stream = null;

        try {
            stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance(algorithm));
            byte[] ignored = new byte[65536];

            int read;
            do {
                read = stream.read(ignored);
            } while (read > 0);

            return String.format(java.util.Locale.ROOT, "%1$0" + hashLength + "x", new BigInteger(1, stream.getMessageDigest().digest()));
        } catch (Exception var9) {
        } finally {
            close(stream);
        }

        return null;
    }

    public static String getSHA(File file) {
        return getDigest(file, "SHA", 40);
    }

    public static String copyAndDigest(InputStream inputStream, OutputStream outputStream, String algorithm, int hashLength) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var10) {
            throw new RuntimeException("Missing Digest. " + algorithm, var10);
        }

        byte[] buffer = new byte[65536];

        try {
            for (int read = inputStream.read(buffer); read >= 1; read = inputStream.read(buffer)) {
                digest.update(buffer, 0, read);
                outputStream.write(buffer, 0, read);
            }
        } finally {
            close(inputStream);
            close(outputStream);
        }

        return String.format(java.util.Locale.ROOT, "%1$0" + hashLength + "x", new BigInteger(1, digest.digest()));
    }

    private static byte[] createChecksum(File file, String algorithm) {
        BufferedInputStream fis = null;

        try {
            fis = new BufferedInputStream(new FileInputStream(file));
            byte[] e = new byte[1024];
            MessageDigest complete = MessageDigest.getInstance(algorithm);

            int numRead;
            do {
                numRead = fis.read(e);
                if (numRead > 0) {
                    complete.update(e, 0, numRead);
                }
            } while (numRead != -1);

            byte[] var7 = complete.digest();
            return var7;
        } catch (Exception var10) {
        } finally {
            close(fis);
        }

        return null;
    }

    public static String getChecksum(File file, String algorithm) {
        if (file == null) {
            return null;
        } else if (!file.exists()) {
            return null;
        } else {
            byte[] b = createChecksum(file, algorithm);
            if (b == null) {
                return null;
            } else {
                StringBuilder result = new StringBuilder();
                byte[] var7 = b;
                int var6 = b.length;

                for (int var5 = 0; var5 < var6; ++var5) {
                    byte cb = var7[var5];
                    result.append(Integer.toString((cb & 255) + 256, 16).substring(1));
                }

                return result.toString();
            }
        }
    }

    private static void close(Closeable a) {
        try {
            a.close();
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public static File getRunningJar() {
        try {
            return new File(URLDecoder.decode(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8"));
        } catch (UnsupportedEncodingException var1) {
            throw new RuntimeException("Cannot get running file!", var1);
        }
    }

    public static void copyFile(File source, File dest, boolean replace) throws IOException {
        if (dest.isFile()) {
            if (!replace) {
                return;
            }
        } else {
            createFile(dest);
        }

        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(new FileInputStream(source));
            os = new BufferedOutputStream(new FileOutputStream(dest));
            byte[] buffer = new byte[1024];

            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }

            if (os != null) {
                os.close();
            }
        }

    }

    public static void deleteFile(File file) {
        if (!file.isFile() && !file.isDirectory()) {
            return;
        }

        String path = file.getAbsolutePath();

        if (file.delete()) {
            File parent = file.getParentFile();
            if (parent != null && !parent.equals(file)) {
                File[] list = parent.listFiles();
                if (list != null && list.length <= 0) {
                    deleteFile(parent);
                }
            }
        } else {
            if (fileExists(file)) {
                LOGGER.warn("Could not delete file: {}", path, new RuntimeException());
            }
        }
    }

    public static void deleteFile(String path) {
        deleteFile(new File(path));
    }

    public static void deleteDirectory(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Specified path is not a directory: " + dir.getAbsolutePath());
        }

        File[] list = dir.listFiles();

        if (list == null) {
            throw new RuntimeException("Folder is corrupted: " + dir.getAbsolutePath());
        }

        for (File file : list) {
            if (file.equals(dir)) {
                continue;
            }

            if (file.isDirectory()) {
                deleteDirectory(file);
            }

            if (file.isFile()) {
                deleteFile(file);
            }
        }

        String path = dir.getAbsolutePath();
        deleteFile(dir);
    }

    public byte[] getFile(File archive, String requestedFile) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ZipInputStream in = null;

        try {
            in = new ZipInputStream(new FileInputStream(archive));

            while (true) {
                ZipEntry entry;
                do {
                    if ((entry = in.getNextEntry()) == null) {
                        return out.toByteArray();
                    }
                } while (!entry.getName().equals(requestedFile));

                byte[] buf = new byte[1024];

                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }

            out.close();
        }
    }

    public static boolean createFolder(File dir) throws IOException {
        if (dir == null) {
            throw new NullPointerException();
        } else if (dir.isDirectory() && dir.exists()) {
            return false;
        } else if (!dir.mkdirs()) {
            throw new IOException("Cannot create folders: " + dir.getAbsolutePath());
        } else if (!dir.canWrite()) {
            throw new IOException("Created directory is not accessible: " + dir.getAbsolutePath());
        } else {
            return true;
        }
    }

    public static boolean createFolder(String dir) throws IOException {
        return dir != null && createFolder(new File(dir));
    }

    public static boolean folderExists(File folder) {
        return folder != null && folder.isDirectory() && folder.exists();
    }

    public static boolean folderExists(String path) {
        return path != null && folderExists(new File(path));
    }

    public static boolean fileExists(File file) {
        return file != null && file.isFile() && file.exists();
    }

    public static boolean fileExists(String path) {
        return path != null && fileExists(new File(path));
    }

    public static void createFile(File file) throws IOException {
        if (fileExists(file)) {
            return;
        }

        if (file.getParentFile() != null && !folderExists(file.getParentFile())) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Could not create parent:" + file.getAbsolutePath());
            }
        }

        if (!file.createNewFile() && !fileExists(file)) {
            throw new IOException("Could not create file, or it was created/deleted simultaneously: " + file.getAbsolutePath());
        }
    }

    public static void createFile(String file) throws IOException {
        createFile(new File(file));
    }

    public static void unZip(File zip, File folder, boolean replace) throws IOException {
        createFolder(folder);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
        byte[] buffer = new byte[1024];

        while (true) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                File newFile = new File(folder, fileName);
                if (!replace && newFile.isFile()) {
                    LOGGER.debug("[UnZip] File exists: {}", newFile.getAbsoluteFile());
                } else {
                    LOGGER.debug("[UnZip] {}", newFile.getAbsoluteFile());
                    createFile(newFile);
                    BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(newFile));

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }

                    fos.close();
                }
            }

            zis.closeEntry();
            zis.close();
            return;
        }
    }

    public static String getResource(URL resource, String charset) throws IOException {
        BufferedInputStream is = new BufferedInputStream(resource.openStream());
        InputStreamReader reader = new InputStreamReader(is, charset);
        StringBuilder b = new StringBuilder();

        while (reader.ready()) {
            b.append((char) reader.read());
        }

        reader.close();
        return b.toString();
    }

    public static String getResource(URL resource) throws IOException {
        return getResource(resource, "UTF-8");
    }

    public static String getFolder(URL url, String separator) {
        String[] folders = url.toString().split(separator);
        String s = "";

        for (int i = 0; i < folders.length - 1; ++i) {
            s = s + folders[i] + separator;
        }

        return s;
    }

    public static String getFolder(URL url) {
        return getFolder(url, "/");
    }

    private static File getNeighborFile(File file, String filename) {
        File parent = file.getParentFile();
        if (parent == null) {
            parent = new File("/");
        }

        return new File(parent, filename);
    }

    public static File getNeighborFile(String filename) {
        return getNeighborFile(getRunningJar(), filename);
    }

    public static String getExtension(File f) {
        if (!f.isFile() && f.isDirectory()) {
            return null;
        } else {
            String ext = "";
            String s = f.getName();
            int i = s.lastIndexOf(46);
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase(java.util.Locale.ROOT);
            }

            return ext;
        }
    }

    public static byte[] gzipUncompress(InputStream in) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        byte[] buffer = new byte[2048];
        int read;

        while ((read = gzipIn.read(buffer)) != -1)
            bOut.write(buffer, 0, read);

        return bOut.toByteArray();
    }
}
