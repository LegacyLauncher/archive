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
      this.data = par2;
   }

   void write(DataOutput par1DataOutput) throws IOException {
      par1DataOutput.writeLong(this.data);
   }

   void load(DataInput par1DataInput, int par2) throws IOException {
      this.data = par1DataInput.readLong();
   }

   public byte getId() {
      return 4;
   }

   public String toString() {
      return "" + this.data;
   }

   public NBTBase copy() {
      return new NBTTagLong(this.getName(), this.data);
   }

   public boolean equals(Object par1Obj) {
      if (super.equals(par1Obj)) {
         NBTTagLong var2 = (NBTTagLong)par1Obj;
         return this.data == var2.data;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return super.hashCode() ^ (int)(this.data ^ this.data >>> 32);
   }
}
