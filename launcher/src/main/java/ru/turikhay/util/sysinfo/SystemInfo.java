package ru.turikhay.util.sysinfo;

import java.util.List;

public class SystemInfo {
    private final List<String> lines;
    private final boolean is64Bit;
    private final List<GraphicsCard> graphicsCards;

    public SystemInfo(List<String> lines, boolean is64Bit, List<GraphicsCard> graphicsCards) {
        this.lines = lines;
        this.is64Bit = is64Bit;
        this.graphicsCards = graphicsCards;
    }

    public List<String> getLines() {
        return lines;
    }

    public boolean is64Bit() {
        return is64Bit;
    }

    public List<GraphicsCard> getGraphicsCards() {
        return graphicsCards;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "lines=" + lines +
                ", is64Bit=" + is64Bit +
                ", graphicsCards=" + graphicsCards +
                '}';
    }
}
