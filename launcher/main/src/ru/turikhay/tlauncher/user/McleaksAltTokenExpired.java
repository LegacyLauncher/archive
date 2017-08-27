package ru.turikhay.tlauncher.user;

public class McleaksAltTokenExpired extends InvalidCredentialsException {
    McleaksAltTokenExpired() {
        super("ALT-TOKEN has expired", "alt-token");
    }
}
