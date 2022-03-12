package ru.turikhay.tlauncher.repository;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RepoList {
    private static final Logger LOGGER = LogManager.getLogger(RepoList.class);

    private static final int FILE_BUFFER = 1024 * 8; // 8 kb
    private static final long GLOBAL_BUFFER_MAX = 1024 * 1024 * 10; // 10 mb
    private static final AtomicLong GLOBAL_BUFFER = new AtomicLong();

    private final String name;
    private final Object sync = new Object();

    private final ArrayList<IRepo> list = new ArrayList<>();
    private RelevantRepoList relevant = new RelevantRepoList();

    public RepoList(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("name");
        }

        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public final RelevantRepoList getRelevant() {
        return relevant;
    }

    public InputStream read(String path, Proxy proxy) throws IOException {
        IOException ex = null;
        List<IRepo> l = getRelevant().getList();
        int timeout = U.getConnectionTimeout();

        Object total = new Object();
        Time.start(total);

        LOGGER.debug("Fetching from {}: \"{}\", timeout: {}, proxy: {}", name, path, timeout / 1000, proxy);
        int attempt = 0;

        for (IRepo repo : l) {
            ++attempt;
            Object current = new Object();
            Time.start(current);

            String _path; // path to show in the logs
            if (repo instanceof AppenderRepo) {
                try {
                    _path = String.valueOf(((AppenderRepo) repo).makeUrl(path));
                } catch (IOException ioE) {
                    _path = "(failed to make url: \"" + path + "\")";
                }
            } else {
                _path = path;
            }

            try {
                InputStream result = read(connect(repo, path, timeout, proxy, attempt));
                long[] deltas = Time.stop(total, current);
                LOGGER.debug("Fetched successfully from {}: \"{}\": {} ms; total: {} ms, attempt: {}",
                        name, _path, deltas[1], deltas[0], attempt);
                return result;
            } catch (IOException ioE) {
                LOGGER.error("Failed to fetch from {}: \"{}\": attempt: {}, exception: {}",
                        name, _path, attempt, ioE.toString());
                if (ex == null) {
                    ex = ioE;
                } else {
                    ex.addSuppressed(ioE);
                }
            }
        }

        if (ex != null) {
            throw ex;
        } else {
            throw new IOException("Unable to fetch repo due to unknown reason");
        }
    }

    public final InputStream read(String path) throws IOException {
        return read(path, U.getProxy());
    }

    protected URLConnection connect(IRepo repo, String path, int timeout, Proxy proxy, int attempt) throws IOException {
        return repo.get(path, timeout, proxy);
    }

    protected InputStream read(URLConnection connection) throws IOException {
        int size = connection instanceof HttpURLConnection ? connection.getContentLength() : -1;
        InputStream in = connection.getInputStream();
        return read(in, size);
    }

    private InputStream read(InputStream in, int size) throws IOException {
        //log("Reading into buffer", in);
        try {
            return readIntoBuffer(in, size);
        } catch (BufferException repoException) {
            //log("Could not read into buffer", repoException);
        }

        //log("Reading into file", in);
        try {
            return readIntoFile(in);
        } catch (BufferException repoException) {
            LOGGER.error("Could not read into file from {}", name, repoException);
        }

        //log("Reading directly", in);
        return in;
    }

    private InputStream readIntoBuffer(InputStream in, final int size) throws BufferException, IOException {
        if (size < 1) {
            throw new BufferException("input too small");
        }

        if (GLOBAL_BUFFER.addAndGet(size) > GLOBAL_BUFFER_MAX) {
            throw new BufferException("buffer is full");
        }

        byte[] buffer = new byte[size];
        IOUtils.read(in, buffer);

        return new FilterInputStream(new ByteArrayInputStream(buffer)) {
            public void close() throws IOException {
                GLOBAL_BUFFER.addAndGet(-size);
                super.close();
            }
        };
    }

    private InputStream readIntoFile(InputStream in) throws BufferException, IOException {
        if (GLOBAL_BUFFER.addAndGet(FILE_BUFFER) > GLOBAL_BUFFER_MAX) {
            throw new BufferException("buffer is full");
        }

        final File temp = File.createTempFile("tlauncher-repo", null);
        temp.deleteOnExit();

        try (OutputStream out = new FileOutputStream(temp)) {
            IOUtils.copy(new BufferedInputStream(in, FILE_BUFFER), out);
        } finally {
            GLOBAL_BUFFER.addAndGet(-FILE_BUFFER);
        }

        return new FilterInputStream(new FileInputStream(temp)) {
            public void close() throws IOException {
                super.close();
                temp.delete();
            }
        };
    }

    protected void add(IRepo repo) {
        synchronized (sync) {
            if (list.contains(repo)) {
                throw new IllegalArgumentException("repo already added");
            }

            list.add(repo);
            relevant = makeRelevantRepoList();
        }
    }

    public void markInvalid(IRepo repo) {
        synchronized (sync) {
            int index = list.indexOf(repo);
            if (index == -1 || index == list.size() - 1) {
                return;
            }
            list.add(repo);
            list.remove(repo);

            relevant = makeRelevantRepoList();
        }
    }

    public class RelevantRepoList {
        private final List<IRepo> repoList;

        protected RelevantRepoList() {
            synchronized (sync) {
                repoList = Collections.unmodifiableList(new ArrayList<>(list));
            }
        }

        public List<IRepo> getList() {
            return repoList;
        }

        public IRepo getFirst() {
            return repoList.isEmpty() ? null : repoList.get(0);
        }
    }

    protected RelevantRepoList makeRelevantRepoList() {
        return new RelevantRepoList();
    }

    private static class BufferException extends Exception {
        BufferException(String description) {
            super(description);
        }
    }
}