package net.legacylauncher.user.minecraft.strategy.rqnpr;

import net.legacylauncher.exceptions.ParseException;
import org.apache.logging.log4j.Logger;

public interface Parser<V extends Validatable> {
    V parseResponse(Logger logger, String response) throws ParseException;
}
