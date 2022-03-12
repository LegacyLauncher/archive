package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagByteArray extends NBTBase {
    public byte[] byteArray;

    public NBTTagByteArray(String par1Str) {
        super(par1Str);
    }

    public NBTTagByteArray(String par1Str, byte[] par2ArrayOfByte) {
        super(par1Str);
        byteArray = par2ArrayOfByte;
    }

    void write(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeInt(byteArray.length);
        par1DataOutput.write(byteArray);
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        int var3 = par1DataInput.readInt();
        byteArray = new byte[var3];
        par1DataInput.readFully(byteArray);
    }

    public byte getId() {
        return (byte) 7;
    }

    public String toString() {
        return "[" + byteArray.length + " bytes]";
    }

    public NBTBase copy() {
        byte[] var1 = new byte[byteArray.length];
        System.arraycopy(byteArray, 0, var1, 0, byteArray.length);
        return new NBTTagByteArray(getName(), var1);
    }

    public boolean equals(Object par1Obj) {
        return super.equals(par1Obj) && Arrays.equals(byteArray, ((NBTTagByteArray) par1Obj).byteArray);
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(byteArray);
    }
}
