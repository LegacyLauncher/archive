package ru.turikhay.tlauncher.user.minecraft;

import ru.turikhay.tlauncher.user.MinecraftUser;
import ru.turikhay.tlauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import ru.turikhay.tlauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestStrategy;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import ru.turikhay.tlauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.ProfileNotCreatedException;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.create.MinecraftProfileCreator;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import ru.turikhay.tlauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MinecraftOAuthAuthenticate {

    // Вот что бывает, когда с самого начала забил на юнит тестирование,
    // но надо протестировать хотя бы эту часть программы...

    // Вся логика плюс-минус понятна. Надеюсь. Просто названия длинные и реализация скрыта.

    private final MicrosoftOAuthCodeRequestStrategy microsoftOAuthCodeRequestStrategy;
    private final MicrosoftOAuthCodeExchanger microsoftOAuthCodeExchanger;
    private final XboxLiveAuthenticator xboxLiveAuthenticator;
    private final XSTSAuthenticator xstsAuthenticator;
    private final MinecraftServicesAuthenticator minecraftServicesAuthenticator;
    private final GameOwnershipValidator gameOwnershipValidator;
    private final MinecraftProfileRequester minecraftProfileRequester;
    private final MinecraftProfileCreator minecraftProfileCreator;
    private final MinecraftProfileConverter minecraftProfileConverter;

    public MinecraftOAuthAuthenticate(MicrosoftOAuthCodeRequestStrategy microsoftOAuthCodeRequestStrategy,
                                      MicrosoftOAuthCodeExchanger microsoftOAuthCodeExchanger,
                                      XboxLiveAuthenticator xboxLiveAuthenticator,
                                      XSTSAuthenticator xstsAuthenticator,
                                      MinecraftServicesAuthenticator minecraftServicesAuthenticator,
                                      GameOwnershipValidator gameOwnershipValidator,
                                      MinecraftProfileRequester minecraftProfileRequester,
                                      MinecraftProfileCreator minecraftProfileCreator,
                                      MinecraftProfileConverter minecraftProfileConverter) {
        this.microsoftOAuthCodeRequestStrategy = microsoftOAuthCodeRequestStrategy;
        this.microsoftOAuthCodeExchanger = microsoftOAuthCodeExchanger;
        this.xboxLiveAuthenticator = xboxLiveAuthenticator;
        this.xstsAuthenticator = xstsAuthenticator;
        this.minecraftServicesAuthenticator = minecraftServicesAuthenticator;
        this.gameOwnershipValidator = gameOwnershipValidator;
        this.minecraftProfileRequester = minecraftProfileRequester;
        this.minecraftProfileCreator = minecraftProfileCreator;
        this.minecraftProfileConverter = minecraftProfileConverter;
    }

    public MinecraftUser authenticate() throws MinecraftAuthenticationException, InterruptedException, IOException, TimeoutException {
        MicrosoftOAuthExchangeCode oareq = microsoftOAuthCodeRequestStrategy.requestMicrosoftOAuthCode();
        MicrosoftOAuthToken oaex = microsoftOAuthCodeExchanger.exchangeMicrosoftOAuthCode(oareq);
        XboxServiceAuthenticationResponse xbAuth = xboxLiveAuthenticator.xboxLiveAuthenticate(oaex.getAccessToken());
        XboxServiceAuthenticationResponse xbXsts = xstsAuthenticator.xstsAuthenticate(xbAuth.getToken());
        MinecraftServicesToken mcsToken = minecraftServicesAuthenticator.minecraftServicesAuthenticate(xbXsts);
        gameOwnershipValidator.checkGameOwnership(mcsToken);
        MinecraftOAuthProfile profile;
        try {
            profile = minecraftProfileRequester.requestProfile(mcsToken);
        } catch (ProfileNotCreatedException e) {
            profile = minecraftProfileCreator.createProfile(mcsToken);
        }
        return minecraftProfileConverter.convertToMinecraftUser(oaex, mcsToken, profile);
    }

}
