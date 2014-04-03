package net.minecraft.common;

import com.turikhay.util.U;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NBTTagCompound extends NBTBase {
   private Map tagMap = new HashMap();

   public NBTTagCompound() {
      super("");
   }

   public NBTTagCompound(String par1Str) {
      super(par1Str);
   }

   void write(DataOutput par1DataOutput) throws IOException {
      Iterator var2 = this.tagMap.values().iterator();

      while(var2.hasNext()) {
         NBTBase var3 = (NBTBase)var2.next();
         NBTBase.writeNamedTag(var3, par1DataOutput);
      }

      par1DataOutput.writeByte(0);
   }

   void load(DataInput par1DataInput, int par2) throws IOException {
      if (par2 > 512) {
         throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
      } else {
         this.tagMap.clear();

         NBTBase var3;
         while((var3 = NBTBase.func_130104_b(par1DataInput, par2 + 1)).getId() != 0) {
            this.tagMap.put(var3.getName(), var3);
         }

      }
   }

   public Collection getTags() {
      return this.tagMap.values();
   }

   public byte getId() {
      return 10;
   }

   public void setTag(String par1Str, NBTBase par2NBTBase) {
      this.tagMap.put(par1Str, par2NBTBase.setName(par1Str));
   }

   void setByte(String par1Str, byte par2) {
      this.tagMap.put(par1Str, new NBTTagByte(par1Str, par2));
   }

   public void setShort(String par1Str, short par2) {
      this.tagMap.put(par1Str, new NBTTagShort(par1Str, par2));
   }

   public void setInteger(String par1Str, int par2) {
      this.tagMap.put(par1Str, new NBTTagInt(par1Str, par2));
   }

   public void setLong(String par1Str, long par2) {
      this.tagMap.put(par1Str, new NBTTagLong(par1Str, par2));
   }

   public void setFloat(String par1Str, float par2) {
      this.tagMap.put(par1Str, new NBTTagFloat(par1Str, par2));
   }

   public void setDouble(String par1Str, double par2) {
      this.tagMap.put(par1Str, new NBTTagDouble(par1Str, par2));
   }

   public void setString(String par1Str, String par2Str) {
      this.tagMap.put(par1Str, new NBTTagString(par1Str, par2Str));
   }

   public void setByteArray(String par1Str, byte[] par2ArrayOfByte) {
      this.tagMap.put(par1Str, new NBTTagByteArray(par1Str, par2ArrayOfByte));
   }

   public void setIntArray(String par1Str, int[] par2ArrayOfInteger) {
      this.tagMap.put(par1Str, new NBTTagIntArray(par1Str, par2ArrayOfInteger));
   }

   public void setCompoundTag(String par1Str, NBTTagCompound par2NBTTagCompound) {
      this.tagMap.put(par1Str, par2NBTTagCompound.setName(par1Str));
   }

   public void setBoolean(String par1Str, boolean par2) {
      this.setByte(par1Str, (byte)(par2 ? 1 : 0));
   }

   public NBTBase getTag(String par1Str) {
      return (NBTBase)this.tagMap.get(par1Str);
   }

   public boolean hasKey(String par1Str) {
      return this.tagMap.containsKey(par1Str);
   }

   byte getByte(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0 : ((NBTTagByte)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 1, var3));
      }
   }

   public short getShort(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0 : ((NBTTagShort)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 2, var3));
      }
   }

   public int getInteger(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0 : ((NBTTagInt)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 3, var3));
      }
   }

   public long getLong(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0L : ((NBTTagLong)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 4, var3));
      }
   }

   public float getFloat(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0.0F : ((NBTTagFloat)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 5, var3));
      }
   }

   public double getDouble(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? 0.0D : ((NBTTagDouble)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 6, var3));
      }
   }

   public String getString(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? "" : ((NBTTagString)this.tagMap.get(par1Str)).data;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 8, var3));
      }
   }

   public byte[] getByteArray(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? new byte[0] : ((NBTTagByteArray)this.tagMap.get(par1Str)).byteArray;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 7, var3));
      }
   }

   public int[] getIntArray(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? new int[0] : ((NBTTagIntArray)this.tagMap.get(par1Str)).intArray;
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 11, var3));
      }
   }

   public NBTTagCompound getCompoundTag(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? new NBTTagCompound(par1Str) : (NBTTagCompound)this.tagMap.get(par1Str);
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 10, var3));
      }
   }

   public NBTTagList getTagList(String par1Str) {
      try {
         return !this.tagMap.containsKey(par1Str) ? new NBTTagList(par1Str) : (NBTTagList)this.tagMap.get(par1Str);
      } catch (ClassCastException var3) {
         throw new RuntimeException("Error parsing NBT:" + U.toLog(par1Str, 9, var3));
      }
   }

   public boolean getBoolean(String par1Str) {
      return this.getByte(par1Str) != 0;
   }

   public void removeTag(String par1Str) {
      this.tagMap.remove(par1Str);
   }

   public String toString() {
      String var1 = this.getName() + ":[";

      String var3;
      for(Iterator var2 = this.tagMap.keySet().iterator(); var2.hasNext(); var1 = var1 + var3 + ":" + this.tagMap.get(var3) + ",") {
         var3 = (String)var2.next();
      }

      return var1 + "]";
   }

   public boolean hasNoTags() {
      return this.tagMap.isEmpty();
   }

   public NBTBase copy() {
      NBTTagCompound var1 = new NBTTagCompound(this.getName());
      Iterator var2 = this.tagMap.keySet().iterator();

      while(var2.hasNext()) {
         String var3 = (String)var2.next();
         var1.setTag(var3, ((NBTBase)this.tagMap.get(var3)).copy());
      }

      return var1;
   }

   public boolean equals(Object par1Obj) {
      if (super.equals(par1Obj)) {
         NBTTagCompound var2 = (NBTTagCompound)par1Obj;
         return this.tagMap.entrySet().equals(var2.tagMap.entrySet());
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ this.tagMap.hashCode();
   }

   static Map getTagMap(NBTTagCompound par0NBTTagCompound) {
      return par0NBTTagCompound.tagMap;
   }
}
