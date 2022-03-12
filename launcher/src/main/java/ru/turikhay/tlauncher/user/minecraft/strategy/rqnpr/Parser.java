package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.exceptions.ParseException;

public interface Parser<V extends Validatable> {
    V parseResponse(Logger logger, String response) throws ParseException;
}
