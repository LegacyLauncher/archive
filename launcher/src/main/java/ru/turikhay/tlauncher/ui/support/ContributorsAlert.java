package ru.turikhay.tlauncher.ui.support;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.FileUtil;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ContributorsAlert {
    private static final Logger LOGGER = LogManager.getLogger(ContributorsAlert.class);

    private static JsonObject contributors;
    private static SimpleConfiguration proofreaders;

    private static void showIt() throws Exception {
        if (TLauncher.getInstance() == null) {
            return;
        }
        if (contributors == null) {
            contributors = (JsonObject) new JsonParser().parse(new InputStreamReader(ContributorsAlert.class.getResourceAsStream("/lang/_contrib.json"), FileUtil.getCharset()));
        }
        if (proofreaders == null) {
            proofreaders = new SimpleConfiguration(ContributorsAlert.class.getResource("/lang/_proofr.properties"));
        }

        Configuration settings = TLauncher.getInstance().getSettings();
        Locale locale = settings.getLocale();

        boolean hasUpgraded = TLauncher.getVersion().greaterThanOrEqualTo(settings.getVersion("update.asked")),
                contributorsHaveBeenShownBefore = locale.toString().equals(settings.get("contributors"));

        settings.set("contributors", locale.toString());
        settings.set("update.asked", TLauncher.getVersion());

        if (hasUpgraded && !contributorsHaveBeenShownBefore) {
            Locale ruLocale = LangConfiguration.ru_RU;
            boolean isUssr = ruLocale != null && settings.isLikelyRussianSpeakingLocale();

            List<String> contributorList = new ArrayList<>();
            int others = 0;

            JsonArray contribArray = (JsonArray) contributors.get(locale.toString());

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
                    b.append("\n\u2022 <b>").append(contributor).append("</b>");
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
            LOGGER.error("Couldn't show window", e);
        }
    }

    private ContributorsAlert() {
    }
}
