package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagDouble extends NBTBase {
	/** The double value for the tag. */
	public double data;

	public NBTTagDouble(String par1Str) {
		super(par1Str);
	}

	public NBTTagDouble(String par1Str, double par2) {
		super(par1Str);
		this.data = par2;
	}

	/**
	 * Write the actual data contents of the tag, implemented in NBT extension
	 * classes
	 */
	@Override
	void write(DataOutput par1DataOutput) throws IOException {
		par1DataOutput.writeDouble(this.data);
	}

	/**
	 * Read the actual data contents of the tag, implemented in NBT extension
	 * classes
	 */
	@Override
	void load(DataInput par1DataInput, int par2) throws IOException {
		this.data = par1DataInput.readDouble();
	}

	/**
	 * Gets the type byte for the tag.
	 */
	@Override
	public byte getId() {
		return (byte) 6;
	}

	@Override
	public String toString() {
		return "" + this.data;
	}

	/**
	 * Creates a clone of the tag.
	 */
	@Override
	public NBTBase copy() {
		return new NBTTagDouble(this.getName(), this.data);
	}

	@Override
	public boolean equals(Object par1Obj) {
		if (super.equals(par1Obj)) {
			NBTTagDouble var2 = (NBTTagDouble) par1Obj;
			return this.data == var2.data;
		}
		return false;
	}

	@Override
	public int hashCode() {
		long var1 = Double.doubleToLongBits(this.data);
		return super.hashCode() ^ (int) (var1 ^ var1 >>> 32);
	}
}
