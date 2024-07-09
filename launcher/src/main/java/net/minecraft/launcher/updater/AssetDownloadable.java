package net.minecraft.launcher.updater;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.RetryDownloadException;
import net.legacylauncher.managers.AssetsManager;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.util.FileUtil;

import java.io.File;
import java.io.IOException;

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

    public void onComplete() throws RetryDownloadException {
        String expectHash = asset.hash(), gotHash = FileUtil.getDigest(getDestination(), "SHA", 40);

        if (expectHash.equals(gotHash)) {
            if (asset.isCompressed()) {
                try {
                    AssetsManager.decompress(getDestination(), new File(folder, asset.getHash()), asset.getHash());
                } catch (RetryDownloadException rdE) {
                    throw rdE;
                } catch (IOException ioE) {
                    throw new RetryDownloadException("could not decompress " + gotHash, ioE);
                }
            }
        } else {
            log.error("Invalid hash: {}; expected: {}", gotHash, expectHash);
            throw new RetryDownloadException(gotHash + ';' + expectHash);
        }
    }
}
