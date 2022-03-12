package ru.turikhay.tlauncher.ui.swing.extended;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.git.ITokenResolver;

public class HtmlSubstitutor implements ITokenResolver {
    @Override
    public String resolveToken(String token) {
        int separatorIndex = StringUtils.indexOf(token, ':');
        if (separatorIndex == -1 || separatorIndex == token.length() - 1) {
            return "";
        }

        String tokenName = StringUtils.substring(token, 0, separatorIndex),
                tokenValue = StringUtils.substring(token, separatorIndex + 1);

        switch (tokenName) {
            case "image":
                return tokenValue; // handled by ExtendedImageView
            case "size":
                return String.valueOf(SwingUtil.magnify(Integer.parseInt(tokenValue)));
        }

        return "";
    }
}
