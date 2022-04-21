package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

public class NBTTagIntArray extends NBTBase {
    public int[] intArray;

    public NBTTagIntArray(String par1Str) {
        super(par1Str);
    }

    public NBTTagIntArray(String par1Str, int[] par2ArrayOfInteger) {
        super(par1Str);
        intArray = par2ArrayOfInteger;
    }

    void write(DataOutput par1DataOutput) throws IOException {
        par1DataOutput.writeInt(intArray.length);

        for (int i : intArray) {
            par1DataOutput.writeInt(i);
        }

    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        int var3 = par1DataInput.readInt();
        intArray = new int[var3];

        for (int var4 = 0; var4 < var3; ++var4) {
            intArray[var4] = par1DataInput.readInt();
        }

    }

    public byte getId() {
        return (byte) 11;
    }

    public String toString() {
        return "[" + intArray.length + " bytes]";
    }

    public NBTBase copy() {
        int[] var1 = new int[intArray.length];
        System.arraycopy(intArray, 0, var1, 0, intArray.length);
        return new NBTTagIntArray(getName(), var1);
    }

    public boolean equals(Object par1Obj) {
        if (!super.equals(par1Obj)) {
            return false;
        } else {
            NBTTagIntArray var2 = (NBTTagIntArray) par1Obj;
            return intArray == null && var2.intArray == null || intArray != null && Arrays.equals(intArray, var2.intArray);
        }
    }

    public int hashCode() {
        return super.hashCode() ^ Arrays.hashCode(intArray);
    }
}
