package net.minecraft.launcher.updater;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.common.exceptions.LocalIOException;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.RetryDownloadException;
import net.legacylauncher.managers.AssetsManager;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Slf4j
public class AssetDownloadable extends Downloadable {
    private final AssetIndex.AssetObject asset;
    private final File folder;

    public AssetDownloadable(AssetIndex.AssetObject obj, File objectsFolder) {
        asset = obj;
        folder = objectsFolder;

        String path = AssetIndex.getPath(asset.hash());

        setURL(Repository.ASSETS_REPO, path);
        setDestination(new File(folder, path));

        //log("URL:", getURL(), "; destination:", getDestination());
    }

    public void onComplete() throws IOException {
        String expectHash = asset.hash(), gotHash = FileUtil.getDigest(getDestination(), "SHA", 40);

        if (!expectHash.equals(gotHash)) {
            log.error("Invalid hash: {}; expected: {}", gotHash, expectHash);
            throw new RetryDownloadException(gotHash + ';' + expectHash);
        }

        if (asset.isCompressed()) {
            File output = new File(folder, asset.getHash());
            try {
                AssetsManager.decompress(getDestination(), output, asset.getHash());
            } catch (RetryDownloadException rdE) {
                throw rdE;
            } catch (IOException ioE) {
                throw new LocalIOException(String.format(Locale.ROOT,
                        "%s -> %s",
                        getDestination().getAbsolutePath(),
                        output
                ), ioE);
            }
        }
    }
}
