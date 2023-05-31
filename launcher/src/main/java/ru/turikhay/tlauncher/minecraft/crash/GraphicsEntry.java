package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.OS;
import ru.turikhay.util.sysinfo.GraphicsCard;
import ru.turikhay.util.sysinfo.SystemInfo;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class GraphicsEntry extends PatternContainerEntry {
    private static final Logger LOGGER = LogManager.getLogger(GraphicsEntry.class);

    private final Pattern intelWin10BugJrePattern, intelWin10BugCardNamePattern, intelBugMinecraft1_10Pattern;
    private final PatternEntry amd, intel;

    public GraphicsEntry(CrashManager manager) {
        super(manager, "graphics");

        setAnyPatternMakesCapable(true);

        intelWin10BugJrePattern = manager.getVar("intel-win10-bug-jre-pattern") == null ? null : Pattern.compile("1\\.(?:[8-9]|[1-9][0-9]+)\\.[0-9](?:-.+|_(?!60)([1-9]?)(?:(1)[0-9]|[6-9])[0-9])");
        intelWin10BugCardNamePattern = manager.getVar("intel-win10-bug-card-pattern") == null ? Pattern.compile("Intel HD Graphics(?: [2-3]000)?") : Pattern.compile(manager.getVar("intel-win10-bug-card-pattern"));
        intelBugMinecraft1_10Pattern = manager.getVar("intel-bug-minecraft-1.10") == null ? Pattern.compile(".*1\\.(?:1[0-9]|[2-9][0-9])(?:\\.[\\d]+|)(?:-.+|)") : Pattern.compile(manager.getVar("intel-bug-minecraft-1.10"));

        addPattern("general", Pattern.compile("[\\s]*org\\.lwjgl\\.LWJGLException: Pixel format not accelerated"));
        addPattern("general", Pattern.compile("WGL: The driver does not appear to support OpenGL"));

        amd = addPattern("amd",
                Pattern.compile(manager.getVar("amd-pattern") == null ? "^#[ ]+C[ ]+\\[atio(?:gl|[0-9a-z]{2,})xx\\.dll\\+0x[0-9a-z]+]$" : manager.getVar("amd-pattern"))
        );

        intel = addPattern("intel",
                Pattern.compile(manager.getVar("intel-pattern") == null ? "^# C[ ]+\\[ig[0-9a-z]+icd(?:32|64)\\.dll\\+0x[0-9a-z]+]$" : manager.getVar("intel-pattern"))
        );
    }

    @Override
    protected boolean requiresSysInfo() {
        return true;
    }

    @Override
    protected boolean checkCapability(List<PatternEntry> capablePatterns) {
        if (!OS.WINDOWS.isCurrent()) {
            setPath("general-linux");
            return true;
        }

        setPath("general");

        if (capablePatterns.contains(amd)) {
            LOGGER.info("{} is relevant because the crash logs explicitly mention " +
                    "AMD graphics card drivers", getName());
            return setToUpdateDrivers("AMD");
        }

        if (capablePatterns.contains(intel)) {
            LOGGER.info("{} is relevant because the crash logs explicitly mention " +
                    "Intel graphics card drivers", getName());
            setToUpdateDrivers("Intel");

            if (intelBugMinecraft1_10Pattern.matcher(getManager().getVersion()).matches()) {
                LOGGER.info("We're currently running Minecraft 1.10+, but still having Intel HD graphics " +
                        "driver issue.");

                setPath("intel.1.10");
                addButton(getManager().getButton("open-settings"));
            }

            return true;
        }

        SystemInfo sysInfo;

        try {
            sysInfo = getManager().getSystemInfoReporter().getReport().get();
        } catch (Exception e) {
            LOGGER.warn("Could not get system info result", e);
            return true;
        }

        if (OS.VERSION.contains("10.")) {
            Optional<GraphicsCard> intelGraphics = getDisplayDevice(sysInfo, "intel");
            if (intelGraphics.filter(card ->
                    intelWin10BugCardNamePattern.matcher(card.getName()).matches()).isPresent()) {
                LOGGER.info("Using DxDiag we found out that the machine is running on 1st or 2nd generation of " +
                        "Intel graphics chipset");
                LOGGER.debug("External pattern: {}", intelWin10BugJrePattern);

                if (intelWin10BugJrePattern == null ?
                        (OS.JAVA_VERSION.getMajor() == 8 ? OS.JAVA_VERSION.getUpdate() > 60 : OS.JAVA_VERSION.getMajor() > 8) : // 8u60 or above
                        intelWin10BugJrePattern.matcher(System.getProperty("java.version")).matches()) {
                    LOGGER.info("We're currently running Java version on Windows 10 that have known incompatibility " +
                            "bug with 1st and 2nd generation Intel HD graphics chipsets");

                    clearButtons();

                    setPath("intel.downgrade-to-jre8u60");
                    newButton("intel.buttons.downgrade-to-jre8u60", new VarUrlAction("intel-bug-jre-link", "https://yadi.sk/d/dvzmBSqttQXhy/Java%208%20update%2045/Installers"));
                    return true;
                }
            }
        }

        boolean
                haveIntel = getDisplayDevice(sysInfo, "intel").isPresent(),
                haveNvidia = getDisplayDevice(sysInfo, "nvidia").isPresent(),
                haveAmd = getDisplayDevice(sysInfo, "amd").isPresent() || getDisplayDevice(sysInfo, "ati ").isPresent();

        if (haveIntel) {
            if (haveNvidia) {
                setToUpdateDrivers("Intel", "NVIDIA");
                newButton("intel-nvidia-select", new VarUrlAction("intel-nvidia-select-url", "https://wiki.llaun.ch/guide:gpu-select:nvidia"));
                return true;
            }
            if (haveAmd) {
                setToUpdateDrivers("Intel", "AMD");
                newButton("intel-amd-select", new VarUrlAction("intel-nvidia-select-url", "https://wiki.llaun.ch/guide:gpu-select:amd"));
                return true;
            }
            return setToUpdateDrivers("Intel");
        }

        if (haveAmd && haveNvidia) {
            return setToUpdateDrivers("AMD", "NVIDIA");
        }

        if (haveNvidia) {
            return setToUpdateDrivers("NVIDIA");
        }

        if (haveAmd) {
            return setToUpdateDrivers("AMD");
        }

        setPermitHelp(false);
        return true;
    }

    private boolean setToUpdateDrivers(String... manufacturers) {
        clearButtons();

        StringBuilder nameBuilder = new StringBuilder();
        for (String manufacturerName : manufacturers) {
            nameBuilder.append(", ").append(manufacturerName);

            String manufacturer = manufacturerName.toLowerCase(java.util.Locale.ROOT);
            newButton("driver-update", new VarUrlAction(manufacturer + "-driver-update", "https://wiki.llaun.ch/update:driver:" + manufacturer), manufacturerName);
        }
        setPath("update-driver", nameBuilder.substring(", ".length()));

        if (manufacturers.length == 1) {
            setImage("logo-" + manufacturers[0].toLowerCase(java.util.Locale.ROOT) + "@32");
        }

        return true;
    }

    private static Optional<GraphicsCard> getDisplayDevice(SystemInfo systemInfo, String name) {
        name = name.toLowerCase(java.util.Locale.ROOT);
        for (GraphicsCard card : systemInfo.getGraphicsCards()) {
            if (card.getName() == null) {
                continue;
            }
            if (card.getName().toLowerCase(java.util.Locale.ROOT).contains(name)) {
                return Optional.of(card);
            }
        }
        return Optional.empty();
    }

    private class VarUrlAction implements Action {
        private final String url;

        VarUrlAction(String varName, String fallbackUrl) {
            String url = getManager().getVar(varName);
            if (url == null) {
                url = fallbackUrl;
            }
            this.url = url;
        }

        @Override
        public void execute() {
            OS.openLink(url);
        }
    }
}
