package ru.turikhay.tlauncher.user.minecraft.strategy.gos;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.user.minecraft.strategy.mcsauth.MinecraftServicesToken;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.InvalidResponseException;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.MockRequester;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class GameOwnershipValidatorTest {

    @Test
    void test() throws GameOwnershipValidationException, IOException {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.returning("{\"items\":[{\"name\":\"product_minecraft\",\"signature\":\"jwt sig\"},{\"name\":\"game_minecraft\",\"signature\":\"jwt sig\"}],\"signature\":\"jwt sig\",\"keyId\":\"1\"}")
        );
        s.checkGameOwnership(new MinecraftServicesToken("access_token", 0));
    }

    @Test
    void unknownError() {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.throwing(new InvalidResponseException("bad response"))
        );
        GameOwnershipValidationException e = assertThrows(
                GameOwnershipValidationException.class,
                () -> s.checkGameOwnership(new MinecraftServicesToken("access_token", 0))
        );
        assertFalse(e.isKnownNotToOwn());
    }

    @Test
    void noItemsField() {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.returning("{}")
        );
        GameOwnershipValidationException e = assertThrows(
                GameOwnershipValidationException.class,
                () -> s.checkGameOwnership(new MinecraftServicesToken("access_token", 0))
        );
        assertFalse(e.isKnownNotToOwn());
    }

    @Test
    void emptyItemsField() {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.returning("{\"items\":[]}")
        );
        GameOwnershipValidationException e = assertThrows(
                GameOwnershipValidationException.class,
                () -> s.checkGameOwnership(new MinecraftServicesToken("access_token", 0))
        );
        assertTrue(e.isKnownNotToOwn());
    }

    @Test
    void noProductMinecraft() {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.returning("{\"items\":[{\"name\":\"game_minecraft\",\"signature\":\"jwt sig\"}],\"signature\":\"jwt sig\",\"keyId\":\"1\"}")
        );
        GameOwnershipValidationException e = assertThrows(
                GameOwnershipValidationException.class,
                () -> s.checkGameOwnership(new MinecraftServicesToken("access_token", 0))
        );
        assertTrue(e.isKnownNotToOwn());
    }

    @Test
    void noGameMinecraft() {
        GameOwnershipValidator s = new GameOwnershipValidator(
                MockRequester.returning("{\"items\":[{\"name\":\"product_minecraft\",\"signature\":\"jwt sig\"}],\"signature\":\"jwt sig\",\"keyId\":\"1\"}")
        );
        GameOwnershipValidationException e = assertThrows(
                GameOwnershipValidationException.class,
                () -> s.checkGameOwnership(new MinecraftServicesToken("access_token", 0))
        );
        assertTrue(e.isKnownNotToOwn());
    }

}