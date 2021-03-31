package ru.turikhay.util.windows.dxdiag;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SysInfo {
    private final String os, model, lang, cpu, ram, dxVersion;

    SysInfo(String os, String model, String lang, String cpu, String ram, String dxVersion) {
        this.os = os;
        this.model = model;
        this.lang = lang;
        this.cpu = cpu;
        this.ram = ram;
        this.dxVersion = dxVersion;
    }

    SysInfo(Section section) {
        this(
                section.get("OperatingSystem"),
                section.get("SystemManufacturer") + " " + section.get("SystemModel"),
                section.get("Language"),
                section.get("Processor"),
                section.get("Memory"),
                section.get("DirectXVersion")
        );
    }

    public boolean is64Bit() {
        return os != null && os.contains("64-bit");
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("os", os)
                .append("model", model)
                .append("lang", lang)
                .append("cpu", cpu)
                .append("ram", ram)
                .append("dxVersion", dxVersion)
                .toString();
    }
}
