package net.minecraft.launcher.updater;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.util.FileUtil;

import java.io.File;
import java.util.*;

public class AssetIndex {
    public static final String DEFAULT_ASSET_NAME = "pre-1.6";
    private final Map<String, AssetIndex.AssetObject> objects = new LinkedHashMap<>();
    private boolean virtual;

    public Map<String, AssetIndex.AssetObject> getFileMap() {
        return objects;
    }

    public Boolean map_to_resources;

    public boolean isMapToResources() {
        return map_to_resources != null && map_to_resources;
    }

    public Set<AssetIndex.AssetObject> getUniqueObjects() {
        return new HashSet<>(objects.values());
    }

    public boolean isVirtual() {
        return virtual;
    }

    public static class AssetObject {
        private String hash;
        private long size;
        private boolean reconstruct;
        private String compressedHash;
        private long compressedSize;

        public AssetObject() {
        }

        public String hash() {
            return isCompressed() ? compressedHash : hash;
        }

        public long size() {
            return isCompressed() ? compressedSize : size;
        }

        public String getHash() {
            return hash;
        }

        public long getSize() {
            return size;
        }

        public boolean shouldReconstruct() {
            return reconstruct;
        }

        public boolean isCompressed() {
            return compressedHash != null;
        }

        public String getCompressedHash() {
            return compressedHash;
        }

        public long getCompressedSize() {
            return compressedSize;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            AssetObject that = (AssetObject) o;
            if (compressedSize != that.compressedSize) {
                return false;
            }
            if (reconstruct != that.reconstruct) {
                return false;
            }
            if (size != that.size) {
                return false;
            }
            if (!Objects.equals(compressedHash, that.compressedHash)) {
                return false;
            }
            return Objects.equals(hash, that.hash);
        }

        public int hashCode() {
            int result = hash != null ? hash.hashCode() : 0;
            result = 31 * result + (int) (size ^ size >>> 32);
            result = 31 * result + (reconstruct ? 1 : 0);
            result = 31 * result + (compressedHash != null ? compressedHash.hashCode() : 0);
            result = 31 * result + (int) (compressedSize ^ compressedSize >>> 32);
            return result;
        }

        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("compressed", isCompressed())
                    .append("hash", isCompressed() ? compressedHash : hash)
                    .append("size", isCompressed() ? compressedSize : size)
                    .append("reconstruct", reconstruct)
                    .build();
        }

        /*public String getFilename()
        {
            return getPath(hash);
        }

        public String getCompressedFilename()
        {
            return getPath(compressedHash);
        }*/
    }

    public static String getPath(String hash) {
        return hash.substring(0, 2) + "/" + hash;
    }

    public static String getHash(File file) {
        return FileUtil.getDigest(file, "SHA", 40);
    }
}
