package net.legacylauncher.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.legacylauncher.minecraft.PromotedServer;
import net.legacylauncher.minecraft.PromotedServerDeserializer;
import net.legacylauncher.ui.notice.Notice;
import net.legacylauncher.ui.notice.NoticeDeserializer;
import net.legacylauncher.ui.notification.UrlNotificationObject;

import java.util.*;

public final class BootConfiguration {
    private boolean stats, ely;
    private final Map<String, List<String>> repositories = new HashMap<>();
    private final Map<String, List<Notice>> notices = new HashMap<>();
    private final Map<String, List<PromotedServer>> promotedServers = new HashMap<>();
    private final Map<String, String> feedback = new HashMap<>();
    private final Map<String, UrlNotificationObject> notifications = new HashMap<>();
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

    public Map<String, String> getFeedback() {
        return feedback;
    }

    public Map<String, UrlNotificationObject> getNotifications() {
        return notifications;
    }

    public boolean isAllowNoticeDisable(UUID userId) {
        return (allowNoticeDisable > 0) && (userId.hashCode() % allowNoticeDisable == 0);
    }

    public int getAllowNoticeDisable() {
        return allowNoticeDisable;
    }

    public static BootConfiguration parse(String options) {
        Objects.requireNonNull(options, "options");

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Notice.class, new NoticeDeserializer())
                .registerTypeAdapter(PromotedServer.class, new PromotedServerDeserializer())
                .create();

        return gson.fromJson(options, BootConfiguration.class);
    }
}
