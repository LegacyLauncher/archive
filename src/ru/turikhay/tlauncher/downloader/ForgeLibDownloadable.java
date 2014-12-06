package ru.turikhay.tlauncher.downloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;

import org.tukaani.xz.XZInputStream;

import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

public class ForgeLibDownloadable extends Downloadable {

	private final File unpacked;

	public ForgeLibDownloadable(String url, File packedLib, File unpackedLib) {
		super(url, packedLib);

		this.unpacked = unpackedLib;
	}

	@Override
	void onComplete() throws RetryDownloadException {
		super.onComplete();

		try {
			unpackLibrary(getDestination(), unpacked);
		} catch(Throwable t) {
			throw new RetryDownloadException("cannot unpack forge library", t);
		}

	}

	// synchronized simply prevents outofmemory error if there are many threads trying to unpack their libraries. c:
	private synchronized static void unpackLibrary(File library, File output) throws IOException {
		log("Synchronized unpacking:", library);

		output.delete();

		InputStream in = null;
		JarOutputStream jos = null;
		try {
			in = new FileInputStream(library);
			in = new XZInputStream(in);

			log("Decompressing...");
			byte[] decompressed = readFully(in);
			log("Decompressed successfully");

			String end = new String(decompressed, decompressed.length - 4, 4);

			if (!end.equals("SIGN"))
				throw new RetryDownloadException("signature missing");

			log("Signature matches!");

			int x = decompressed.length;
			int len = decompressed[(x - 8)] & 0xFF | (decompressed[(x - 7)] & 0xFF) << 8 | (decompressed[(x - 6)] & 0xFF) << 16 | (decompressed[(x - 5)] & 0xFF) << 24;

			log("Now getting checksums...");
			byte[] checksums = Arrays.copyOfRange(decompressed, decompressed.length - len - 8, decompressed.length - 8);

			FileUtil.createFile(output);
			FileOutputStream jarBytes = new FileOutputStream(output);
			jos = new JarOutputStream(jarBytes);

			log("Now unpacking...");
			Pack200.newUnpacker().unpack(new ByteArrayInputStream(decompressed), jos);
			log("Unpacked successfully");

			log("Now trying to write checksums...");
			jos.putNextEntry(new JarEntry("checksums.sha1"));
			jos.write(checksums);
			jos.closeEntry();

			log("Now finishing...");
		} catch(IOException e) {
			output.delete();
			throw e;
		} finally {
			close(in, jos);
			FileUtil.deleteFile(library);
		}
		log("Done:", output);
	}

	private static void close(Closeable...closeables) {
		for(Closeable c : closeables)
			try {
				c.close();
			} catch(Exception e) {
				// Ignore
			}
	}

	private static byte[] readFully(InputStream stream) throws IOException {
		byte[] data = new byte[4096];
		ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
		int len;

		do {
			len = stream.read(data);
			if (len > 0) {
				entryBuffer.write(data, 0, len);
			}
		} while (len != -1);

		return entryBuffer.toByteArray();
	}

	private static void log(Object...o) { U.log("[ForgeLibDownloadable]", o); }
}
