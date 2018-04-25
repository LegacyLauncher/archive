package ru.turikhay.tlauncher.stats;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;

public class Statsd {
    private static final StatsDClient statsd = new NonBlockingStatsDClient("tlauncher.test", "statsd.ely.by", 8125);

    public static void test() {
        statsd.incrementCounter("testCounter");
    }
}
