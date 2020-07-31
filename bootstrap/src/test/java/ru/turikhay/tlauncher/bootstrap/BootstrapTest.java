package ru.turikhay.tlauncher.bootstrap;

import com.github.zafarkhaja.semver.Version;
import org.testng.annotations.Test;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.LocalLauncherMeta;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.task.TaskList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;

public class BootstrapTest {

    @Test
    public void testDownloadLibraries() throws Exception {
        Bootstrap bootstrap = Bootstrap.createBootstrap();
        LocalLauncherMeta localLauncherMeta = Json.parse(getClass().getResourceAsStream("/launcher_meta.json"), LocalLauncherMeta.class);

        TaskList list = bootstrap.downloadLibraries(localLauncherMeta);
        list.call();

        Set<Task> taskSet = list.getTaskSet();
        assertEquals(localLauncherMeta.getLibraries().size(), taskSet.size());
        assertFalse(taskSet.isEmpty());

        for(Task task : taskSet) {
            assertFalse(task.isExecuting(), task + " execute state");
            assertTrue(task.getProgress() == 1., task + " finish state");
        }
    }

    @Test
    public void testShouldUpdateBootstrap() throws Exception {
        Bootstrap bootstrap = Bootstrap.createBootstrap();

        UpdateMeta updateMeta = UpdateMeta.fetchFrom(getClass().getResourceAsStream("/bootstrap_update.json"));
        assertNotNull(updateMeta.getBootstrap());

        Version currentVersion = bootstrap.getMeta().getVersion();

        updateMeta.getBootstrap().setVersion(currentVersion.incrementMajorVersion());
        assertNotNull(bootstrap.getBootstrapUpdate(updateMeta));

        updateMeta.getBootstrap().setVersion(currentVersion.incrementMinorVersion());
        assertNotNull(bootstrap.getBootstrapUpdate(updateMeta));

        updateMeta.getBootstrap().setVersion(currentVersion.incrementPatchVersion());
        assertNotNull(bootstrap.getBootstrapUpdate(updateMeta));
    }

    @Test
    public void testCreateBootstrapWithJvmArgs() throws Exception {
        Map<String, String> propMap = new HashMap<String, String>();
        propMap.put("brand", "mcl");

        propMap.put("targetJar", "legacy.jar");
        propMap.put("targetLibFolder", "lib");

        try {
            setupJvmProperties(propMap, false);
            Bootstrap bootstrap = Bootstrap.createBootstrap();
            assertEquals(bootstrap.getMeta().getShortBrand(), propMap.get("brand"));

            assertEquals(bootstrap.getTargetJar(), new File(propMap.get("targetJar")));
            assertEquals(bootstrap.getTargetLibFolder(), new File(propMap.get("targetLibFolder")));
        } finally {
            setupJvmProperties(propMap, true);
        }
    }

    @Test
    public void testHandleFatalError() throws Exception {
        Bootstrap bootstrap = Bootstrap.createBootstrap();
        Bootstrap.handleFatalError(bootstrap, null, new ClassNotFoundException(), false);
    }

    private static final String jvmArgPrefix = "tlauncher.bootstrap.";
    private void setupJvmProperties(Map<String, String> propMap, boolean clear) {
        for(Map.Entry<String, String> entry : propMap.entrySet()) {
            if(clear) {
                System.clearProperty(jvmArgPrefix + entry.getKey());
            } else {
                System.setProperty(jvmArgPrefix + entry.getKey(), entry.getValue());
            }
        }
    }

}