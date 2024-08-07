package net.legacylauncher.ui.support;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.configuration.LangConfiguration;
import net.legacylauncher.configuration.SimpleConfiguration;
import net.legacylauncher.ui.alert.Alert;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
public final class ContributorsAlert {
    private static JsonObject contributors;
    private static SimpleConfiguration proofreaders;

    private static void showIt() throws Exception {
        if (LegacyLauncher.getInstance() == null) {
            return;
        }
        if (contributors == null) {
            try (InputStream is = ContributorsAlert.class.getResourceAsStream("/lang/_contrib.json")) {
                assert is != null;
                InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
                contributors = new GsonBuilder().create().fromJson(reader, JsonObject.class);
            }
        }
        if (proofreaders == null) {
            proofreaders = new SimpleConfiguration(ContributorsAlert.class.getResource("/lang/_proofr.properties"));
        }

        Configuration settings = LegacyLauncher.getInstance().getSettings();
        Locale locale = settings.getLocale();

        boolean hasUpgraded = LegacyLauncher.getVersion().isHigherThanOrEquivalentTo(settings.getVersion("update.asked")),
                contributorsHaveBeenShownBefore = locale.toString().equals(settings.get("contributors"));

        settings.set("contributors", locale.toString());
        settings.set("update.asked", LegacyLauncher.getVersion());

        if (hasUpgraded && !contributorsHaveBeenShownBefore) {
            Locale ruLocale = LangConfiguration.ru_RU;
            boolean isUssr = ruLocale != null && settings.isLikelyRussianSpeakingLocale();

            List<String> contributorList = new ArrayList<>();
            int others = 0;

            JsonArray contribArray = contributors.getAsJsonArray(locale.toString());

            if (contribArray == null) {
                return;
            }

            for (JsonElement elem : contribArray.getAsJsonArray()) {
                if (elem.getAsJsonPrimitive().isNumber()) {
                    others = elem.getAsInt();
                    break;
                }
                contributorList.add(elem.getAsString());
            }

            if (contributorList.isEmpty()) {
                return;
            }

            StringBuilder b = new StringBuilder();

            b.append("<b>Legacy Launcher</b> ");
            b.append(isUssr ? "переведён на" : "is translated to");
            b.append(" <b>");
            b.append(locale.getDisplayName(locale));
            b.append("</b> ");
            b.append(isUssr ? "благодаря" : "thanks to");
            b.append(" ");

            if (contributorList.size() > 1) {
                b.append(isUssr ? "этим людям" : "these people");
                b.append(":\n");
                for (String contributor : contributorList) {
                    b.append("\n• <b>").append(contributor).append("</b>");
                }
                if (others > 0) {
                    b.append("\n ... and ").append(others).append(" others");
                }
            } else {
                b.append("<b>").append(contributorList.get(0)).append("</b>!");
            }

            String proofreader = proofreaders.get(locale.toString());

            if (proofreader != null) {
                b.append("\n\n");
                b.append(isUssr ? "Перед выпуском перевод проверил товарищ" : "Translation proofreading is done by");
                b.append(" <b>");
                b.append(proofreader);
                b.append("</b>");
            }

            Alert.showMessage(isUssr ? "Перевод" : "Translation", b.toString());
        }
    }

    public static void showAlert() {
        try {
            showIt();
        } catch (Exception e) {
            log.error("Couldn't show window", e);
        }
    }

    private ContributorsAlert() {
    }
}
