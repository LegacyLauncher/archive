package net.legacylauncher.ui.pr.beeline;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class FraudHuntersTaskTest {

    @Test
    @Disabled
    void install() throws ExecutionException, InterruptedException {
        FraudHuntersTask task = new FraudHuntersTask();
        task.prepareLauncher(null).get();
    }

    @Test
    void queryVersion() throws ExecutionException, InterruptedException {
        FraudHuntersTask task = new FraudHuntersTask();
        System.out.println(task.queryLauncherVersion().get());
    }

    @Test
    void queryHash() throws ExecutionException, InterruptedException {
        FraudHuntersTask task = new FraudHuntersTask();
        System.out.println(task.queryLauncherHash().get());
    }

}
