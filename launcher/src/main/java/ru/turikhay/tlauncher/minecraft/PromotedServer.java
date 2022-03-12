package ru.turikhay.tlauncher.minecraft;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.minecraft.auth.Account;

import java.util.*;

public class PromotedServer extends Server {
    private boolean claimed;
    private final List<String> family = new ArrayList<>();

    private final Set<Account.AccountType> accountTypes = new HashSet<Account.AccountType>() {
        {
            Collections.addAll(this, Account.AccountType.values());
        }
    };

    public final List<String> getFamily() {
        return family;
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

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("family", family).append("accountTypes", accountTypes);
    }
}
