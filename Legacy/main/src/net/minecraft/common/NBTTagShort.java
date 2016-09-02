package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagShort extends NBTBase {
    public short data;

    public NBTTagShort(String par1Str) {
        super(par1Str);
    }

    public NBTTagShort(String par1Str, short par2) {
        super(par1Str);
        data = par2;
    }

    void write(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeShort(data);
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        data = par1DataInput.readShort();
    }

    public byte getId() {
        return (byte) 2;
    }

    public String toString() {
        return "" + data;
    }

    public NBTBase copy() {
        return new NBTTagShort(getName(), data);
    }

    public boolean equals(Object par1Obj) {
        if (super.equals(par1Obj)) {
            NBTTagShort var2 = (NBTTagShort) par1Obj;
            return data == var2.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ data;
    }
}
