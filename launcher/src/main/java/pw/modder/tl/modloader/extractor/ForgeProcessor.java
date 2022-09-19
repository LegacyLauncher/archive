package pw.modder.tl.modloader.extractor;

import com.google.common.base.Charsets;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.http.client.fluent.Request;
import pw.modder.http.HttpClientUtils;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ForgeProcessor {
    private final ForgeExtractor.InstallProfile profile;
    private final Map<String, String> vars = new HashMap<>();
    private final Path temp, librariesDir;

    public ForgeProcessor(ForgeExtractor.InstallProfile profile, File librariesDir, File versionsDir) throws IOException, IllegalArgumentException {
        if (profile.getJson() != null && profile.getJsonData() == null)
            throw new IllegalArgumentException("Forge (new) version data is not loaded!");

        this.profile = profile;
        this.temp = Files.createTempDirectory("tl-forge");
        String minecraft_version = profile.getMinecraft();
        this.librariesDir = librariesDir.toPath();

        // process vars
        profile.getData().forEach((key, value) -> {
            String val = value.getClient();
            int len = val.length();
            if (val.charAt(0) == '[' && val.charAt(len - 1) == ']') {
                // artifact
                vars.put(key, this.librariesDir.resolve(getArtifact(val.substring(1, len - 1))).toAbsolutePath().toString());
            } else if (val.charAt(0) == '\'' && val.charAt(len - 1) == '\'') {
                // literal
                vars.put(key, val.substring(1, len - 1));
            } else {
                // temp file
                vars.put(key, temp.resolve(val).toAbsolutePath().toString());
            }
        });

        vars.put("SIDE", "client");
        vars.put("MINECRAFT_JAR", new File(versionsDir, minecraft_version + "/" + minecraft_version + ".jar").getAbsolutePath());
    }

    public void downloadLibraries() throws IOException {
        if (profile.getLibraries() == null) return;

        for (ForgeExtractor.ProcessorLibrary library : profile.getLibraries()) {
            File target = librariesDir.resolve(library.getPath()).toFile();
            HttpClientUtils.execute(Request.Get(library.getUrl()))
                    .saveContent(target);

            if (!FileUtil.getSHA(target).equals(library.getSha1()))
                throw new IOException("Error downloading " + library.getName() + ", SHA1 mismatch!");
        }
    }

    public void runProcessors() throws IOException, InterruptedException {
        if (profile.getProcessors() == null) return;

        for (ForgeExtractor.ForgeProcessor processor : profile.getProcessors()) {
            Path jar = librariesDir.resolve(getArtifact(processor.getJar()));
            String classpath = processor.getClasspath().stream()
                    .map(lib -> librariesDir.resolve(getArtifact(lib)).toAbsolutePath().toString())
                    .collect(Collectors.joining(File.pathSeparator));
            classpath = classpath + File.pathSeparator + jar.toAbsolutePath();

            JarFile jarFile = new JarFile(jar.toFile());
            String mainClass;
            try {
                mainClass = jarFile.getManifest().getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            } finally {
                jarFile.close();
            }

            List<String> commands = Arrays.asList(
                    "-cp",
                    classpath,
                    mainClass
            );

            List<String> processed_args = processor.getArgs().stream()
                    .map(this::processVar)
                    .collect(Collectors.toList());

            commands.addAll(processed_args);

            JavaProcessLauncher l = new JavaProcessLauncher(Charsets.UTF_8, OS.getJavaPath(), commands.toArray(new String[]{}));
            // run processor
            JavaProcess p = l.start();
            p.getRawProcess().waitFor();
            // after processor run
            if (processor.getOutput() == null) continue;
            for (Map.Entry<String, String> entry : processor.getOutput().entrySet()) {
                // check SHA-1
                File file = new File(vars.get(processVar(entry.getKey())));
                String sha1 = vars.get(processVar(entry.getValue()));

                if (sha1.equals(FileUtil.getSHA(file))) continue;
                throw new IllegalArgumentException("Processor output SHA is not expected");
            }
        }
    }

    public CompleteVersion getCompleteVersion() {
        if (profile.getJson() == null) return profile.getVersionInfo().getCompleteVersion();

        return profile.getJsonData();
    }

    private String processVar(String val) {
        int len = val.length();
        if (val.charAt(0) == '[' && val.charAt(len - 1) == ']') {
            // artifact
            return librariesDir.resolve(getArtifact(val.substring(1, len - 1))).toAbsolutePath().toString();
        }

        if (val.charAt(0) == '{' && val.charAt(len - 1) == '}') {
            // variable
            return vars.get(val.substring(1, len - 1));
        }

        return val;
    }

    private static String getArtifact(String name) {
        if (name == null)
            throw new NullPointerException("Artifact name is null!");

        String[] name_parts = name.split("@", 2);
        String[] path_parts = name_parts[0].split(":", 4);
        String extension = (name_parts.length == 2) ? name_parts[1] : "jar";

        if (path_parts.length == 4)
            // group:id:version:classifier@extension
            // example: de.oceanlabs.mcp:mcp_config:1.16.4-20201102.104115:mappings@txt
            return String.format(Locale.ROOT, "%1$s/%2$s/%3$s/%2$s-%3$s-%4$s.%5$s",
                    path_parts[0].replace('.', '/'),
                    path_parts[1],
                    path_parts[2],
                    path_parts[3],
                    extension
            );

        if (path_parts.length == 3)
            // group:id:version@extension
            return String.format(Locale.ROOT, "%1$s/%2$s/%3$s/%2$s-%3$s.%4$s",
                    path_parts[0].replace('.', '/'),
                    path_parts[1],
                    path_parts[2],
                    extension
            );

        throw new IllegalArgumentException("Provided string is not a Maven Artifact locator!");
    }
}
