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

   void write(DataOutput par1DataOutput) throws IOException {
      par1DataOutput.writeInt(this.byteArray.length);
      par1DataOutput.write(this.byteArray);
   }

   void load(DataInput par1DataInput, int par2) throws IOException {
      int var3 = par1DataInput.readInt();
      this.byteArray = new byte[var3];
      par1DataInput.readFully(this.byteArray);
   }

   public byte getId() {
      return 7;
   }

   public String toString() {
      return "[" + this.byteArray.length + " bytes]";
   }

   public boolean equals(Object par1Obj) {
      return super.equals(par1Obj) ? Arrays.equals(this.byteArray, ((NBTTagByteArray)par1Obj).byteArray) : false;
   }

   public int hashCode() {
      return super.hashCode() ^ Arrays.hashCode(this.byteArray);
   }
}
