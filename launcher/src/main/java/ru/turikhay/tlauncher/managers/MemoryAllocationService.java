package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.versions.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.OS;
import ru.turikhay.util.async.AsyncThread;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Future;

public class MemoryAllocationService {
    private static final Logger LOGGER = LogManager.getLogger(MemoryAllocationService.class);

    static final int DEFAULT_BASE_REQUIREMENT = 2048;

    private final OsInfo os;
    private final MemoryInfo mem;

    MemoryAllocationService(OsInfo os, MemoryInfo context) {
        this.os = os;
        this.mem = context;
    }

    public MemoryAllocationService() {
        this(
                new OsInfo(OS.CURRENT, OS.Arch.CURRENT),
                new MemoryInfo((int) Math.min(OS.Arch.TOTAL_RAM_MB, Integer.MAX_VALUE))
        );
    }

    private final Lazy<Range> range = Lazy.of(this::doQueryRange);

    private final Lazy<Hint> fallbackHint = Lazy.of(() ->
            new Hint(doQueryDefaultXmx(), DEFAULT_BASE_REQUIREMENT, false)
    );

    public MemoryInfo getMemoryInfo() {
        return mem;
    }

    public Range getRange() {
        return range.get();
    }

    public Future<Hint> queryHint(VersionContext versionContext) {
        return AsyncThread.future(() -> doQueryHint(versionContext));
    }

    public Hint getFallbackHint() {
        return fallbackHint.get();
    }

    private int doQueryDefaultXmx() {
        if (os.isArch(OS.Arch.x86) || mem.total <= 2000) {
            return Math.min(1024, mem.total);
        } else if (mem.total <= 2500) {
            return 1536;
        }
        return DEFAULT_BASE_REQUIREMENT;
    }

    private Range doQueryRange() {
        if (os.isArch(OS.Arch.x86) || mem.total <= 1200) {
            return new Range(
                    Math.min(512, mem.total),
                    Math.min(1024, mem.total)
            );
        }
        return new Range(Math.min(1024, mem.total), mem.total);
    }

    private Hint doQueryHint(VersionContext versionCtx) {
        BaseVersionTable table = BaseVersionTable.getInstance();
        BaseVersionTable.BaseVersion base = table.findBaseVersion(versionCtx);
        if (base == null) {
            base = table.getDefaultBaseVersion();
            LOGGER.warn("No base version found for {}, will use fallback: {}",
                    versionCtx.id, base.family);
        }
        int baseRequirement = base.requirement.apply(new QueryContext(os, mem, versionCtx));
        int xmx = baseRequirement;
        if (mem.total >= 6000 && versionCtx.isForge()) {
            xmx = Math.min(mem.safeMax, xmx * 2);
            LOGGER.debug("Detected Forge version: bumping x 2: {} -> {} MiB",
                    baseRequirement, xmx);
        }
        int actual = Math.min(mem.safeMax, xmx);
        if (!os.is64Bit()) {
            actual = Math.min(1024, actual);
        }
        return new Hint(actual, xmx, true);
    }

    @Override
    public String toString() {
        return "MemoryAllocationService{" +
                "os=" + os +
                ", mem=" + mem +
                ", range=" + range +
                ", fallbackHint=" + fallbackHint +
                '}';
    }

    public static class Hint {
        private final int actual, desired;
        private final boolean confident;

        public Hint(int actual, int desired, boolean confident) {
            this.actual = actual;
            this.desired = desired;
            this.confident = confident;
        }

        public int getActual() {
            return actual;
        }

        public int getDesired() {
            return desired;
        }

        public boolean isConfident() {
            return confident;
        }

        public boolean isUnderAllocation() {
            return actual < desired;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Hint hint = (Hint) o;
            return actual == hint.actual && desired == hint.desired && confident == hint.confident;
        }

        @Override
        public int hashCode() {
            return Objects.hash(actual, desired, confident);
        }

        @Override
        public String toString() {
            return "Hint{" +
                    "actual=" + actual +
                    ", desired=" + desired +
                    ", confident=" + confident +
                    '}';
        }

    }

    public static class Range {
        private final int min, max;

        public Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "min=" + min +
                    ", max=" + max +
                    '}';
        }
    }

    private static int roundToClosestBase(Number value, int base) {
        return (int) (Math.round(value.doubleValue() / base) * base);
    }

    private static int computeClosestBase(Number value) {
        // floorToClosestBase(4778, 1024) == 2048
        // floorToClosestBase(4779, 1024) == 3072
        if (value.intValue() < 4779) {
            return 256;
        } else {
            return 1024;
        }
    }

    public static class OsInfo {
        final OS os;
        final OS.Arch arch;

        public OsInfo(OS os, OS.Arch arch) {
            this.os = os;
            this.arch = arch;
        }

        public boolean isOs(OS os) {
            return this.os == os;
        }

        public boolean isArch(OS.Arch arch) {
            return this.arch == arch;
        }

        public boolean isArm() {
            return this.arch.isARM();
        }

        public boolean is64Bit() {
            return this.arch.is64Bit();
        }

        @Override
        public String toString() {
            return "OsInfo{" +
                    "os=" + os +
                    ", arch=" + arch +
                    '}';
        }
    }

    public static class MemoryInfo {
        final int total, base, safeMax;

        MemoryInfo(int total) {
            this.total = total;
            this.base = computeClosestBase(total);
            this.safeMax = Math.max(
                    Math.min(total, 1024),
                    roundToClosestBase(total * (total < 2048 ? .5 : .75), base)
            );
        }

        public int getTotal() {
            return total;
        }

        public int getBase() {
            return base;
        }

        public int getSafeMax() {
            return safeMax;
        }

        @Override
        public String toString() {
            return "MemoryInfo{" +
                    "total=" + total +
                    ", base=" + base +
                    ", safeMax=" + safeMax +
                    '}';
        }
    }

    public static class VersionContext {
        private final String id;
        private final Date releaseTime;
        private final @Nullable Path gameDir;

        public VersionContext(String id, Date releaseTime, @Nullable Path gameDir) {
            this.id = id;
            this.releaseTime = releaseTime;
            this.gameDir = gameDir;
        }

        public VersionContext(Version version, @Nullable Path gameDir) {
            this(version.getID(), version.getReleaseTime(), gameDir);
        }

        public String getId() {
            return id;
        }

        public Date getReleaseTime() {
            return releaseTime;
        }

        public boolean isForge() {
            return id.toLowerCase(Locale.ROOT).contains("forge");
        }

        @Override
        public String toString() {
            return "VersionContext{" +
                    "id='" + id + '\'' +
                    ", releaseTime=" + releaseTime +
                    ", gameDir=" + gameDir +
                    '}';
        }
    }

    static class QueryContext {
        final OsInfo os;
        final MemoryInfo mem;
        final VersionContext versionCtx;

        QueryContext(OsInfo os, MemoryInfo mem, VersionContext versionCtx) {
            this.os = os;
            this.mem = mem;
            this.versionCtx = versionCtx;
        }
    }
}
