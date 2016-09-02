package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.windows.DxDiag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CrashEntry extends IEntry {
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

    private List<OS> osList = new ArrayList<OS>(Arrays.asList(OS.values())), _osList = Collections.unmodifiableList(osList);

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

    private final List<Button> buttons = new ArrayList<Button>(), _buttons = Collections.unmodifiableList(buttons);

    public final List<Button> getButtons() {
        return _buttons;
    }

    protected final void addButton(Button button) {
        buttons.add(U.requireNotNull(button));
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

    protected boolean checkCapability() throws Exception {
        if (getVersionPattern() != null && !getVersionPattern().matcher(getManager().getVersion()).matches()) {
            log("is not capable because of Minecraft version");
            return false;
        }

        if (getExitCode() != 0 && getExitCode() != getManager().getExitCode()) {
            log("is not capable because of exit code");
            return false;
        }

        if (!isCompatibleWith(OS.CURRENT)) {
            log("is not capable because of OS");
            return false;
        }

        if (getJrePattern() != null && !getJrePattern().matcher(System.getProperty("java.version")).matches()) {
            log("is not capable because of Java version");
            return false;
        }

        if (getGraphicsCardPattern() != null) {
            log("graphics card pattern", getGraphicsCardPattern());

            if (!DxDiag.isScannable()) {
                log("is not capable because it requires DXDiag scanner");
                return false;
            }

            DxDiag result;
            try {
                result = DxDiag.get();
            } catch (Exception e) {
                log("is not capable because DxDiag result is unavailable");
                return false;
            }

            List<DxDiag.DisplayDevice> deviceList = result.getDisplayDevices();
            if (deviceList == null || deviceList.isEmpty()) {
                log("is not capable because display devices list is unavailable:", deviceList);
                return false;
            }

            for (DxDiag.DisplayDevice device : deviceList) {//DXDiagScanner.DXDiagScannerResult.DXDiagDisplayDevice device : deviceList) {
                if (getGraphicsCardPattern().matcher(device.getCardName()).matches()) {
                    log("is capable, found device:", device);
                    return true;
                }
            }

            return false;
        }

        if (isArchIssue()) {
            if (OS.Arch.x86.isCurrent() && DxDiag.isScannable()) {
                boolean is64Bit = false;

                try {
                    is64Bit = DxDiag.get().getSystemInfo().is64Bit();
                } catch (Exception e) {
                    log("Could not determinte if system is 64-bit...");
                }

                if(OS.Arch.x86.isCurrent() && is64Bit) {
                    return true;
                }

                log("is not capable because OS and Java arch are the same");
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
