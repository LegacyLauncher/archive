package net.minecraft.launcher.versions;

import com.google.gson.annotations.Expose;
import net.minecraft.launcher.updater.VersionList;
import ru.turikhay.tlauncher.repository.Repository;

import java.util.Date;

public class PartialVersion implements Version {
    private String id;
    private String jar;
    private String url;
    private Date time;
    private Date releaseTime;
    private String type;
    private Repository source;

    @Expose
    private VersionList list;

    public String getID() {
        return id;
    }

    public String getJar() {
        return jar;
    }

    public String getUrl() {
        return url;
    }

    public void setID(String id) {
        this.id = id;
    }

    public ReleaseType getReleaseType() {
        return ReleaseType.of(type);
    }

    @Override
    public String getType() {
        return type;
    }

    public Repository getSource() {
        return source;
    }

    public void setSource(Repository repository) {
        if (repository == null) {
            throw new NullPointerException();
        } else {
            source = repository;
        }
    }

    public Date getUpdatedTime() {
        return time;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public VersionList getVersionList() {
        return list;
    }

    public void setVersionList(VersionList list) {
        if (list == null) {
            throw new NullPointerException();
        } else {
            this.list = list;
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o == null) {
            return false;
        } else if (hashCode() == o.hashCode()) {
            return true;
        } else if (!(o instanceof Version)) {
            return false;
        } else {
            Version compare = (Version) o;
            return compare.getID() != null && compare.getID().equals(id);
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', time=" + time + ", url='" + url + "',release=" + releaseTime + ", type=" + type + ", source=" + source + ", list=" + list + "}";
    }
}
