package ru.turikhay.util.windows.dxdiag;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Optional;

public class DxDiagReport {
    private final SysInfo sysInfo;
    private final List<DisplayDevice> displayDevices;

    public DxDiagReport(SysInfo sysInfo, List<DisplayDevice> displayDevices) {
        this.sysInfo = sysInfo;
        this.displayDevices = displayDevices;
    }

    public SysInfo getSysInfo() {
        return sysInfo;
    }

    public List<DisplayDevice> getDisplayDevices() {
        return displayDevices;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("sysInfo", sysInfo)
                .append("displayDevices", displayDevices)
                .toString();
    }
}
