package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import net.minecraft.launcher.updater.AssetDownloadable;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.component.ComponentDependence;
import ru.turikhay.tlauncher.component.LauncherComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableContainer;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.repository.RepositoryProxy;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

@ComponentDependence({VersionManager.class, VersionLists.class})
public class AssetsManager extends LauncherComponent {
    private static final Logger LOGGER = LogManager.getLogger(AssetsManager.class);

    private final Gson gson = U.getGson();
    private final Object assetsFlushLock = new Object();

    public AssetsManager(ComponentManager manager) {
        super(manager);
    }

    public DownloadableContainer downloadResources(CompleteVersion version, List<AssetIndex.AssetObject> list) {
        File baseDirectory = manager.getLauncher().getVersionManager().getLocalList().getBaseDirectory();
        DownloadableContainer container = new DownloadableContainer();
        container.addAll(getResourceFiles(version, baseDirectory, list));
        return container;
    }

    private Set<Downloadable> getResourceFiles(CompleteVersion version, File baseDirectory, List<AssetIndex.AssetObject> list) {
        File objectsFolder = new File(baseDirectory, "assets/objects");
        Set<Downloadable> result = new HashSet<>();

        for (AssetIndex.AssetObject object : list) {

            File destination = new File(objectsFolder, object.getHash());
            if (destination.isFile()) {
                String hash = FileUtil.getDigest(destination, "SHA", 40);
                if (object.getHash().equals(hash)) {
                    //log("Skipped existing asset:", hash);
                    continue;
                }
            }

            result.add(new AssetDownloadable(object, objectsFolder));
        }

        return result;
    }

    List<AssetIndex.AssetObject> getResourceFiles(CompleteVersion version, File baseDirectory, boolean local) {
        List<AssetIndex.AssetObject> list = null;
        if (!local) {
            try {
                list = getRemoteResourceFilesList(version, baseDirectory, true);
            } catch (Exception var7) {
                LOGGER.error("Cannot get remote assets list. Trying to use the local one.", var7);
            }
        }

        if (list == null) {
            list = getLocalResourceFilesList(version, baseDirectory);
        }

        if (list == null) {
            try {
                list = getRemoteResourceFilesList(version, baseDirectory, true);
            } catch (Exception var6) {
                LOGGER.error("Gave up trying to get assets list", var6);
            }
        }

        return list;
    }

    private List<AssetIndex.AssetObject> getLocalResourceFilesList(CompleteVersion version, File baseDirectory) {
        String indexName = version.getAssetIndex().getId();
        File indexesFolder = new File(baseDirectory, "assets/indexes/");
        File indexFile = new File(indexesFolder, indexName + ".json");
        LOGGER.debug("Reading indexes from file {}", indexFile);

        AssetIndex index;
        try (Reader reader = new FileReader(indexFile)) {
            AssetIndex obj = gson.fromJson(reader, AssetIndex.class);
            index = Objects.requireNonNull(obj);
        } catch (Exception e) {
            LOGGER.error("could not read index file {}", indexFile, e);
            return null;
        }

        return new ArrayList<>(index.getUniqueObjects());
    }

    private List<AssetIndex.AssetObject> getRemoteResourceFilesList(CompleteVersion version, File baseDirectory, boolean save) throws IOException {

        String indexName = version.getAssetIndex().getId();
        if (indexName == null) {
            indexName = "legacy";
        }

        File assets = new File(baseDirectory, "assets");
        File indexesFolder = new File(assets, "indexes");
        File indexFile = new File(indexesFolder, indexName + ".json");

        Reader json;

        if (StringUtils.isBlank(version.getAssetIndex().getUrl())) {
            LOGGER.debug("Reading from repository...");
            json = Repository.OFFICIAL_VERSION_REPO.read("indexes/" + indexName + ".json");
        } else {
            LOGGER.debug("Reading from index: {}", version.getAssetIndex().getUrl());
            json = new StringReader(RepositoryProxy.requestMaybeProxy(version.getAssetIndex().getUrl()));
        }

        File tempIndexFile = null;

        if (save) {
            tempIndexFile = File.createTempFile("tlauncher-assets", null);
            tempIndexFile.deleteOnExit();

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(tempIndexFile), StandardCharsets.UTF_8)) {
                IOUtils.copy(json, writer);
            }

            json = new FileReader(tempIndexFile);
        }

        AssetIndex index = gson.fromJson(json, AssetIndex.class);
        ArrayList<AssetIndex.AssetObject> result = new ArrayList<>(index.getUniqueObjects());

        if (save) {
            synchronized (assetsFlushLock) {
                try (InputStream in = new FileInputStream(tempIndexFile);
                     OutputStream out = new FileOutputStream(indexFile)) {
                    IOUtils.copy(in, out);
                } finally {
                    tempIndexFile.delete();
                }
            }
            LOGGER.debug("Assets index has been saved into file: {}", indexFile);
        }

        return result;
    }

    public ResourceChecker checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast) throws AssetsNotFoundException {
        LOGGER.debug("Checking resources...");

        List<AssetIndex.AssetObject> list;
        if (local) {
            list = getLocalResourceFilesList(version, baseDirectory);
        } else {
            list = getResourceFiles(version, baseDirectory, false);
        }

        if (list == null) {
            LOGGER.warn("Cannot check resources, because assets list is unavailable");
            throw new AssetsNotFoundException();
        } else {
            return new ResourceChecker(baseDirectory, list, fast);
        }
    }

    public ResourceChecker checkResources(CompleteVersion version, boolean fast) throws AssetsNotFoundException {
        return checkResources(version, manager.getComponent(VersionLists.class).getLocal().getBaseDirectory(), false, fast);
    }

    private static boolean checkResource(File baseDirectory, AssetIndex.AssetObject local, boolean fast) {
        File assetFile = new File(baseDirectory, "assets/objects/" + AssetIndex.getPath(local.getHash()));

        if (fast) {
            return checkFile(assetFile, local.size());
        } else {
            if (checkFile(assetFile, local.size(), local.getHash())) {
                return true;
            }
            if (!local.isCompressed()) {
                return false;
            }
        }

        FileUtil.deleteFile(assetFile);

        File compressedAssetFile = new File(baseDirectory, "assets/objects/" + AssetIndex.getPath(local.getCompressedHash()));
        if (checkFile(assetFile, local.getCompressedSize(), local.getCompressedHash())) {
            try {
                decompress(compressedAssetFile, assetFile, local.getHash());
            } catch (IOException ioE) {
                LOGGER.error("Could not decompress assets", ioE);
                return false;
            }
            return true;
        }

        return false;
    }

    private static boolean checkFile(File assetFile, long size) {
        return assetFile.isFile() && assetFile.length() == size;
    }

    private static boolean checkFile(File assetFile, long size, String hash) {
        return checkFile(assetFile, size) && hash.equals(AssetIndex.getHash(assetFile));
    }

    public static void decompress(File compressedInput, File uncompressedOutput, String expectHash) throws IOException {
        String hash;

        try (InputStream in = new GZIPInputStream(new FileInputStream(compressedInput));
             OutputStream out = new FileOutputStream(uncompressedOutput)) {
            hash = FileUtil.copyAndDigest(in, out, "SHA", 40);
        }

        if (!expectHash.equals(hash)) {
            throw new IOException("could not decompress asset got: " + hash + ", expected: " + expectHash);
        }
    }

    public static final class ResourceChecker extends ExtendedThread {
        final File baseDirectory;
        final boolean fast;
        final List<AssetIndex.AssetObject> objectList;

        ResourceChecker(File baseDirectory, List<AssetIndex.AssetObject> objectList, boolean fast) {
            this.baseDirectory = baseDirectory;
            this.objectList = objectList;
            this.fast = fast;

            startAndWait();
            unlockThread("start");
        }

        private volatile List<AssetIndex.AssetObject> result;
        private volatile AssetIndex.AssetObject current;
        private long delta;

        private Exception e;

        public boolean checkWorking() throws InterruptedException {
            if (isInterrupted()) {
                throw new InterruptedException();
            }
            return result == null && isAlive();
        }

        public AssetIndex.AssetObject getCurrent() {
            return current;
        }

        public List<AssetIndex.AssetObject> getAssetList() {
            return result;
        }

        public long getDelta() {
            return delta;
        }

        public Exception getError() {
            return e;
        }

        @Override
        public void run() {
            checkCurrent();
            lockThread("start");

            try {
                check();
            } catch (Exception e) {
                this.e = e;
            }
        }

        private void check() {
            LOGGER.info("Executing {} assets comparison", fast ? "fast" : "deep");

            List<AssetIndex.AssetObject> result = new ArrayList<>();
            Time.start();

            for (AssetIndex.AssetObject object : objectList) {
                current = object;

                if (checkResource(baseDirectory, object, fast)) {
                    continue;
                }

                if (Thread.interrupted()) {
                    throw new RuntimeException("interrupted");
                }

                result.add(object);
            }
            current = null;
            delta = Time.stop();

            this.result = result;
        }
    }
}
