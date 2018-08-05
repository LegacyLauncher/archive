package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.managers.McleaksStatus;
import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

class McleaksStatusThread extends ExtendedThread {
    private final McleaksStatus parent;

    McleaksStatusThread(McleaksStatus status) {
        this.parent = status;
    }

    @Override
    public void run() {
        checkCurrent();

        try {
            receivePayload();
        } catch(Exception e) {
            Sentry.sendError(McleaksStatusThread.class, "cannot receive payload", e, null);
            parent.receiveNothing();
        }
    }

    private void receivePayload() throws IOException {
        Gson gson = new Gson();
        URL url = new URL("https://api.mcleaks.net/");
        String response = IOUtils.toString(url.openStream(), "UTF-8");
        Payload payload = gson.fromJson(response, Payload.class);
        if(payload != null && payload.serverip != null) {
            parent.receiveStatus(payload);
        } else {
            throw new IOException("cannot parse response: " + response);
        }
    }

    static class Payload {
        String serverip, altdispenserip, updateurl, version, osxVersion, macosVersion, linuxVersion;
    }
}
