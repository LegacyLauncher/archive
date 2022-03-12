package net.minecraft.common;

import ru.turikhay.util.U;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NBTTagCompound extends NBTBase {
    private final Map<String, NBTBase> tagMap = new HashMap<>();

    public NBTTagCompound() {
        super("");
    }

    public NBTTagCompound(String par1Str) {
        super(par1Str);
    }

    void write(DataOutput dataOutput) throws IOException {

        for (NBTBase tag : tagMap.values()) {
            NBTBase.writeNamedTag(tag, dataOutput);
        }

        dataOutput.writeByte(0);
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        if (par2 > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            tagMap.clear();

            NBTBase var3;
            while ((var3 = NBTBase.func_130104_b(par1DataInput, par2 + 1)).getId() != 0) {
                tagMap.put(var3.getName(), var3);
            }

        }
    }

    public Collection<NBTBase> getTags() {
        return tagMap.values();
    }

    public byte getId() {
        return (byte) 10;
    }

    public void setTag(String par1Str, NBTBase par2NBTBase) {
        tagMap.put(par1Str, par2NBTBase.setName(par1Str));
    }

    void setByte(String par1Str, byte par2) {
        tagMap.put(par1Str, new NBTTagByte(par1Str, par2));
    }

    public void setShort(String par1Str, short par2) {
        tagMap.put(par1Str, new NBTTagShort(par1Str, par2));
    }

    public void setInteger(String par1Str, int par2) {
        tagMap.put(par1Str, new NBTTagInt(par1Str, par2));
    }

    public void setLong(String par1Str, long par2) {
        tagMap.put(par1Str, new NBTTagLong(par1Str, par2));
    }

    public void setFloat(String par1Str, float par2) {
        tagMap.put(par1Str, new NBTTagFloat(par1Str, par2));
    }

    public void setDouble(String par1Str, double par2) {
        tagMap.put(par1Str, new NBTTagDouble(par1Str, par2));
    }

    public void setString(String par1Str, String par2Str) {
        tagMap.put(par1Str, new NBTTagString(par1Str, par2Str));
    }

    public void setByteArray(String par1Str, byte[] par2ArrayOfByte) {
        tagMap.put(par1Str, new NBTTagByteArray(par1Str, par2ArrayOfByte));
    }

    public void setIntArray(String par1Str, int[] par2ArrayOfInteger) {
        tagMap.put(par1Str, new NBTTagIntArray(par1Str, par2ArrayOfInteger));
    }

    public void setCompoundTag(String par1Str, NBTTagCompound par2NBTTagCompound) {
        tagMap.put(par1Str, par2NBTTagCompound.setName(par1Str));
    }

    public void setBoolean(String par1Str, boolean par2) {
        setByte(par1Str, (byte) (par2 ? 1 : 0));
    }

    public NBTBase getTag(String par1Str) {
        return tagMap.get(par1Str);
    }

    public boolean hasKey(String par1Str) {
        return tagMap.containsKey(par1Str);
    }

    byte getByte(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0 : ((NBTTagByte) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 1, var3));
        }
    }

    public short getShort(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0 : ((NBTTagShort) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 2, var3));
        }
    }

    public int getInteger(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0 : ((NBTTagInt) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 3, var3));
        }
    }

    public long getLong(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0L : ((NBTTagLong) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 4, var3));
        }
    }

    public float getFloat(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0.0F : ((NBTTagFloat) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 5, var3));
        }
    }

    public double getDouble(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? 0.0D : ((NBTTagDouble) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 6, var3));
        }
    }

    public String getString(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? "" : ((NBTTagString) tagMap.get(par1Str)).data;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 8, var3));
        }
    }

    public byte[] getByteArray(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? new byte[0] : ((NBTTagByteArray) tagMap.get(par1Str)).byteArray;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 7, var3));
        }
    }

    public int[] getIntArray(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? new int[0] : ((NBTTagIntArray) tagMap.get(par1Str)).intArray;
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 11, var3));
        }
    }

    public NBTTagCompound getCompoundTag(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? new NBTTagCompound(par1Str) : (NBTTagCompound) tagMap.get(par1Str);
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 10, var3));
        }
    }

    public NBTTagList getTagList(String par1Str) {
        try {
            return !tagMap.containsKey(par1Str) ? new NBTTagList(par1Str) : (NBTTagList) tagMap.get(par1Str);
        } catch (ClassCastException var3) {
            throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 9, var3));
        }
    }

    public boolean getBoolean(String par1Str) {
        return getByte(par1Str) != 0;
    }

    public void removeTag(String par1Str) {
        tagMap.remove(par1Str);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(getName());
        result.append(":[");

        for (Map.Entry<String, NBTBase> entry : tagMap.entrySet()) {
            result.append(entry.getKey()).append(':').append(entry.getValue()).append(',');
        }

        if (result.charAt(result.length() - 1) == ',') {
            result.setCharAt(result.length() - 1, ']');
        } else {
            result.append(']');
        }

        return result.toString();
    }

    public boolean hasNoTags() {
        return tagMap.isEmpty();
    }

    public NBTBase copy() {
        NBTTagCompound var1 = new NBTTagCompound(getName());

        for (String var3 : tagMap.keySet()) {
            var1.setTag(var3, tagMap.get(var3).copy());
        }

        return var1;
    }

    public boolean equals(Object par1Obj) {
        if (super.equals(par1Obj)) {
            NBTTagCompound var2 = (NBTTagCompound) par1Obj;
            return tagMap.entrySet().equals(var2.tagMap.entrySet());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return super.hashCode() ^ tagMap.hashCode();
    }

    static Map<String, NBTBase> getTagMap(NBTTagCompound par0NBTTagCompound) {
        return par0NBTTagCompound.tagMap;
    }
}
