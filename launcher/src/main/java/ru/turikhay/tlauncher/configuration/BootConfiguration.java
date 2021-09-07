package ru.turikhay.tlauncher.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.minecraft.PromotedServer;
import ru.turikhay.tlauncher.minecraft.PromotedServerDeserializer;
import ru.turikhay.tlauncher.ui.notice.Notice;
import ru.turikhay.tlauncher.ui.notice.NoticeDeserializer;
import ru.turikhay.util.U;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BootConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    private boolean stats, ely;
    private Map<String, List<String>> repositories = new HashMap<String, List<String>>();
    private Map<String, List<Notice>> notices = new HashMap<String, List<Notice>>();
    private Map<String, List<PromotedServer>> promotedServers = new HashMap<>(), outdatedPromotedServers = new HashMap<>();
    private Map<String, String> feedback = new HashMap<>();
    private int allowNoticeDisable;

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

    public Map<String, List<PromotedServer>> getPromotedServers() {
        return promotedServers;
    }

    public Map<String, List<PromotedServer>> getOutdatedPromotedServers() {
        return outdatedPromotedServers;
    }

    public Map<String, String> getFeedback() {
        return feedback;
    }

    public boolean isAllowNoticeDisable(UUID userId) {
        return (allowNoticeDisable > 0) && (userId.hashCode() % allowNoticeDisable == 0);
    }

    public int getAllowNoticeDisable() {
        return allowNoticeDisable;
    }

    public static BootConfiguration parse(String options) {
        U.requireNotNull(options, "options");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Notice.class, new NoticeDeserializer())
                .registerTypeAdapter(PromotedServer.class, new PromotedServerDeserializer())
                .create();

        return gson.fromJson(options, BootConfiguration.class);
    }
}
