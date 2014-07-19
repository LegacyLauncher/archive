package net.minecraft.launcher.updater;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class AssetIndex {
	public static final String DEFAULT_ASSET_NAME = "legacy";
	private Map<String, AssetObject> objects;
	private boolean virtual;

	public AssetIndex() {
		this.objects = new LinkedHashMap<String, AssetObject>();
	}

	public Map<String, AssetObject> getFileMap() {
		return this.objects;
	}

	public Set<AssetObject> getUniqueObjects() {
		return new HashSet<AssetObject>(this.objects.values());
	}

	public boolean isVirtual() {
		return this.virtual;
	}

	public class AssetObject {
		private String filename;
		private String hash;
		private long size;

		public AssetObject() {
		}

		public String getHash() {
			return this.hash;
		}

		public long getSize() {
			return this.size;
		}

		public String getFilename() {
			if (filename == null)
				filename = getHash().substring(0, 2) + "/" + getHash();
			return filename;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;

			if (o == null || getClass() != o.getClass())
				return false;

			AssetObject that = (AssetObject) o;

			if (this.size != that.size)
				return false;

			return hash.equals(that.hash);
		}

		@Override
		public int hashCode() {
			int result = this.hash.hashCode();
			result = 31 * result + (int) (this.size ^ this.size >>> 32);
			return result;
		}
	}
}
