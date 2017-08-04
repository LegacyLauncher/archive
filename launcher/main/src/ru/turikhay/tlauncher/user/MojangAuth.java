package ru.turikhay.tlauncher.user;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.io.IOException;
import java.util.UUID;

public final class MojangAuth implements StandardAuth<MojangUser> {

    @Override
    public MojangUser authorize(String username, String password) throws AuthException, IOException {
        StringUtil.requireNotBlank(username, "username");
        StringUtil.requireNotBlank(password, "password");

        String clientToken = String.valueOf(UUID.randomUUID());
        com.mojang.authlib.UserAuthentication userAuthentication = createUserAuthentication(clientToken);

        userAuthentication.setUsername(username);
        userAuthentication.setPassword(password);

        logIn(userAuthentication);

        return new MojangUser(clientToken, username, userAuthentication);
    }

    @Override
    public void validate(MojangUser user) throws AuthException, IOException {
        logIn(U.requireNotNull(user, "user").getMojangUserAuthentication());
    }

    private void logIn(com.mojang.authlib.UserAuthentication userAuthentication) throws AuthException, IOException {
        try {
            U.requireNotNull(userAuthentication, "user").logIn();
        } catch (com.mojang.authlib.exceptions.UserMigratedException userMigrated) {
            throw new AuthException(userMigrated, "migrated");
        } catch (com.mojang.authlib.exceptions.InvalidCredentialsException invalidCredentials) {
            throw new InvalidCredentialsException(invalidCredentials.getMessage());
        } catch (com.mojang.authlib.exceptions.AuthenticationException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw new AuthUnknownException(e);
        }
    }

    static com.mojang.authlib.UserAuthentication createUserAuthentication(String clientToken) {
        com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService service =
                new com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService(U.getProxy(), StringUtil.requireNotBlank(clientToken));
        return service.createUserAuthentication(com.mojang.authlib.Agent.MINECRAFT);
    }
}
