package ru.turikhay.tlauncher.managers;

import ru.turikhay.app.nstweaker.IsolatedTweakedConnector;
import ru.turikhay.app.nstweaker.TweakHostname;
import ru.turikhay.app.nstweaker.TweakHostnameService;
import ru.turikhay.util.StringUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class McleaksConnector {
    private static final String[] HOSTS = {"authserver.mojang.com", "sessionserver.mojang.com"};

    private List<TweakHostname> hostnameList;
    private IsolatedTweakedConnector connector;

    McleaksConnector() {
    }

    public List<TweakHostname> getList() {
        return hostnameList;
    }

    public HttpsURLConnection setupHttpsConnection(URL url) throws IOException {
        Objects.requireNonNull(url, "url");
        if (connector == null) {
            throw new IllegalStateException("connector is not available");
        }
        return connector.setupHttpsConnection(url);
    }

    void receiveServerIp(String ip) {
        StringUtil.requireNotBlank(ip, "ip");
        this.hostnameList = createHostnameList(ip);
        this.connector = createIsolatedConnector(hostnameList);
    }

    private static List<TweakHostname> createHostnameList(String ip) {
        List<TweakHostname> hostnameList = new ArrayList<>();
        for (String host : HOSTS) {
            hostnameList.add(TweakHostname.ipv4(ip, host));
        }
        return hostnameList;
    }

    private static IsolatedTweakedConnector createIsolatedConnector(List<TweakHostname> list) {
        return new IsolatedTweakedConnector(new TweakHostnameService(list));
    }
}
