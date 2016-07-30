package ru.turikhay.util;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public abstract class Compressor {
   private static Map compressorByMarker = new TreeMap();
   public static Compressor.GzipCompressor GZIP = new Compressor.GzipCompressor();
   public static Compressor.Bzip2Compressor BZIP2 = new Compressor.Bzip2Compressor();
   final String name;
   final byte[] marker;

   public static InputStream uncompressMarked(InputStream in, boolean failIfNoMarker) throws IOException {
      byte[] markerBytes = new byte[5];
      if (in.read(markerBytes) < 5) {
         if (failIfNoMarker) {
            throw new Compressor.MarkerNotFoundException();
         }
      } else {
         String marker = new String(markerBytes);
         Compressor compressor = (Compressor)compressorByMarker.get(marker);
         if (compressor != null) {
            return compressor.uncompress(in);
         }

         if (failIfNoMarker) {
            throw new Compressor.UnknownMarkerException(marker);
         }
      }

      return new SequenceInputStream(new ByteInputStream(markerBytes, markerBytes.length), in);
   }

   public static InputStream uncompressMarked(InputStream in) throws IOException {
      return uncompressMarked(in, false);
   }

   protected Compressor(String name, byte[] marker) {
      this.name = StringUtil.requireNotBlank(name, "name");
      if (marker == null) {
         throw new NullPointerException("marker");
      } else if (marker.length != 5) {
         throw new IllegalArgumentException("marker is too big: " + marker.length);
      } else {
         this.marker = marker;
         compressorByMarker.put(new String(marker), this);
      }
   }

   protected Compressor(String name, String shortName) {
      this(name, U.toByteArray(shortName));
   }

   public final String getName() {
      return this.name;
   }

   abstract InputStream _uncompress(InputStream var1) throws IOException;

   public final InputStream uncompress(InputStream in) throws IOException {
      return new Compressor.CompressedInputStream(this, this._uncompress(in));
   }

   public static class UnknownMarkerException extends IOException {
      public UnknownMarkerException(String marker) {
         super(marker);
      }
   }

   public static class MarkerNotFoundException extends IOException {
   }

   public static class CompressedInputStream extends FilterInputStream {
      private final Compressor compressor;

      protected CompressedInputStream(Compressor compressor, InputStream delegator) {
         super(delegator);
         this.compressor = (Compressor)U.requireNotNull(compressor, "compressor");
      }

      public final Compressor getCompressor() {
         return this.compressor;
      }
   }

   public static class Bzip2Compressor extends Compressor {
      private Bzip2Compressor() {
         super("bzip2", "bzip:");
      }

      InputStream _uncompress(InputStream in) throws IOException {
         return new BZip2CompressorInputStream(in);
      }

      // $FF: synthetic method
      Bzip2Compressor(Object x0) {
         this();
      }
   }

   public static class GzipCompressor extends Compressor {
      private GzipCompressor() {
         super("gzip", "gzip:");
      }

      InputStream _uncompress(InputStream in) throws IOException {
         return new GzipCompressorInputStream(in);
      }

      // $FF: synthetic method
      GzipCompressor(Object x0) {
         this();
      }
   }
}
