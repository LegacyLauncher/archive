package ru.turikhay.tlauncher.ui.frames.yandex;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class YandexInstaller {
    private final static String PARTNER_ID = "7958";

    private final Repository repo;
    private final String downloadUrl;
    private final String downloadHash;
    private final Worker worker;
    private List<YandexOffer> offerList;

    public YandexInstaller(Repository repository, String downloadUrl, String downloadHash) {
        this.repo = repository;
        this.downloadUrl = downloadUrl;
        this.downloadHash = downloadHash;
        this.worker = new Worker();
        worker.startAndWait();
        synchronized (YandexInstaller.class) {
            instance = this;
        }
        log("Initialized:", toString());
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getDownloadHash() {
        return downloadHash;
    }

    public void start(List<YandexOffer> offerList) {
        if(!worker.isThreadLocked()) {
            throw new IllegalStateException("already started");
        }
        if(!worker.isAlive()) {
            throw new IllegalStateException("already finished");
        }
        this.offerList = offerList;
        worker.unlockThread("start");
    }

    private boolean hasOffer(YandexOffer offer) {
        return offerList != null && offerList.indexOf(offer) != -1;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("repo", repo)
                .append("downloadUrl", downloadUrl)
                .append("downloadHash", downloadHash)
                .append("offerList", offerList)
                .append("worker", worker)
                .build();
    }

    private final List<Listener> listeners = Collections.synchronizedList(new ArrayList<YandexInstaller.Listener>());
    public void addListener(Listener listener) {
        listeners.add(listener);
    }
    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private static YandexInstaller instance;
    public synchronized static YandexInstaller getInstance() {
        return instance;
    }
    public synchronized static boolean hasInstance() {
        return instance != null;
    }

    public interface Listener {
        void onYandexStarted(YandexInstaller installer);
        void onYandexFailed(YandexInstaller installer, Throwable t);
        void onYandexInstalling(YandexInstaller installer);
    }

    private class Worker extends ExtendedThread implements Listener {
        @Override
        public void run() {
            log("Ready");
            lockThread("start");
            log("Offer list:", offerList);

            try {
                work();
            } catch(Exception e) {
                onYandexFailed(YandexInstaller.this, e);
            }
        }

        private void work() throws Exception {
            onYandexStarted(YandexInstaller.this);
            File installerFile = downloadInstaller();

            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command(installerFile.getAbsolutePath());

            List<String> args = getInstallerArguments();
            log("Args:", args);
            processBuilder.command().addAll(args);

            onYandexInstalling(YandexInstaller.this);
            Process process = processBuilder.start();
            log("Exit code: ", process.waitFor());

            FileUtil.deleteFile(installerFile);
        }

        private File downloadInstaller() throws IOException {
            File file = File.createTempFile("yandex", null);
            log("File", file);
            try {
                InputStream stream;
                if (repo == null) {
                    stream = U.makeURL(downloadUrl, true).openStream();
                } else {
                    stream = repo.get(downloadUrl);
                }
                downloadStream(stream, file);
            } catch(Exception e) {
                file.deleteOnExit();
                file.delete();
                throw e;
            }
            return file;
        }

        private void downloadStream(InputStream stream, File file) throws IOException {
            log("Downloading", stream);
            try(FileOutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(stream, out);
            }
            String checksum = Sha256.calc(file);
            if(!downloadHash.equals(checksum)) {
                throw new IOException("checksum doesn't match (got: " + checksum + ")");
            }
        }

        private void startInstaller(File file) {
        }

        // --partner <partner parameter> --distr /quiet /msicl "YABROWSER=y YAHOMEPAGE=y YAQSEARCH=y YABM=y"
        private List<String> getInstallerArguments() {
            ArrayList<String> args = new ArrayList<>();

            args.add("--partner");
            args.add(PARTNER_ID);

            if(offerList.isEmpty()) {
                args.add("--noaction");
                args.add("1");
                return args;
            }

            args.add("--distr");
            args.add("/quiet");

            ArrayList<String> parameters = new ArrayList<>();
            if(hasOffer(YandexOffer.BROWSER)) {
                parameters.add("YABROWSER=y");
            }
            if(hasOffer(YandexOffer.SHORTCUTS)) {
                parameters.add("YAHOMEPAGE=y");
                parameters.add("YAQSEARCH=y");
            }
            if(hasOffer(YandexOffer.BROWSER_MANAGER)) {
                parameters.add("YABM=y");
            }
            if(!hasOffer(YandexOffer.ADDONS)) {
                parameters.add("ILIGHT=1");
            }

            if(!parameters.isEmpty()) {
                args.add("/msicl");
                args.add("\"" + StringUtils.join(parameters, " ") + "\"");
            }

            return args;
        }

        private void log(Object...o) {
            U.log("[YandexInstaller:Worker]", o);
        }

        @Override
        public void onYandexStarted(YandexInstaller installer) {
            log("Started");
            for(YandexInstaller.Listener listener : listeners) {
                listener.onYandexStarted(installer);
            }
        }

        @Override
        public void onYandexFailed(YandexInstaller installer, Throwable t) {
            log("Failed", t);
            for(YandexInstaller.Listener listener : listeners) {
                listener.onYandexFailed(installer, t);
            }
        }

        @Override
        public void onYandexInstalling(YandexInstaller installer) {
            log("Installing");
            for(YandexInstaller.Listener listener : listeners) {
                listener.onYandexInstalling(installer);
            }
        }
    }

    private static class Sha256 {
        private static MessageDigest SHA256;
        static {
            try {
                SHA256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException nsaE) {
                throw new Error(nsaE);
            }
        }

        public static String toString(byte[] b) {
            return String.format("%064x", new java.math.BigInteger(1, b));
        }

        public static byte[] digest(byte[] b) {
            try {
                return digest(new ByteArrayInputStream(b));
            } catch (IOException e) {
                throw new Error("unexpected ioE", e);
            }
        }

        public static byte[] digest(InputStream in) throws IOException {
            final MessageDigest digest = SHA256;
            try {
                byte[] dataBytes = new byte[1024];
                int nread;
                while ((nread = in.read(dataBytes)) != -1) {
                    digest.update(dataBytes, 0, nread);
                }
                return digest.digest();
            } finally {
                digest.reset();
            }
        }

        public static String calc(File file) throws IOException {
            FileInputStream input = null;
            try {
                input = new FileInputStream(file);
                return toString(digest(input));
            } finally {
                U.close(input);
            }
        }
    }

    private void log(Object...o) {
        U.log("[YandexInstaller]", o);
    }
}
