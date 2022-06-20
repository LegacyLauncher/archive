package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.versions.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.OS;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;

class BaseVersionTable {
    private static final int ONE_GIG = 1024, TWO_GIGS = 2048, THREE_GIGS = 3072, FOUR_GIGS = 4096;

    private static final Logger LOGGER = LogManager.getLogger(BaseVersionTable.class);

    private static final Lazy<BaseVersionTable> TABLE = Lazy.of(BaseVersionTable::new);

    public static BaseVersionTable getInstance() {
        return TABLE.get();
    }

    private final Date firstVersionReleaseDate = parseDate("2009-05-13T20:11:00+00:00");
    private final List<BaseVersion> versions = new ArrayList<>();

    BaseVersionTable() {
        init();
    }

    public BaseVersion findBaseVersion(MemoryAllocationService.VersionContext version) {
        Date releaseTime = version.getReleaseTime();
        if (releaseTime.compareTo(firstVersionReleaseDate) >= 0) {
            BaseVersion match = null;
            for (BaseVersion baseVersion : versions) {
                if (releaseTime.compareTo(baseVersion.releaseTime) >= 0) {
                    match = baseVersion;
                } else {
                    break;
                }
            }
            if (match != null) {
                LOGGER.debug("Found match for {} ({}): {}", version.getId(), releaseTime, match.family);
                return match;
            }
        }
        LOGGER.warn("No match found for {} ({})", version.getId(), releaseTime);
        return null;
    }

    public BaseVersion getDefaultBaseVersion() {
        return versions.get(versions.size() - 1);
    }

    private boolean hasSimulationDistance;

    private void init() {
        // old versions rarely require more than 512 MiB, but as far as I remember
        // the base requirement was bumped to 1 GiB pretty early
        add("old", "rd-132211", "2009-05-13T20:11:00+00:00", (ctx) -> ONE_GIG);

        // first version to require 500+ MiB at all times
        add("1.3", "1.3", "2012-07-25T22:00:00+00:00", (ctx) -> ONE_GIG);

        // big technical update
        add("1.13", "18w07a", "2018-02-14T17:34:13+00:00", (ctx) -> TWO_GIGS);

        // revamped world generation and increased world height
        hasSimulationDistance = true;
        add("1.18", "1.18_experimental-snapshot-1", "2021-07-13T12:54:19+00:00",
                (ctx) -> {
                    boolean isMacOsArm = ctx.os.isOs(OS.OSX) && ctx.os.arch.isARM();
                    if (ctx.mem.total < 5500) {
                        if (isMacOsArm || ctx.mem.total < 3500) {
                            return TWO_GIGS;
                        }
                        return THREE_GIGS;
                    }
                    if (isMacOsArm) {
                        if (ctx.mem.total < 12000) {
                            return TWO_GIGS;
                        }
                    }
                    return FOUR_GIGS;
                });
    }

    private void add(String family, String id, String releaseTime,
                     Function<MemoryAllocationService.QueryContext, Integer> baseRequirement) {
        BaseVersion baseVersion = new BaseVersion(
                family,
                id,
                parseDate(releaseTime),
                baseRequirement,
                hasSimulationDistance
        );
        versions.add(baseVersion);
    }

    static class BaseVersion implements Comparable<BaseVersion> {
        final String family;
        final String id;
        final Date releaseTime;
        final Function<MemoryAllocationService.QueryContext, Integer> requirement;
        final boolean hasSimulationDistance;

        BaseVersion(String family, String id, Date releaseTime,
                    Function<MemoryAllocationService.QueryContext, Integer> requirement,
                    boolean hasSimulationDistance) {
            this.family = family;
            this.id = id;
            this.releaseTime = releaseTime;
            this.requirement = requirement;
            this.hasSimulationDistance = hasSimulationDistance;
        }

        @Override
        public int compareTo(BaseVersion o) {
            return releaseTime.compareTo(o.releaseTime);
        }
    }

    static Date parseDate(String date) {
        return Date.from(OffsetDateTime.parse(date).toInstant());
    }
}
