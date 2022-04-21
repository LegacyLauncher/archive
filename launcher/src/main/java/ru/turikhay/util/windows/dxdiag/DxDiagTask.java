package ru.turikhay.util.windows.dxdiag;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * Основной таск, отвечающий за вызов процесса DxDiag и парсинга его результатов
 */
class DxDiagTask implements Callable<DxDiagReport> {
    private static final Logger LOGGER = LogManager.getLogger(DxDiagTask.class);

    private File reportFile; // if null, will be created later

    public DxDiagTask(String osName, String osVersion,
                      File reportFile) {
        this.reportFile = reportFile;
    }

    public DxDiagTask() {
        this(OS.NAME, OS.VERSION, null);
    }

    @Override
    public DxDiagReport call() throws Exception {
        DxDiagReport report;
        try {
            List<String> command = buildCommand();
            startAndWait(command);
            report = parseReportFile();
        } catch (InterruptedException interruptedException) {
            throw interruptedException;
        } catch (Exception e) {
            LOGGER.error("DxDiag report failed", e);
            printReportFileContent();
            throw e;
        }
        LOGGER.debug("DxDiag report is complete");
        return report;
    }

    private File getOrCreateOutputFile() throws DxDiagFailedException {
        if (reportFile == null) {
            reportFile = createTempDxDiagFile();
        }
        return reportFile;
    }

    File getReportFile() {
        if (reportFile == null) {
            throw new NullPointerException("outputFile");
        }
        return reportFile;
    }

    File getNullableReportFile() {
        return reportFile;
    }

    void printReportFileContent() {
        if (!LOGGER.isDebugEnabled()) {
            return;
        }
        File outputFile = getNullableReportFile();
        if (outputFile == null) {
            LOGGER.debug("Report file was not yet created");
            return;
        }
        LOGGER.debug("DxDiag report file content: {}", outputFile);
        LOGGER.debug("++++++++++++++++++++");
        try (Scanner scanner = new Scanner(outputFile)) {
            while (scanner.hasNextLine()) {
                LOGGER.debug(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            LOGGER.debug("Oops!", e);
        } finally {
            LOGGER.debug("++++++++++++++++++++");
        }
    }

    List<String> buildCommand() throws DxDiagFailedException {
        List<String> command = new ArrayList<>();
        command.add("cmd.exe");
        command.add("/c");
        command.add(getDxDiagExecutable().getAbsolutePath());
        command.add("/whql:off");

        boolean dontSkip;

        try {
            dontSkip = Double.parseDouble(OS.VERSION) >= 7.0;
        } catch (RuntimeException rE) {
            LOGGER.warn("Could not determine Windows version: {}", OS.VERSION);
            dontSkip = !OS.NAME.toLowerCase(java.util.Locale.ROOT).contains("xp");
        }

        if (dontSkip) {
            command.add("/dontskip");
        }

        command.add("/x");
        command.add(getOrCreateOutputFile().getAbsolutePath());

        return command;
    }

    void startAndWait(List<String> command) throws InterruptedException, DxDiagFailedException {
        LOGGER.debug("Executing DxDiag command: {}", command);

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process;

        try {
            process = processBuilder.start();
        } catch (IOException ioE) {
            throw new DxDiagFailedException("cannot start process", ioE);
        }

        long start = System.currentTimeMillis(), millis;
        do {
            millis = System.currentTimeMillis() - start;
            if ((millis / 1000L) % 5L == 0) {
                LOGGER.debug("Waiting DxDiag to gather info about the system... ({} s)", millis / 1000L);
            }
            //noinspection BusyWait
            Thread.sleep(1000);
        } while (process.isAlive());

        int exitCode = process.exitValue();
        LOGGER.debug("Done in {} ms with exit code {} (0x{})", millis, exitCode, Integer.toHexString(exitCode));

        if (exitCode != 0) {
            throw new DxDiagFailedException("exit code: " + exitCode + " (0x" + Integer.toHexString(exitCode) + ")");
        }
    }

    DxDiagReport parseReportFile() throws DxDiagFailedException {
        SAXBuilder jdomBuilder = new SAXBuilder();
        Document jdomDocument;
        try {
            jdomDocument = jdomBuilder.build(getReportFile());
        } catch (JDOMException | IOException e) {
            throw new DxDiagFailedException("cannot parse report file", e);
        }
        Element root = jdomDocument.getRootElement();
        SysInfo sysInfo = new SysInfo(new Section(root, "SystemInformation"));
        List<DisplayDevice> displayDevices = new ArrayList<>();
        Element displayDevicesElem = root.getChild("DisplayDevices");
        if (displayDevicesElem == null || root.getChild("DisplayDevices") == null) {
            LOGGER.debug("No display devices list");
        } else {
            List<Element> dd = root.getChild("DisplayDevices").getChildren("DisplayDevice");
            for (Element elem : dd) {
                displayDevices.add(new DisplayDevice(new Section(elem)));
            }
        }
        return new DxDiagReport(sysInfo, displayDevices);
    }

    private static File createTempDxDiagFile() throws DxDiagFailedException {
        File outputFile;
        try {
            outputFile = File.createTempFile("tlauncher-dxdiag", ".xml");
        } catch (IOException e) {
            throw new DxDiagFailedException("couldn't create temp file", e);
        }
        outputFile.deleteOnExit();
        return outputFile;
    }

    private static File getDxDiagExecutable() {
        return new File(System.getenv("WINDIR") + "\\system32\\dxdiag.exe");
    }

    public static class Factory implements TaskFactory {
        @Override
        public DxDiagTask createTask() {
            return new DxDiagTask();
        }
    }
}
