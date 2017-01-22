package ru.turikhay.tlauncher.minecraft;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Server {
    public static final int DEFAULT_PORT = 25565;

    private String name, address, family;
    private int port;

    private final Set<Account.AccountType> accountTypes = new HashSet<Account.AccountType>() {
        {
            Collections.addAll(this, Account.AccountType.values());
        }
    };

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
        this.name = U.requireNotNull(name, "name");
    }

    public final String getAddress() {
        return address;
    }

    protected void setAddress(String address) {
        this.address = StringUtil.requireNotBlank(address, "address");
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
        } catch(RuntimeException rE) {
            throw new RuntimeException("could not parse port: " + port, rE);
        }
        setPort(parsedPort);
    }

    public final String getFamily() {
        return family;
    }

    protected void setFamily(String family) {
        this.family = family;
    }

    public final Set<Account.AccountType> getAccountTypes() {
        return Collections.unmodifiableSet(accountTypes);
    }

    public final boolean hasAccountTypeRestriction() {
        return accountTypes.size() < Account.AccountType.values().length;
    }

    protected Set<Account.AccountType> getAccountTypeSet() {
        return accountTypes;
    }

    public String getFullAddress() {
        return address + ":" + port;
    }

    protected void setFullAddress(String fullAddress) {
        U.log("Setting full address:", fullAddress);

        if (fullAddress.startsWith("[") && fullAddress.contains("]:")) {
            U.log("this is ipv6 with port");
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
            U.log(":", getFullAddress());
            return;
        }
        switch(StringUtils.countMatches(fullAddress, ':')) {
            case 0:
                // ipv4 without port
                setAddress(fullAddress);
                setPort(DEFAULT_PORT);
                break;
            case 1:
                // ipv4 with port
                String[] split = StringUtils.split(fullAddress, ':');
                setAddress(split[0]);
                setPort(split[1]);
                break;
            default:
                // ipv6 without port
                setAddress('[' + fullAddress + ']');
                setPort(DEFAULT_PORT);
                break;
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Server)) {
            return false;
        }
        Server s = (Server) o;
        return getFullAddress().equalsIgnoreCase(s.getFullAddress());
    }

    @Override
    public int hashCode() {
        return getFullAddress().toLowerCase().hashCode();
    }

    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("name", name)
                .append("address", address)
                .append("port", port)
                .append("family", family)
                .build();
    }
}
