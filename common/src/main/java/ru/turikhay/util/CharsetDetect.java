package ru.turikhay.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CharsetDetect {

    public static void main(String[] args) {
        System.out.println("Charset: \"" + Charset.defaultCharset().name() + "\"");
    }

    public static Charset detect() {
        return detect(
                System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        );
    }

    public static Charset detect(String javaExec) {
        CodeSource codeSource = CharsetDetect.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) {
            throw new RuntimeException("unknown code source");
        }
        File file;
        try {
            file = new File(codeSource.getLocation().toURI());
        } catch (Exception e) {
            throw new RuntimeException("code source is not a file", e);
        }
        List<String> command = Arrays.asList(
                javaExec,
                "-cp",
                file.getAbsolutePath(),
                CharsetDetect.class.getName()
        );
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("cannot start the process", e);
        }
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            Pattern pattern = Pattern.compile("^Charset: \"(.+)\"$");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String charset = matcher.group(1);
                    return Charset.forName(charset);
                }
            }
            throw new RuntimeException("no charset in process output");
        } finally {
            process.destroy();
        }
    }
}
