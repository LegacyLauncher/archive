package net.minecraft.launcher.updater;

import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.RetryDownloadException;
import ru.turikhay.tlauncher.managers.AssetsManager;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.io.File;
import java.io.IOException;

public class AssetDownloadable extends Downloadable {

    private final AssetIndex.AssetObject asset;
    private final File folder;

    public AssetDownloadable(AssetIndex.AssetObject obj, File objectsFolder) {
        asset = obj;
        folder = objectsFolder;

        String path = AssetIndex.getPath(asset.hash());

        setURL(Repository.ASSETS_REPO, path);
        setDestination(new File(folder, path));

        if (asset.isCompressed()) {
            prefix = "[Asset_c:" + asset.getCompressedHash() + "]";
        } else {
            prefix = "[Asset:" + asset.getHash() + "]";
        }

        //log("URL:", getURL(), "; destination:", getDestination());
    }

    public void onComplete() throws RetryDownloadException {
        String expectHash = asset.hash(), gotHash = FileUtil.getDigest(getDestination(), "SHA", 40);

        if (expectHash.equals(gotHash)) {
            //log("Hash is correct:", gotHash);
            if (asset.isCompressed()) {
                log("Decompressing...");
                try {
                    AssetsManager.decompress(getDestination(), new File(folder, asset.getHash()), asset.getHash());
                } catch (RetryDownloadException rdE) {
                    throw rdE;
                } catch (IOException ioE) {
                    throw new RetryDownloadException("could not decompress " + gotHash, ioE);
                }
            }
        } else {
            log("Invalid hash:", gotHash, "; expected:", expectHash);
            throw new RetryDownloadException(gotHash + ';' + expectHash);
        }
    }

    private final String prefix;

    private void log(Object... o) {
        U.log(prefix, o);
    }
}
