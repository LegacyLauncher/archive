package ru.turikhay.util.windows;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.exceptions.UnsupportedEnvirnomentException;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DxDiag {
    private static final long PROCESS_TIMEOUT = 90 * 1000;

    private final List<DisplayDevice> displayDevices = new ArrayList<DisplayDevice>(), _displayDevices = Collections.unmodifiableList(displayDevices);
    private SystemInformation sysInfo;

    private DxDiag() {
    }

    public List<DisplayDevice> getDisplayDevices() {
        return _displayDevices;
    }

    public DisplayDevice getDisplayDevice(String name) {
        name = name.toLowerCase();

        for (DisplayDevice device : displayDevices) {
            if (device.getCardName() == null) {
                continue;
            }
            if (device.getCardName().toLowerCase().contains(name)) {
                return device;
            }
        }

        return null;
    }

    public SystemInformation getSystemInfo() {
        return sysInfo;
    }

    public void flushToLogger() {
        synchronized (U.lock) {
            log("+++++++++++++");
            log(this);
            log("+++++++++++++");
        }
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sysInfo", sysInfo)
                .append("displayDevices", displayDevices)
                .build();
    }

    private class Section {
        protected final Element elem;

        private Section(Element elem, String name) {
            StringUtil.requireNotBlank(name, "name");
            this.elem = elem;

            if (elem == null) {
                log("[WARN]", name, "is null");
            }
        }

        protected String get(String childName) {
            if (elem == null) {
                return null;
            }

            Element child = elem.getChild(childName);
            if (child == null) {
                //log("Could not find", childName, "in", this);
                return null;
            }
            return child.getValue();
        }

        protected ToStringBuilder toStringBuilder() {
            return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        }

        public final String toString() {
            return toStringBuilder().build();
        }
    }

    public final class SystemInformation extends Section {
        private final String os, model, lang, cpu, ram, dxVersion;

        private SystemInformation(Element elem) {
            super(elem, "SystemInformation");
            os = get("OperatingSystem");
            model = get("SystemManufacturer") + " " + get("SystemModel");
            lang = get("Language");
            cpu = get("Processor");
            ram = get("Memory");
            dxVersion = get("DirectXVersion");
        }

        public boolean is64Bit() {
            return os != null && os.contains("64-bit");
        }

        public ToStringBuilder toStringBuilder() {
            return super.toStringBuilder()
                    .append("os", os)
                    .append("model", model)
                    .append("lang", lang)
                    .append("cpu", cpu)
                    .append("ram", ram)
                    .append("dxVersion", dxVersion);
        }
    }

    public final class DisplayDevice extends Section {
        private final String cardName, manufacturer, dacType, type, key, driverVersion, driverModel, driverDate, hybridGraphics;

        private DisplayDevice(Element elem) {
            super(elem, "DisplayDevice");

            cardName = get("CardName");
            manufacturer = get("Manufacturer");
            dacType = get("DACType");
            type = get("DeviceType");
            key = get("DeviceKey");
            driverVersion = get("DriverVersion");
            driverModel = get("DriverModel");
            driverDate = get("DriverDate");
            hybridGraphics = get("HybridGraphicsGPUType");
        }

        public String getCardName() {
            return cardName;
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public ToStringBuilder toStringBuilder() {
            return super.toStringBuilder()
                    .append("cardName", cardName)
                    .append("manufacturer", manufacturer)
                    .append("dacType", dacType)
                    .append("type", type)
                    .append("key", key)
                    .append("driverVersion", driverVersion)
                    .append("driverModel", driverModel)
                    .append("driverDate", driverDate)
                    .append("hybridGraphics", hybridGraphics);
        }
    }

    private static class Parser {
        static DxDiag parse(InputStream input) throws IOException, JDOMException {
            DxDiag dx = new DxDiag();

            SAXBuilder jdomBuilder = new SAXBuilder();
            Document jdomDocument = jdomBuilder.build(input);

            Element root = jdomDocument.getRootElement();

            dx.sysInfo = dx.new SystemInformation(root.getChild("SystemInformation"));

            Element displayDevices = root.getChild("DisplayDevices");
            if (displayDevices == null || root.getChild("DisplayDevices") == null) {
                log("Could not find display device list");
            } else {
                List<Element> dd = root.getChild("DisplayDevices").getChildren("DisplayDevice");
                for (Element elem : dd) {
                    dx.displayDevices.add(dx.new DisplayDevice(elem));
                }
            }
            return dx;
        }

        private static void log(Object... o) {
            U.log("[DxDiag][Parser]", o);
        }
    }

    private static DxDiagFailedException error;
    private static Worker worker;
    private static DxDiag result;

    static class Worker extends ExtendedThread {

        @Override
        public void run() {
            error = null;
            try {
                result = Parser.parse(new FileInputStream(retrieve()));
                result.flushToLogger();
            } catch (InterruptedException inE) {
                log("Worker interrupted.", inE);
            } catch (DxDiagFailedException dxfE) {
                error = dxfE;
            } catch (Exception e) {
                error = new DxDiagFailedException(e);
            } finally {
                if (worker == this) {
                    worker = null;
                }
            }
        }

        private long startTime = System.currentTimeMillis();
        boolean cancelled = true;

        void cancel() {
            log("Interrupting...");
            interrupt();
        }

        File retrieve() throws Exception {
            Time.start();

            File outputFile = File.createTempFile("tlauncher-dxdiag", null);
            outputFile.deleteOnExit();

            File dxDiagExe = new File(System.getenv("WINDIR") + "\\system32\\dxdiag.exe");

            if (!dxDiagExe.isFile()) {
                throw new FileNotFoundException(dxDiagExe.getAbsolutePath());
            }

            ArrayList<String> commands = new ArrayList<String>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add(dxDiagExe.getAbsolutePath());
            commands.add("/whql:off");

            boolean dontSkip;

            try {
                dontSkip = Double.parseDouble(OS.VERSION) >= 7.0;
            } catch (RuntimeException rE) {
                log("Could not determine Windows version:", OS.VERSION);
                dontSkip = !OS.NAME.toLowerCase().contains("xp");
            }

            if (dontSkip) {
                commands.add("/dontskip");
            }

            commands.add("/x");
            commands.add(outputFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            final Process process = processBuilder.start();

            long lastTime = startTime;
            while (true) {
                lastTime = System.currentTimeMillis();
                log("waiting for the result...", (lastTime - startTime));

                try {
                    process.exitValue();
                } catch (IllegalThreadStateException t) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException interrupted) {
                        log("waiters were released by interruption. resuming...");
                    }
                    continue;
                }

                break;
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new DxDiagFailedException("invalid exit code: " + exitCode);
            }

            return outputFile;
        }

        private void log(Object... o) {
            U.log("[DxDiag][Retriever]", o);
        }
    }

    public static DxDiag get() throws DxDiagFailedException {
        UnsupportedEnvirnomentException.ensureUnder(OS.WINDOWS);

        if (result == null && worker == null) {
            (worker = new Worker()).start();
        }

        if (worker != null) {
            long remainingTimeout = (worker.startTime + PROCESS_TIMEOUT) - System.currentTimeMillis();

            if (remainingTimeout > 0) {
                try {
                    worker.join(remainingTimeout);
                } catch (InterruptedException e) {
                    throw new DxDiagFailedException(e);
                }
            }
        }

        if (error != null) {
            throw error;
        }

        if (result == null) {
            throw new DxDiagFailedException("no result");
        }

        return result;
    }

    public static void cancel() {
        log("Interrupting worker:", new RuntimeException("interrupt trace"));
        if (worker != null) {
            worker.interrupt();
        }
    }

    public static boolean isScannable() {
        return OS.WINDOWS.isCurrent() && (TLauncher.getInstance() == null || TLauncher.getInstance().getSettings().getBoolean("windows.dxdiag"));
    }

    private static void log(Object... o) {
        U.log("[DxDiag]", o);
    }

    public static class DxDiagFailedException extends Exception {
        private DxDiagFailedException(String message) {
            super(message);
        }

        private DxDiagFailedException(Throwable cause) {
            super(cause);
        }
    }

    public static class DxDiagTimeoutException extends DxDiagFailedException {
        private DxDiagTimeoutException() {
            super("timeout");
        }
    }
}
