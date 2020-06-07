package ru.turikhay.util.pastebin;

public enum ExpireDate {
    NEVER("N"),
    TEN_MINUTES("10M"),
    ONE_HOUR("1H"),
    ONE_DAY("1D"),
    ONE_WEEK("1W"),
    TWO_WEEKS("2W"),
    ONE_MONTH("1M");

    private String value;

    ExpireDate(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
