package net.minecraft.launcher.versions;

import java.util.Date;

import ru.turikhay.tlauncher.repository.Repository;
import net.minecraft.launcher.updater.VersionList;

public class PartialVersion implements Version, Cloneable {
	private String id;
	private Date time;
	private Date releaseTime;
	private ReleaseType type;

	private Repository source;
	private VersionList list;

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public ReleaseType getReleaseType() {
		return type;
	}

	@Override
	public Repository getSource() {
		return source;
	}

	@Override
	public void setSource(Repository repository) {
		if (repository == null)
			throw new NullPointerException();

		this.source = repository;
	}

	@Override
	public Date getUpdatedTime() {
		return time;
	}

	@Override
	public Date getReleaseTime() {
		return releaseTime;
	}

	@Override
	public VersionList getVersionList() {
		return list;
	}

	@Override
	public void setVersionList(VersionList list) {
		if (list == null)
			throw new NullPointerException();

		this.list = list;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (this.hashCode() == o.hashCode())
			return true;

		if (!(o instanceof Version))
			return false;

		Version compare = (Version) o;
		if (compare.getID() == null)
			return false;

		return compare.getID().equals(id);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id='" + id + "', time=" + time
				+ ", release=" + releaseTime + ", type=" + type + ", source="
				+ source + ", list=" + list + "}";
	}
	
	@Override
	public Object cloneSafely() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
