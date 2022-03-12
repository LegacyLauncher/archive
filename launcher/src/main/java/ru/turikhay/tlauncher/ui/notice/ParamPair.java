package ru.turikhay.tlauncher.ui.notice;

class ParamPair {
    final float fontSize;
    final int width;

    ParamPair(float fontSize, int width) {
        this.fontSize = fontSize;
        this.width = width;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParamPair paramPair = (ParamPair) o;

        return Float.compare(paramPair.fontSize, fontSize) == 0 && width == paramPair.width;
    }

    @Override
    public int hashCode() {
        int result = (fontSize != 0.0f ? Float.floatToIntBits(fontSize) : 0);
        result = 31 * result + width;
        return result;
    }
}
