package net.legacylauncher.managers;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.common.exceptions.LocalIOException;
import net.legacylauncher.component.ComponentDependence;
import net.legacylauncher.component.LauncherComponent;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.DownloadableContainer;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.repository.RepositoryProxy;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.Time;
import net.legacylauncher.util.U;
import net.legacylauncher.util.async.ExtendedThread;
import net.minecraft.launcher.updater.AssetDownloadable;
import net.minecraft.launcher.updater.AssetIndex;
import net.minecraft.launcher.versions.CompleteVersion;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Slf4j
@ComponentDependence({VersionManager.class, VersionLists.class})
public class AssetsManager extends LauncherComponent {
    private final Gson gson = U.getGson();
    private final Object assetsFlushLock = new Object();

    public AssetsManager(ComponentManager manager) {
        super(manager);
    }

    public DownloadableContainer collectAssets(CompleteVersion version, List<AssetIndex.AssetObject> list) throws LocalIOException {
        File baseDirectory = manager.getLauncher().getVersionManager().getLocalList().getBaseDirectory();
        DownloadableContainer container = new DownloadableContainer();
        container.addAll(getResourceFiles(version, baseDirectory, list));
        return container;
    }

    private Set<Downloadable> getResourceFiles(CompleteVersion version, File baseDirectory, List<AssetIndex.AssetObject> list) throws LocalIOException {
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
                log.error("Cannot get remote assets list. Trying to use the local one.", var7);
            }
        }

        if (list == null) {
            list = getLocalResourceFilesList(version, baseDirectory);
        }

        if (list == null) {
            try {
                list = getRemoteResourceFilesList(version, baseDirectory, true);
            } catch (Exception var6) {
                log.error("Gave up trying to get assets list", var6);
            }
        }

        return list;
    }

    private List<AssetIndex.AssetObject> getLocalResourceFilesList(CompleteVersion version, File baseDirectory) {
        String indexName = version.getAssetIndex().getId();
        File indexesFolder = new File(baseDirectory, "assets/indexes/");
        File indexFile = new File(indexesFolder, indexName + ".json");
        log.debug("Reading indexes from file {}", indexFile);

        AssetIndex index;
        try (Reader reader = new FileReader(indexFile)) {
            AssetIndex obj = gson.fromJson(reader, AssetIndex.class);
            index = Objects.requireNonNull(obj);
        } catch (Exception e) {
            log.error("could not read index file {}", indexFile, e);
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
            log.debug("Reading from repository...");
            json = Repository.OFFICIAL_VERSION_REPO.read("indexes/" + indexName + ".json");
        } else {
            log.debug("Reading from index: {}", version.getAssetIndex().getUrl());
            json = new StringReader(RepositoryProxy.requestMaybeProxy(version.getAssetIndex().getUrl()));
        }

        File tempIndexFile = null;

        if (save) {
            tempIndexFile = File.createTempFile("tlauncher-assets", null);
            tempIndexFile.deleteOnExit();

            try (Writer writer = new OutputStreamWriter(Files.newOutputStream(tempIndexFile.toPath()), StandardCharsets.UTF_8)) {
                IOUtils.copy(json, writer);
            }

            json = new FileReader(tempIndexFile);
        }

        AssetIndex index = gson.fromJson(json, AssetIndex.class);
        ArrayList<AssetIndex.AssetObject> result = new ArrayList<>(index.getUniqueObjects());

        if (save) {
            synchronized (assetsFlushLock) {
                try (InputStream in = Files.newInputStream(tempIndexFile.toPath());
                     OutputStream out = Files.newOutputStream(indexFile.toPath())) {
                    IOUtils.copy(in, out);
                } finally {
                    tempIndexFile.delete();
                }
            }
            log.debug("Assets index has been saved into file: {}", indexFile);
        }

        return result;
    }

    public ResourceChecker checkResources(CompleteVersion version, File baseDirectory, boolean local, boolean fast) throws AssetsNotFoundException {
        log.debug("Checking resources...");

        List<AssetIndex.AssetObject> list;
        if (local) {
            list = getLocalResourceFilesList(version, baseDirectory);
        } else {
            list = getResourceFiles(version, baseDirectory, false);
        }

        if (list == null) {
            log.warn("Cannot check resources, because assets list is unavailable");
            throw new AssetsNotFoundException();
        } else {
            return new ResourceChecker(baseDirectory, list, fast);
        }
    }

    public ResourceChecker checkResources(CompleteVersion version, boolean fast) throws AssetsNotFoundException {
        return checkResources(version, manager.getComponent(VersionLists.class).getLocal().getBaseDirectory(), false, fast);
    }

    private static boolean checkResource(File baseDirectory, AssetIndex.AssetObject local, boolean fast) throws IOException {
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
            decompress(compressedAssetFile, assetFile, local.getHash());
            return true;
        }

        return false;
    }

    private static boolean checkFile(File assetFile, long size) {
        return assetFile.isFile() && assetFile.length() == size;
    }

    private static boolean checkFile(File assetFile, long size, String hash) throws LocalIOException {
        return checkFile(assetFile, size) && hash.equals(AssetIndex.getHash(assetFile));
    }

    public static void decompress(File compressedInput, File uncompressedOutput, String expectHash) throws IOException {
        String hash;

        try (InputStream in = new GZIPInputStream(Files.newInputStream(compressedInput.toPath()));
             OutputStream out = Files.newOutputStream(uncompressedOutput.toPath())) {
            hash = FileUtil.copyAndDigest(in, out, "SHA", 40, true);
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

        private void check() throws IOException {
            log.info("Executing {} assets comparison", fast ? "fast" : "deep");

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
