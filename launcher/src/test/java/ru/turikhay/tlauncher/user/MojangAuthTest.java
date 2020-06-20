package ru.turikhay.tlauncher.user;

import org.testng.annotations.Test;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.U;

import java.util.UUID;

import static org.testng.Assert.*;

public class MojangAuthTest {
    private final MojangAuth auth = new MojangAuth();

    @Test
    public void testAuthorize() throws Exception {
        authorize(auth);
    }

    @Test
    public void testValidate() throws Exception {
        MojangUser user = authorize(auth);
        auth.validate(user);
        assertUser(user);
    }

    static MojangUser authorize(MojangAuth auth) throws Exception {
        MojangUser user = auth.authorize("alex.paokarath4@gmail.com", "12092002ok");
        assertUser(user);
        return user;
    }

    static void assertUser(MojangUser user) throws Exception {
        assertEquals(user.getUsername(), "alex.paokarath4@gmail.com", "username");
        assertEquals(user.getDisplayName(), "AlexNick02GR", "displayName");
        assertEquals(user.getMojangUserAuthentication().getSelectedProfile().getId(), UUID.fromString("1f52be97-476e-44c3-b3dd-8a8be8b3bbb4"), "uuid");
    }
}