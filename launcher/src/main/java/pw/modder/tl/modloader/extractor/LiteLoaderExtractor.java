package pw.modder.tl.modloader.extractor;

public class LiteLoaderExtractor {
    private LiteLoaderExtractor() {
    }


//    public static CompleteVersion extract(File file) throws IOException {
//        ZipFile zipFile = new ZipFile(file);
//
//        ZipEntry profileEntry = zipFile.getEntry("install_profile.json");
//        if (profileEntry == null) throw new IOException(zipFile.getName() + ": cannot load install_profile.json");
//
//        InstallProfile profile = U.getGson().fromJson(
//                IOUtils.toString(zipFile.getInputStream(profileEntry), StandardCharsets.UTF_8),
//                InstallProfile.class
//        );
//
//        if (profile.getCompleteVersion() != null) {
//            return profile.getCompleteVersion();
//        }
//
//        throw new IOException("Cannot parse " + file.getName() + ", unknown format");
//    }
//
//    private static class InstallProfile {
//        // old manifest only
//        private ForgeExtractor.Install install;
//        private CompleteVersion versionInfo;
//
//        public ForgeExtractor.Install getInstall() {
//            return install;
//        }
//
//        public CompleteVersion getCompleteVersion() {
//            return versionInfo;
//        }
//    }
//    // Json inside installer jar
}
