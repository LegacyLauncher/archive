package net.legacylauncher.user.minecraft.strategy.gos;

import net.legacylauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import net.legacylauncher.user.minecraft.strategy.rqnpr.*;
import org.apache.http.client.fluent.Request;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class GameOwnershipValidator extends RequestAndParseStrategy<MinecraftServicesToken, MinecraftUserGameOwnershipResponse> {
    private static final Logger LOGGER = LogManager.getLogger(GameOwnershipValidator.class);

    public GameOwnershipValidator() {
        this(new HttpClientRequester<>(token ->
                Request.Get("https://api.minecraftservices.com/entitlements/license?requestId=" + UUID.randomUUID())
                        .addHeader("Authorization", "Bearer " + token.getAccessToken()))
        );
    }

    GameOwnershipValidator(Requester<MinecraftServicesToken> requester) {
        this(requester, GsonParser.defaultParser(MinecraftUserGameOwnershipResponse.class));
    }

    GameOwnershipValidator(Requester<MinecraftServicesToken> requester,
                           Parser<MinecraftUserGameOwnershipResponse> parser) {
        super(LOGGER, requester, parser);
    }

    public void checkGameOwnership(MinecraftServicesToken token)
            throws GameOwnershipValidationException, IOException {
        MinecraftUserGameOwnershipResponse response;
        try {
            response = requestAndParse(token);
        } catch (InvalidResponseException e) {
            throw new GameOwnershipValidationException(e);
        }
        List<MinecraftUserGameOwnershipResponse.Item> items = response.getItems();
        if (items.isEmpty()) {
            throw new GameOwnershipValidationException("no ownership found");
        }
        if (items.stream().noneMatch(item -> "product_minecraft".equals(item.getName()))) {
            throw new GameOwnershipValidationException("no \"product_minecraft\"");
        }
        if (items.stream().noneMatch(item -> "game_minecraft".equals(item.getName()))) {
            throw new GameOwnershipValidationException("no \"game_minecraft\"");
        }
    }
}
