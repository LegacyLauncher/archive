package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagLong extends NBTBase {
    public long data;

    public NBTTagLong(String par1Str) {
        super(par1Str);
    }

    public NBTTagLong(String par1Str, long par2) {
        super(par1Str);
        data = par2;
    }

    void write(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeLong(data);
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        data = par1DataInput.readLong();
    }

    public byte getId() {
        return (byte) 4;
    }

    public String toString() {
        return "" + data;
    }

    public NBTBase copy() {
        return new NBTTagLong(getName(), data);
    }

    public boolean equals(Object par1Obj) {
        if (super.equals(par1Obj)) {
            NBTTagLong var2 = (NBTTagLong) par1Obj;
            return data == var2.data;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ (int) (data ^ data >>> 32);
    }
}
