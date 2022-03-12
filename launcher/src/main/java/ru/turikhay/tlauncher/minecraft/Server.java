package ru.turikhay.tlauncher.minecraft;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

public class Server {
    public static final int DEFAULT_PORT = 25565;

    private String name, address;
    private int port;

    protected Server() {
    }

    public Server(String name, String address, int port) {
        setName(name);
        setAddress(address);
        setPort(port);
    }

    public final String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public final String getAddress() {
        return address;
    }

    protected void setAddress(String address) {
        this.address = address;
    }

    public final int getPort() {
        return port;
    }

    protected void setPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("illegal port: " + port);
        }
        this.port = port;
    }

    protected void setPort(String port) {
        int parsedPort;
        try {
            parsedPort = Integer.parseInt(port);
        } catch (RuntimeException rE) {
            throw new RuntimeException("could not parse port: " + port, rE);
        }
        setPort(parsedPort);
    }

    public String getFullAddress() {
        return address + (port == DEFAULT_PORT ? "" : ":" + port);
    }

    protected void setFullAddress(String fullAddress) {
        fullAddress = StringUtils.trim(fullAddress);

        if (fullAddress.startsWith("[") && fullAddress.contains("]:")) {
            // ipv6 with port
            String address;
            int port;
            try {
                address = fullAddress.substring(0, fullAddress.indexOf(']'));
                port = Integer.parseInt(fullAddress.substring(fullAddress.indexOf(']') + 2));
            } catch (RuntimeException rE) {
                throw new RuntimeException("could not parse ipv6 address: " + fullAddress, rE);
            }
            setAddress(address);
            setPort(port);
            return;
        }
        switch (StringUtils.countMatches(fullAddress, ':')) {
            case 0:
                // ipv4 without port
                setAddress(fullAddress);
                setPort(DEFAULT_PORT);
                break;
            case 1:
                // ipv4 with port
                if (fullAddress.startsWith(":")) {
                    setAddress("");
                    setPort(fullAddress.substring(1));
                } else {
                    String[] split = StringUtils.split(fullAddress, ':');
                    setAddress(split[0]);
                    setPort(split[1]);
                }
                break;
            default:
                // ipv6 without port
                setAddress('[' + fullAddress + ']');
                setPort(DEFAULT_PORT);
                break;
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Server)) {
            return false;
        }
        Server s = (Server) o;
        return getFullAddress().equalsIgnoreCase(s.getFullAddress());
    }

    @Override
    public int hashCode() {
        return getFullAddress().toLowerCase(java.util.Locale.ROOT).hashCode();
    }

    public final String toString() {
        return toStringBuilder().build();
    }

    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("address", address)
                .append("port", port);
    }
}
