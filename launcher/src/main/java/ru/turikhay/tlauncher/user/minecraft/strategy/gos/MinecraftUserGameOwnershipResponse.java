package ru.turikhay.tlauncher.user.minecraft.strategy.gos;

import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.util.List;
import java.util.Objects;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.*;

public class MinecraftUserGameOwnershipResponse implements Validatable {
    private List<Item> items;

    public MinecraftUserGameOwnershipResponse(List<Item> items) {
        this.items = items;
    }

    public MinecraftUserGameOwnershipResponse() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinecraftUserGameOwnershipResponse that = (MinecraftUserGameOwnershipResponse) o;

        return Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public void validate() {
        notNull(items, "items");
        for (Item item : items) {
            item.validate();
        }
    }

    @Override
    public String toString() {
        return "MinecraftUserGameOwnershipResponse{" +
                "items=" + items +
                '}';
    }

    public static class Item implements Validatable {
        private String name, signature;

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (!name.equals(item.name)) return false;
            return signature.equals(item.signature);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + signature.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "name='" + name + '\'' +
                    ", signature='" + signature + '\'' +
                    '}';
        }

        @Override
        public void validate() {
            notEmpty(name, "name");
            notEmpty(signature, "signature");
        }
    }
}
