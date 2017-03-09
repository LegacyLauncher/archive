package ru.turikhay.tlauncher.minecraft.crash;

import ru.turikhay.util.OS;
import ru.turikhay.util.windows.DxDiag;

import java.util.List;
import java.util.regex.Pattern;

public class GraphicsEntry extends PatternContainerEntry {
    private final Pattern intelWin10BugJrePattern, intelWin10BugCardNamePattern, intelBugMinecraft1_10Pattern;
    private final PatternEntry general, amd, intel;

    public GraphicsEntry(CrashManager manager) {
        super(manager, "graphics");

        setAnyPatternMakesCapable(true);

        intelWin10BugJrePattern = manager.getVar("intel-win10-bug-jre-pattern") == null ? null : Pattern.compile("1\\.(?:[8-9]|[1-9][0-9]+)\\.[0-9](?:-.+|_(?!60)([1-9]?)(?:(1)[0-9]|[6-9])[0-9])");
        intelWin10BugCardNamePattern = manager.getVar("intel-win10-bug-card-pattern") == null ? Pattern.compile("Intel HD Graphics(?: [2-3]000)?") : Pattern.compile(manager.getVar("intel-win10-bug-card-pattern"));
        intelBugMinecraft1_10Pattern = manager.getVar("intel-bug-minecraft-1.10") == null ? Pattern.compile(".*1\\.(?:1[0-9]|[2-9][0-9])(?:\\.[\\d]+|)(?:-.+|)") : Pattern.compile(manager.getVar("intel-bug-minecraft-1.10"));

        general = addPattern("general",
                Pattern.compile("[\\s]*org\\.lwjgl\\.LWJGLException\\: Pixel format not accelerated")
        );

        amd = addPattern("amd",
                Pattern.compile(manager.getVar("amd-pattern") == null ? "^#[ ]+C[ ]+\\[atio(?:gl|[0-9a-z]{2,})xx\\.dll\\+0x[0-9a-z]+\\]$" : manager.getVar("amd-pattern"))
        );

        intel = addPattern("intel",
                Pattern.compile(manager.getVar("intel-pattern") == null ? "^# C[ ]+\\[ig[0-9a-z]+icd(?:32|64)\\.dll\\+0x[0-9a-z]+\\]$" : manager.getVar("intel-pattern"))
        );
    }

    @Override
    protected boolean requiresDxDiag() {
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
            log("found AMD pattern");
            return setToUpdateDrivers("AMD");
        }

        if (capablePatterns.contains(intel)) {
            log("found Intel pattern");
            setToUpdateDrivers("Intel");

            if (intelBugMinecraft1_10Pattern.matcher(getManager().getVersion()).matches()) {
                log("We're currently running Minecraft 1.10+, but still having Intel HD graphics drivers issue.");

                setPath("intel.1.10");
                addButton(getManager().getButton("open-settings"));
            }

            return true;
        }

        if (!DxDiag.isScannable()) {
            return true;
        }

        DxDiag result;

        try {
            result = DxDiag.get();
        } catch (Exception e) {
            log("could not get dxdiag result", e);
            return true;
        }

        if (OS.VERSION.contains("10.")) {
            DxDiag.DisplayDevice intelGraphics = result.getDisplayDevice("intel");
            if (intelGraphics != null && intelWin10BugCardNamePattern.matcher(intelGraphics.getCardName()).matches()) {
                log("DXDiag found 1st or 2nd generation of Intel chipset");
                log("External pattern:", intelWin10BugJrePattern);

                if (intelWin10BugJrePattern == null ?
                        (OS.JAVA_VERSION.getMajor() == 8? OS.JAVA_VERSION.getUpdate() > 60 : OS.JAVA_VERSION.getMajor() > 8) : // 8u60 or above
                        intelWin10BugJrePattern.matcher(System.getProperty("java.version")).matches()) {
                    log("We're currently running Java version on Windows 10 that have known incompatibility bug with first- and -second generation Intel HD graphics chipsets");

                    clearButtons();

                    setPath("intel.downgrade-to-jre8u60");
                    newButton("intel.buttons.downgrade-to-jre8u60", new VarUrlAction("intel-bug-jre-link", "http://tlaun.ch/wiki/trbl:intel-8u60"));
                    return true;
                }
            }
        }

        boolean
                haveIntel = result.getDisplayDevice("intel") != null,
                haveNvidia = result.getDisplayDevice("nvidia") != null,
                haveAmd = result.getDisplayDevice("amd") != null || result.getDisplayDevice("ati ") != null;

        if (haveIntel) {
            if (haveNvidia) {
                setToUpdateDrivers("Intel", "NVIDIA");
                newButton("intel-nvidia-select", new VarUrlAction("intel-nvidia-select-url", "http://tlaun.ch/wiki/guide:select-intel-nvidia"));
                return true;
            }
            if (haveAmd) {
                return setToUpdateDrivers("Intel", "AMD");
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

        log("offering to update drivers for:", manufacturers);

        StringBuilder nameBuilder = new StringBuilder();
        for (String manufacturerName : manufacturers) {
            nameBuilder.append(", ").append(manufacturerName);

            String manufacturer = manufacturerName.toLowerCase();
            newButton("driver-update", new VarUrlAction(manufacturer + "-driver-update", "http://tlaun.ch/wiki/update:driver:" + manufacturer), manufacturerName);
        }
        setPath("update-driver", nameBuilder.substring(", ".length()));

        if (manufacturers.length == 1) {
            setImage("manufacturer-" + manufacturers[0].toLowerCase() + ".png");
        }

        return true;
    }

    private class VarUrlAction implements Action {
        private String url;

        VarUrlAction(String varName, String fallbackUrl) {
            String url = getManager().getVar(varName);
            if (url == null) {
                url = fallbackUrl;
            }
            this.url = url;
        }

        @Override
        public void execute() throws Exception {
            OS.openLink(url);
        }
    }
}
