package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.jna.JNA;
import ru.turikhay.util.OS;
import ru.turikhay.util.sysinfo.GraphicsCard;
import ru.turikhay.util.sysinfo.SystemInfo;

import java.util.*;
import java.util.regex.Pattern;

public class CrashEntry extends IEntry {
    private static final Logger LOGGER = LogManager.getLogger(CrashEntry.class);

    public CrashEntry(CrashManager manager, String name) {
        super(manager, name);
        setPath(null);
    }

    private boolean localizable = true;

    public final boolean isLocalizable() {
        return localizable;
    }

    protected final void setLocalizable(boolean localizable) {
        this.localizable = localizable;
    }

    private boolean fake;

    public final boolean isFake() {
        return fake;
    }

    protected final void setFake(boolean fake) {
        this.fake = fake;
    }

    private boolean permitHelp = true;

    public final boolean isPermitHelp() {
        return permitHelp;
    }

    public void setPermitHelp(boolean permitHelp) {
        this.permitHelp = permitHelp;
    }

    private int exitCode;

    public final int getExitCode() {
        return exitCode;
    }

    protected final void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    private final List<OS> osList = new ArrayList<>(Arrays.asList(OS.values()));
    private final List<OS> _osList = Collections.unmodifiableList(osList);

    public final boolean isCompatibleWith(OS os) {
        return osList.contains(os);
    }

    protected void setOS(OS... os) {
        osList.clear();
        Collections.addAll(osList, os);
    }

    private boolean archIssue;

    public final boolean isArchIssue() {
        return archIssue;
    }

    protected final void setArchIssue(boolean archIssue) {
        this.archIssue = archIssue;
    }

    private Pattern graphicsCardPattern;

    public final Pattern getGraphicsCardPattern() {
        return graphicsCardPattern;
    }

    protected final void setGraphicsCardPattern(Pattern graphicsCardPattern) {
        this.graphicsCardPattern = graphicsCardPattern;
    }

    private Pattern versionPattern;

    public final Pattern getVersionPattern() {
        return versionPattern;
    }

    protected final void setVersionPattern(Pattern versionPattern) {
        this.versionPattern = versionPattern;
    }

    private Pattern jrePattern;

    public final Pattern getJrePattern() {
        return jrePattern;
    }

    protected final void setJrePattern(Pattern jrePattern) {
        this.jrePattern = jrePattern;
    }

    private String imagePath;

    public final String getImage() {
        return imagePath;
    }

    protected final void setImage(String imagePath) {
        this.imagePath = imagePath;
    }

    private String title;
    private Object[] titleVars;

    public final String getTitle() {
        return title;
    }

    protected final void setTitle(String title, Object... vars) {
        this.title = title;
        titleVars = vars;
    }

    public final Object[] getTitleVars() {
        return titleVars;
    }

    private String body;
    private Object[] bodyVars;

    public final String getBody() {
        return body;
    }

    protected final void setBody(String body, Object... vars) {
        this.body = body;
        bodyVars = vars;
    }

    public final Object[] getBodyVars() {
        return bodyVars;
    }

    protected final void setPath(String path, Object... vars) {
        String prefix = getLocPath(path);
        setTitle(prefix + ".title", vars);
        setBody(prefix + ".body", vars);
    }

    private final List<Button> buttons = new ArrayList<>(), _buttons = Collections.unmodifiableList(buttons);

    public final List<Button> getButtons() {
        return _buttons;
    }

    protected final void addButton(Button button) {
        buttons.add(Objects.requireNonNull(button));
    }

    protected final void clearButtons() {
        buttons.clear();
    }

    protected Button newButton(String text, Action action, Object... vars) {
        //final String path = getLocPath(StringUtil.requireNotBlank(text, "text"));
        Button button = new Button(text);
        button.setLocalizable(true, false);
        button.setText(text, vars);
        button.getActions().add(action);
        addButton(button);
        return button;
    }

    protected boolean requiresSysInfo() {
        return isCompatibleWith(OS.WINDOWS) && (getGraphicsCardPattern() != null || isArchIssue());
    }

    protected boolean checkCapability() throws Exception {
        if (getVersionPattern() != null && !getVersionPattern().matcher(getManager().getVersion()).matches()) {
            LOGGER.debug("{} is not relevant because of Minecraft version", getName());
            return false;
        }

        if (getExitCode() != 0 && getExitCode() != getManager().getExitCode()) {
            LOGGER.debug("{} is not relevant because of exit code", getName());
            return false;
        }

        if (!isCompatibleWith(OS.CURRENT)) {
            LOGGER.debug("{} is not relevant because of OS", getName());
            return false;
        }

        if (getJrePattern() != null && !getJrePattern().matcher(System.getProperty("java.version")).matches()) {
            LOGGER.debug("{} is not relevant because of Java version", getName());
            return false;
        }

        if (getGraphicsCardPattern() != null) {
            LOGGER.debug("graphics card pattern of {}: {}", getName(), getGraphicsCardPattern());

            SystemInfo systemInfo;
            try {
                systemInfo = getManager().getSystemInfoReporter().getReport().get();
            } catch (Exception e) {
                LOGGER.debug("{} is not capable because system info report is unavailable", getName());
                return false;
            }

            List<GraphicsCard> graphicsCards = systemInfo.getGraphicsCards();
            if (graphicsCards == null || graphicsCards.isEmpty()) {
                LOGGER.debug("{} is not capable because graphics devices list is unavailable: {}",
                        getName(), graphicsCards);
                return false;
            }

            for (GraphicsCard card : graphicsCards) {
                if (getGraphicsCardPattern().matcher(card.getName()).matches()) {
                    LOGGER.debug("{} is capable, found device: {}", getName(), card);
                    return true;
                }
            }

            return false;
        }

        if (isArchIssue()) {
            if (OS.Arch.x86.isCurrent()) {
                boolean is64Bit;

                Optional<Boolean> jnaIs64Bit = JNA.is64Bit();
                if (jnaIs64Bit.isPresent()) {
                    is64Bit = jnaIs64Bit.get();
                    LOGGER.info("JNA reported system is 64-bit: {}", is64Bit);
                } else {
                    try {
                        is64Bit = getManager().getSystemInfoReporter().getReport().get().is64Bit();
                        LOGGER.info("DxDiag reported system is 64-bit: {}", is64Bit);
                    } catch (Exception e) {
                        LOGGER.warn("Could not detect if system is 64-bit");
                        is64Bit = false;
                    }
                }

                if (OS.Arch.x86.isCurrent() && is64Bit) {
                    return true;
                }

                LOGGER.debug("{} is not capable because OS and Java arch are the same", getName());
            }
            return false;
        }

        return true;
    }

    String getLocPath(String path) {
        return path == null ? "crash." + getName() : "crash." + getName() + "." + path;
    }

    @Override
    public ToStringBuilder buildToString() {
        return super.buildToString()
                .append("exitCode", exitCode)
                .append("fake", fake)
                .append("permitHelp", permitHelp)
                .append("os", osList)
                .append("archIssue", archIssue)
                .append("version", versionPattern)
                .append("jre", jrePattern)
                .append("graphics", graphicsCardPattern)
                .append("title", getTitle())
                .append("body", getBody())
                .append("buttons", getButtons());
    }
}
