package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NBTTagList extends NBTBase {
    private List<NBTBase> tagList = new ArrayList<>();
    private byte tagType;

    public NBTTagList() {
        super("");
    }

    public NBTTagList(String par1Str) {
        super(par1Str);
    }

    void write(DataOutput par1DataOutput) throws IOException {
        if (!tagList.isEmpty()) {
            tagType = tagList.get(0).getId();
        } else {
            tagType = 1;
        }

        par1DataOutput.writeByte(tagType);
        par1DataOutput.writeInt(tagList.size());

        for (NBTBase nbtBase : tagList) {
            nbtBase.write(par1DataOutput);
        }
    }

    void load(DataInput par1DataInput, int par2) throws IOException {
        if (par2 > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        } else {
            tagType = par1DataInput.readByte();
            int var3 = par1DataInput.readInt();
            tagList = new ArrayList<>();

            for (int var4 = 0; var4 < var3; ++var4) {
                NBTBase var5 = NBTBase.newTag(tagType, null);
                var5.load(par1DataInput, par2 + 1);
                tagList.add(var5);
            }

        }
    }

    public byte getId() {
        return (byte) 9;
    }

    public String toString() {
        return tagList.size() + " entries of type " + NBTBase.getTagName(tagType);
    }

    public void appendTag(NBTBase par1NBTBase) {
        tagType = par1NBTBase.getId();
        tagList.add(par1NBTBase);
    }

    public NBTBase removeTag(int par1) {
        return tagList.remove(par1);
    }

    public NBTBase tagAt(int par1) {
        return tagList.get(par1);
    }

    public int tagCount() {
        return tagList.size();
    }

    public NBTBase copy() {
        NBTTagList var1 = new NBTTagList(getName());
        var1.tagType = tagType;

        for (NBTBase var3 : tagList) {
            NBTBase var4 = var3.copy();
            var1.tagList.add(var4);
        }

        return var1;
    }

    public boolean equals(Object par1Obj) {
        if (super.equals(par1Obj)) {
            NBTTagList var2 = (NBTTagList) par1Obj;
            if (tagType == var2.tagType) {
                return tagList.equals(var2.tagList);
            }
        }

        return false;
    }

    public int hashCode() {
        return super.hashCode() ^ tagList.hashCode();
    }
}
