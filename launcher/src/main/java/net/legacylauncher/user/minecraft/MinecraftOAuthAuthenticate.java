package net.legacylauncher.user.minecraft;

import net.legacylauncher.user.MinecraftUser;
import net.legacylauncher.user.minecraft.strategy.MinecraftAuthenticationException;
import net.legacylauncher.user.minecraft.strategy.gos.GameOwnershipValidator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesAuthenticator;
import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestStrategy;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;
import net.legacylauncher.user.minecraft.strategy.oatoken.MicrosoftOAuthToken;
import net.legacylauncher.user.minecraft.strategy.oatoken.exchange.MicrosoftOAuthCodeExchanger;
import net.legacylauncher.user.minecraft.strategy.pconv.MinecraftProfileConverter;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;
import net.legacylauncher.user.minecraft.strategy.preq.MinecraftProfileRequester;
import net.legacylauncher.user.minecraft.strategy.preq.ProfileNotCreatedException;
import net.legacylauncher.user.minecraft.strategy.preq.create.MinecraftProfileCreator;
import net.legacylauncher.user.minecraft.strategy.xb.XboxServiceAuthenticationResponse;
import net.legacylauncher.user.minecraft.strategy.xb.auth.XboxLiveAuthenticator;
import net.legacylauncher.user.minecraft.strategy.xb.xsts.XSTSAuthenticator;

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
