package ru.turikhay.util.windows.dxdiag;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

class DxDiagTest {

    @Test
    @Disabled
    void makeRealQuery() throws ExecutionException, InterruptedException {
        DxDiag dxDiag = new DxDiag();
        System.out.println(dxDiag.getScheduledTask().get());
    }

}