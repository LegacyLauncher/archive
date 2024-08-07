package net.legacylauncher.bootstrap.ssl;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class FixSSL {
    private static boolean IS_FIXED = false;

    public static boolean isFixed() {
        return IS_FIXED;
    }

    public static void addLetsEncryptCertSupportIfNeeded() {
        try {
            addLetsEncryptIntoTrustStore();
        } catch (Exception e) {
            log.error("Couldn't add LetsEncrypt root certificate", e);
        }
    }

    private static boolean doesContainLetsEncryptRootCert(Map<String, Certificate> jreTrustStore,
                                                          Map<String, Certificate> letsEncryptStore) {
        for (Certificate letsEncryptCert : letsEncryptStore.values()) {
            if (!jreTrustStore.containsValue(letsEncryptCert)) {
                log.info("JRE trust store doesn't contain {}", letsEncryptCert);
                return false;
            }
        }
        return true;
    }

    private static void addLetsEncryptIntoTrustStore() throws Exception {
        Map<String, Certificate> jreTrustStore = loadJreTrustStore();
        Map<String, Certificate> letsEncryptStore = loadLetsEncryptStore();
        if (doesContainLetsEncryptRootCert(jreTrustStore, letsEncryptStore)) {
            return;
        }
        KeyStore mergedStore = mergeStores(jreTrustStore, letsEncryptStore);
        useNewKeyStoreGlobally(mergedStore);
        IS_FIXED = true;
    }

    private static Map<String, Certificate> loadJreTrustStore() throws Exception {
        File cacertsFile = new File(System.getProperty("java.home"), "lib/security/cacerts");
        return loadStore(Files.newInputStream(cacertsFile.toPath()), "changeit");
    }

    private static Map<String, Certificate> loadLetsEncryptStore() throws Exception {
        return loadStore(FixSSL.class.getResourceAsStream("lekeystore.jks"), "supersecretpassword");
    }

    private static KeyStore mergeStores(Map<String, Certificate> store0, Map<String, Certificate> store1) throws Exception {
        KeyStore mergedKeyStore = newKeyStore();
        mergedKeyStore.load(null, new char[0]);
        for (Map.Entry<String, Certificate> entry : store0.entrySet()) {
            mergedKeyStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Certificate> entry : store1.entrySet()) {
            mergedKeyStore.setCertificateEntry(entry.getKey(), entry.getValue());
        }
        return mergedKeyStore;
    }

    private static void useNewKeyStoreGlobally(KeyStore keyStore) throws Exception {
        log.info("Adding LetsEncrypt into trust store");
        TrustManagerFactory instance = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        instance.init(keyStore);
        final SSLContext tls = SSLContext.getInstance("TLS");
        tls.init(null, instance.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(tls.getSocketFactory());
    }

    private static Map<String, Certificate> loadStore(InputStream input, String password) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        loadCAStore(keyStore, input, password);
        return readTrustStore(keyStore);
    }

    private static Map<String, Certificate> readTrustStore(KeyStore keyStore) throws KeyStoreException {
        Map<String, Certificate> result = new HashMap<>();
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate certificate = keyStore.getCertificate(alias);
            result.put(alias, certificate);
        }
        return result;
    }

    private static void loadCAStore(KeyStore keyStore, InputStream input, String password)
            throws IOException, CertificateException, NoSuchAlgorithmException {
        try {
            keyStore.load(input, password.toCharArray());
        } finally {
            input.close();
        }
    }

    private static KeyStore newKeyStore() throws KeyStoreException {
        return KeyStore.getInstance(KeyStore.getDefaultType());
    }

    private FixSSL() {
    }
}
