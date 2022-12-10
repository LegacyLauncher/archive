package ru.turikhay.util.sysinfo;

public class GraphicsCard {
    private final String name, vendor, version;

    public GraphicsCard(String name, String vendor, String version) {
        this.name = name;
        this.vendor = vendor;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "GraphicsCard{" +
                "name='" + name + '\'' +
                ", vendor='" + vendor + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
