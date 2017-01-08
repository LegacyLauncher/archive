package ru.turikhay.tlauncher.ui.accounts;

import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class UsernameField extends LocalizableTextField {
    private static final long serialVersionUID = -5813187607562947592L;
    private UsernameField.UsernameState state;
    String username;

    public UsernameField(CenterPanel pan, UsernameField.UsernameState state) {
        super(pan, "account.username");
        setState(state);
    }

    public UsernameField.UsernameState getState() {
        return state;
    }

    public void setState(UsernameField.UsernameState state) {
        if (state == null) {
            throw new NullPointerException();
        } else {
            this.state = state;
            setPlaceholder(state.placeholder);
        }
    }

    public enum UsernameState {
        USERNAME("account.username"),
        EMAIL("account.email");

        private final String placeholder;

        UsernameState(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }
}
