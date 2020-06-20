package ru.turikhay.util.json;

import com.google.gson.JsonPrimitive;
import org.testng.annotations.Test;
import ru.turikhay.util.U;

import static org.testng.Assert.*;


public class LegacyVersionSerializerTest {
    @Test
    public void testDeserialize() throws Exception {
        U.log(new LegacyVersionSerializer().deserialize(new JsonPrimitive("1.79.5-beta"), null, null));
    }

}