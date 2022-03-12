package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;

public class NBTTagEnd extends NBTBase {
    public NBTTagEnd() {
        super(null);
    }

    void load(DataInput par1DataInput, int par2) {
    }

    void write(DataOutput par1DataOutput) {
    }

    public byte getId() {
        return (byte) 0;
    }

    public String toString() {
        return "END";
    }

    public NBTBase copy() {
        return new NBTTagEnd();
    }
}
