package net.minecraft.launcher.updater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.managers.AssetsManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;

import java.io.File;
import java.io.IOException;

public class AssetDownloadable extends Downloadable {
    private static final Logger LOGGER = LogManager.getLogger();

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
            LOGGER.error("Invalid hash: {}; expected: {}", gotHash, expectHash);
            throw new RetryDownloadException(gotHash + ';' + expectHash);
        }
    }
}
