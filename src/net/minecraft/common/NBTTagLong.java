package net.minecraft.common;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagLong extends NBTBase {
	/** The long value for the tag. */
	public long data;

	public NBTTagLong(String par1Str) {
		super(par1Str);
	}

	public NBTTagLong(String par1Str, long par2) {
		super(par1Str);
		this.data = par2;
	}

	/**
	 * Write the actual data contents of the tag, implemented in NBT extension
	 * classes
	 */
	@Override
	void write(DataOutput par1DataOutput) throws IOException {
		par1DataOutput.writeLong(this.data);
	}

	/**
	 * Read the actual data contents of the tag, implemented in NBT extension
	 * classes
	 */
	@Override
	void load(DataInput par1DataInput, int par2) throws IOException {
		this.data = par1DataInput.readLong();
	}

	/**
	 * Gets the type byte for the tag.
	 */
	@Override
	public byte getId() {
		return (byte) 4;
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
		return new NBTTagLong(this.getName(), this.data);
	}

	@Override
	public boolean equals(Object par1Obj) {
		if (super.equals(par1Obj)) {
			NBTTagLong var2 = (NBTTagLong) par1Obj;
			return this.data == var2.data;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ (int) (this.data ^ this.data >>> 32);
	}
}
