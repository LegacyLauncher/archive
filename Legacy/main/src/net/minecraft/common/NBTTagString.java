package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagString extends NBTBase {
    public String data;

    public NBTTagString(String par1Str) {
        super(par1Str);
    }

    public NBTTagString(String par1Str, String par2Str) {
        super(par1Str);
        data = par2Str;
        if (par2Str == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void write(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeUTF(data);
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        data = par1DataInput.readUTF();
    }

    public byte getId() {
        return (byte) 8;
    }

    public String toString() {
        return data;
    }

    public NBTBase copy() {
        return new NBTTagString(getName(), data);
    }

    public boolean equals(Object par1Obj) {
        if (!super.equals(par1Obj)) {
            return false;
        } else {
            NBTTagString var2 = (NBTTagString) par1Obj;
            return data == null && var2.data == null || data != null && data.equals(var2.data);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ data.hashCode();
    }
}
