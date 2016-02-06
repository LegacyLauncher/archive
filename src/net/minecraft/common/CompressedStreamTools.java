package net.minecraft.common;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class CompressedStreamTools {
   public static void safeWrite(NBTTagCompound par0NBTTagCompound, File par1File) throws IOException {
      File var2 = new File(par1File.getAbsolutePath() + "_tmp");
      if (var2.exists()) {
         var2.delete();
      }

      write(par0NBTTagCompound, var2);
      if (par1File.exists()) {
         par1File.delete();
      }

      if (par1File.exists()) {
         throw new IOException("Failed to delete " + par1File);
      } else {
         var2.renameTo(par1File);
      }
   }

   private static void write(NBTTagCompound par0NBTTagCompound, File par1File) throws IOException {
      DataOutputStream var2 = new DataOutputStream(new FileOutputStream(par1File));

      try {
         write(par0NBTTagCompound, (DataOutput)var2);
      } finally {
         var2.close();
      }

   }

   public static NBTTagCompound read(File par0File) throws IOException {
      if (!par0File.exists()) {
         return null;
      } else {
         DataInputStream var1 = new DataInputStream(new FileInputStream(par0File));

         NBTTagCompound var2;
         try {
            var2 = read((DataInput)var1);
         } finally {
            var1.close();
         }

         return var2;
      }
   }

   public static NBTTagCompound read(DataInput par0DataInput) throws IOException {
      NBTBase var1 = NBTBase.readNamedTag(par0DataInput);
      if (var1 instanceof NBTTagCompound) {
         return (NBTTagCompound)var1;
      } else {
         throw new IOException("Root tag must be a named compound tag");
      }
   }

   public static void write(NBTTagCompound par0NBTTagCompound, DataOutput par1DataOutput) throws IOException {
      NBTBase.writeNamedTag(par0NBTTagCompound, par1DataOutput);
   }
}
