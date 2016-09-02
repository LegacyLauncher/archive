package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.exception.ExceptionList;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.meta.UpdateMeta;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Updater {
    private static final List<String> UPDATE_URL_LIST = Arrays.asList(
            "http://cdn.turikhay.ru/tlauncher/update/%s.json",
            "http://u.tlauncher.ru/update/%s.json",
            "http://repo.tlauncher.ru/update/%s.json",
            "http://turikhay.ru/tlauncher/update/%s.json",
            "http://tlaun.ch/update/%s.json"
    );

    public static UpdateMeta getUpdate(String brand) throws ExceptionList {
        log("Requesting update for: " + brand);

        List<Exception> eList = new ArrayList<Exception>();

        for (String _url : UPDATE_URL_LIST) {
            try {
                URL url = new URL(String.format(_url, brand));
                log("URL: " + url);

                UpdateMeta meta = Json.parse(url.openConnection(U.getProxy()).getInputStream(), UpdateMeta.class);

                log("Success!");

                return meta;
            } catch (Exception e) {
                e.printStackTrace();
                eList.add(e);
            }
        }

        throw new ExceptionList(eList);
    }

    private static void log(String s) {
        U.log("[Updater]", s);
    }

    private Updater() {
    }
}
