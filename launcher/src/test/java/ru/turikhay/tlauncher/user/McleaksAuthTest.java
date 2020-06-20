package ru.turikhay.tlauncher.user;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.turikhay.tlauncher.managers.McleaksManager;
import ru.turikhay.util.U;

public class McleaksAuthTest {

    @BeforeMethod
    public void setUp() throws Exception {
        McleaksManager.getStatus().waitForResponse(0);
    }

    @Test
    public void testAuthorize() throws Exception {
        McleaksAuth auth = new McleaksAuth();
        String code = "";
        McleaksUser user;
        try {
            user = auth.authorize("beEJysWp0cRYgrkO");
        } catch(McleaksAltTokenExpired expired) {
            return;
        }
        U.log(user);
        auth.validate(user);
        U.log(user);
        //assertEquals(user.getDisplayName(), "");
    }

    @Test
    public void testValidate() throws Exception {
    }

}