package ru.turikhay.tlauncher.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.minecraft.Server;
import ru.turikhay.tlauncher.minecraft.ServerDeserializer;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.tlauncher.ui.notice.NoticeDeserializer;
import ru.turikhay.util.U;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BootConfiguration {
    private boolean stats, ely;
    private Map<String, List<String>> repositories = new HashMap<String, List<String>>();
    private Map<String, List<Notice>> notices = new HashMap<String, List<Notice>>();

    public boolean isStatsAllowed() {
        return stats;
    }

    public boolean isElyAllowed() {
        return ely;
    }

    public Map<String, List<String>> getRepositories() {
        return repositories;
    }

    public Map<String, List<Notice>> getNotices() {
        return notices;
    }

    public static BootConfiguration parse(BootBridge bridge) {
        try {
            U.log("parsing bootconfig");
            return parse(bridge.getOptions());
        } catch(RuntimeException rE) {
            U.log("could not parse bootconfig", rE);
        }
        U.log("returned empty bootconfig");
        return new BootConfiguration();
    }

    private static BootConfiguration parse(String options) {
        U.requireNotNull(options, "options");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Notice.class, new NoticeDeserializer())
                .registerTypeAdapter(Server.class, new ServerDeserializer())
                .create();

        return gson.fromJson(options, BootConfiguration.class);
    }
}
