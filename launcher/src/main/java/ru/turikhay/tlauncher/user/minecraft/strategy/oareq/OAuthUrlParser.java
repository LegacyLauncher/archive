package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.turikhay.tlauncher.exceptions.ParseException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class OAuthUrlParser {
    public String parseAndValidate(String url)
            throws MicrosoftOAuthCodeRequestException, ParseException {
        List<NameValuePair> pairs;
        try {
            pairs = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new ParseException(e);
        }
        String error = findByKey(pairs, "error");
        if (error != null) {
            if (error.equals("access_denied")) {
                throw new CodeRequestCancelledException("redirect page received \"access_denied\"");
            }
            throw new CodeRequestErrorException(error, findByKey(pairs, "error_description"));
        }
        Optional<NameValuePair> code = pairs.stream().filter(p -> p.getName().equals("code")).findAny();
        if (code.isPresent()) {
            return code.get().getValue();
        }
        throw new ParseException("no code in query");
    }

    private static String findByKey(List<NameValuePair> pairs, String key) {
        Optional<NameValuePair> pair = pairs.stream()
                .filter(p -> p.getName().equals(key))
                .findAny();
        return pair.map(NameValuePair::getValue).orElse(null);
    }
}
