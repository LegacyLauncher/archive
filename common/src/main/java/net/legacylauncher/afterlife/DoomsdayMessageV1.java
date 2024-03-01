package net.legacylauncher.afterlife;

import java.util.*;

public class DoomsdayMessageV1 {
    public static final List<String> URL_LIST = Collections.unmodifiableList(Arrays.asList(
            "https://europe-west3-legacylauncher.cloudfunctions.net/doomsday",
            "https://functions.yandexcloud.net/d4e6s85mtp5rjisfrgil"
    ));

    @SuppressWarnings({"unused", "MismatchedQueryAndUpdateOfCollection"})
    private Map<String, String> messages;

    public DoomsdayMessageV1() {
    }

    public DoomsdayMessageV1(Map<String, String> messages) {
        this.messages = messages;
    }

    public DoomsdayMessageV1 validate() {
        Objects.requireNonNull(messages);
        messages.values().forEach(Objects::requireNonNull);
        return this;
    }

    public String getMessageOrDefault(String locale) {
        return messages.getOrDefault(locale, messages.get("en_US"));
    }

    public String getMessageOrDefault() {
        return getMessageOrDefault(Locale.getDefault().toString());
    }

    @Override
    public String toString() {
        return "DoomsdayMessage{" +
                "messages=" + messages +
                '}';
    }
}
