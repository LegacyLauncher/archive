package ru.turikhay.util.windows.dxdiag;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DisplayDevice {
    private final String cardName, manufacturer, dacType, type, key, driverVersion, driverModel,
            driverDate, hybridGraphics;

    DisplayDevice(String cardName, String manufacturer, String dacType, String type, String key,
                  String driverVersion, String driverModel, String driverDate, String hybridGraphics) {
        this.cardName = cardName;
        this.manufacturer = manufacturer;
        this.dacType = dacType;
        this.type = type;
        this.key = key;
        this.driverVersion = driverVersion;
        this.driverModel = driverModel;
        this.driverDate = driverDate;
        this.hybridGraphics = hybridGraphics;
    }

    DisplayDevice(Section section) {
        this(
                section.get("CardName"),
                section.get("Manufacturer"),
                section.get("DACType"),
                section.get("DeviceType"),
                section.get("DeviceKey"),
                section.get("DriverVersion"),
                section.get("DriverModel"),
                section.get("DriverDate"),
                section.get("HybridGraphicsGPUType")
        );
    }

    public String getCardName() {
        return cardName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getDriverVersion() {
        return driverVersion;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("cardName", cardName)
                .append("manufacturer", manufacturer)
                .append("dacType", dacType)
                .append("type", type)
                .append("key", key)
                .append("driverVersion", driverVersion)
                .append("driverModel", driverModel)
                .append("driverDate", driverDate)
                .append("hybridGraphics", hybridGraphics)
                .toString();
    }
}
