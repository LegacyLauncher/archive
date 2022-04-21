package ru.turikhay.tlauncher.user.minecraft.strategy.preq.create;

import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.preq.MinecraftOAuthProfile;

import java.io.IOException;
import java.util.function.Function;

public class MinecraftProfileCreator {
    private final ProfileCreatorUserInterface userInterface;
    private final Function<String, ProfileCreateRequest> createRequestFactory;

    public MinecraftProfileCreator(
            ProfileCreatorUserInterface userInterface,
            Function<String, ProfileCreateRequest> createRequestFactory) {
        this.userInterface = userInterface;
        this.createRequestFactory = createRequestFactory;
    }

    public MinecraftProfileCreator(ProfileCreatorUserInterface userInterface) {
        this(userInterface, ProfileCreateRequest::new);
    }

    public MinecraftOAuthProfile createProfile(MinecraftServicesToken token) throws IOException, ProfileCreationAbortedException {
        String profileName;
        do {
            profileName = userInterface.requestProfileName();
            if (profileName == null) {
                throw new ProfileCreationAbortedException();
            }
            try {
                return createRequestFactory.apply(profileName).createProfile(token);
            } catch (MinecraftProfileCreateException e) {
                userInterface.showProfileUnavailableMessage(profileName);
            }
        } while (true);
    }
}
