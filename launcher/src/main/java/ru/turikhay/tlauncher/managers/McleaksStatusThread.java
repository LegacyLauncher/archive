package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import ru.turikhay.util.async.ExtendedThread;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
        } catch (Exception e) {
            parent.receiveNothing();
        }
    }

    private void receivePayload() throws IOException {
        Gson gson = new Gson();
        URL url = new URL("https://api.mcleaks.net/");
        String response = IOUtils.toString(url.openStream(), StandardCharsets.UTF_8);
        Payload payload = gson.fromJson(response, Payload.class);
        if (payload != null && payload.serverip != null) {
            parent.receiveStatus(payload);
        } else {
            throw new IOException("cannot parse response: " + response);
        }
    }

    static class Payload {
        String serverip, altdispenserip, updateurl, version, osxVersion, macosVersion, linuxVersion;
    }
}
