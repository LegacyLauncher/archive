package ru.turikhay.tlauncher.minecraft.crash;

import net.minecraft.launcher.versions.CompleteVersion;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.JavaManager;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;

import java.util.Optional;
import java.util.regex.Pattern;

public class Java16Entry extends PatternEntry {
    public Java16Entry(CrashManager manager) {
        super(manager, "java16",
                Pattern.compile("^Exception in thread \"main\" java.lang.UnsupportedClassVersionError" +
                        ": .+ has been compiled by a more recent version" +
                        " of the Java Runtime \\(class file version .+\\), this version of the Java Runtime" +
                        " only recognizes class file versions up to .+$")
        );
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if(!super.checkCapability()) {
            return false;
        }

        CompleteVersion version = getManager().getLauncher().getCompleteVersion();

        String requiredJava = Optional.ofNullable(version.getJavaVersion())
                .map(CompleteVersion.JavaVersion::getMajorVersion)
                .map(String::valueOf)
                .orElse("16+");

        String jreType = TLauncher.getInstance().getSettings()
                .get(JavaManagerConfig.class).getJreTypeOrDefault().getType();

        if(jreType.equals(JavaManagerConfig.Recommended.TYPE)) {
            // we use recommended jre
            if(version.getReleaseTime() != null
                    && version.getReleaseTime().toInstant().compareTo(JavaManager.JAVA16_UPGRADE_POINT) >= 0)
            {
                // this version released after the Java 16 upgrade point
                if(getManager().getLauncher().getJreType().getType()
                        .equals(JavaManagerConfig.Current.TYPE))
                {
                    // but the launcher used current JRE
                    if(version.getJavaVersion() == null) {
                        // version has no java version requirement
                        // => an old launcher version corrupted json file
                        setPath("force-update-version");
                        addButton(getManager().getButton("force-update"));
                        return true;
                    }
                }
            } else {
                // but the version released before the upgrade point
                // => ???
                setPath("old-java", requiredJava);
            }
        } else {
            // => user probably has no idea what they're doing
            setPath("change-to-recommended", requiredJava);
            newButton("retry-with-recommended", () -> {
                TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .jre.setValue(JavaManagerConfig.Recommended.TYPE);
                TLauncher.getInstance().getFrame().mp.defaultScene.settingsForm.get()
                        .saveValues();
                TLauncher.getInstance().getUIListeners().getMinecraftUIListener()
                        .getCrashProcessingFrame().get().getCrashFrame().setVisible(false);
                TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.startLauncher();
            });
            setPermitHelp(false);
        }
        return true;
    }
}
