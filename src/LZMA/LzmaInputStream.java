package LZMA;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class LzmaInputStream extends FilterInputStream {
	boolean isClosed;
	CRangeDecoder RangeDecoder;
	byte  [] dictionary;
	int dictionarySize;
	int dictionaryPos;
	int GlobalPos;
	int rep0, rep1, rep2, rep3;
	int lc;
	int lp;
	int pb;
	int State;
	boolean PreviousIsMatch;
	int RemainLen;
	int [] probs;

	byte [] uncompressed_buffer;
	int uncompressed_size;
	int uncompressed_offset;
	long GlobalNowPos;
	long GlobalOutSize;

	static final int LZMA_BASE_SIZE = 1846;
	static final int LZMA_LIT_SIZE  = 768;

	final static int kBlockSize = 0x10000;

	static final int kNumStates = 12;

	static final int kStartPosModelIndex = 4;
	static final int kEndPosModelIndex   = 14;
	static final int kNumFullDistances   = (1 << (kEndPosModelIndex >> 1));

	static final int kNumPosSlotBits    = 6;
	static final int kNumLenToPosStates = 4;

	static final int kNumAlignBits   = 4;
	static final int kAlignTableSize = (1 << kNumAlignBits);

	static final int kMatchMinLen = 2;

	static final int IsMatch     = 0;
	static final int IsRep       = (IsMatch + (kNumStates << CRangeDecoder.kNumPosBitsMax));
	static final int IsRepG0     = (IsRep + kNumStates);
	static final int IsRepG1     = (IsRepG0 + kNumStates);
	static final int IsRepG2     = (IsRepG1 + kNumStates);
	static final int IsRep0Long  = (IsRepG2 + kNumStates);
	static final int PosSlot     = (IsRep0Long + (kNumStates << CRangeDecoder.kNumPosBitsMax));
	static final int SpecPos     = (PosSlot + (kNumLenToPosStates << kNumPosSlotBits));
	static final int Align       = (SpecPos + kNumFullDistances - kEndPosModelIndex);
	static final int LenCoder    = (Align + kAlignTableSize);
	static final int RepLenCoder = (LenCoder + CRangeDecoder.kNumLenProbs);
	static final int Literal     = (RepLenCoder + CRangeDecoder.kNumLenProbs);

	public LzmaInputStream (InputStream in) throws IOException {
		super(in);

		isClosed = false;

		readHeader();

		fill_buffer();
	}

	private void LzmaDecode(int outSize)  throws IOException {
		byte previousbyte;
		int posStateMask = (1 << (pb)) - 1;
		int literalPosMask = (1 << (lp)) - 1;

		uncompressed_size = 0;

		if (RemainLen == -1) {
			return ;
		}

		while(RemainLen > 0 && uncompressed_size < outSize) {
			int pos = dictionaryPos - rep0;
			if (pos < 0)
				pos += dictionarySize;
			uncompressed_buffer[uncompressed_size++] = dictionary[dictionaryPos] = dictionary[pos];
			if (++dictionaryPos == dictionarySize)
				dictionaryPos = 0;
			RemainLen--;
		}
		if (dictionaryPos == 0)
			previousbyte = dictionary[dictionarySize - 1];
		else
			previousbyte = dictionary[dictionaryPos - 1];

		while(uncompressed_size < outSize) {
			int posState = (int)( (uncompressed_size + GlobalPos ) & posStateMask);

			if (RangeDecoder.BitDecode(probs, IsMatch + (State << CRangeDecoder.kNumPosBitsMax) + posState) == 0) {
				int ind_prob = Literal + (LZMA_LIT_SIZE * (((
				                                (uncompressed_size + GlobalPos )
				                                & literalPosMask) << lc) + ((previousbyte & 0xFF) >> (8 - lc))));

				if (State < 4)
					State = 0;
				else if (State < 10)
					State -= 3;
				else
					State -= 6;
				if (PreviousIsMatch) {
					int pos = dictionaryPos - rep0;
					if (pos < 0)
						pos += dictionarySize;
					byte matchbyte = dictionary[pos];

					previousbyte = RangeDecoder.LzmaLiteralDecodeMatch(probs,ind_prob, matchbyte);
					PreviousIsMatch = false;
				} else {
					previousbyte = RangeDecoder.LzmaLiteralDecode(probs,ind_prob);
				}

				uncompressed_buffer[uncompressed_size++] = previousbyte;

				dictionary[dictionaryPos] = previousbyte;
				if (++dictionaryPos == dictionarySize)
					dictionaryPos = 0;

			} else {
				PreviousIsMatch = true;
				if (RangeDecoder.BitDecode(probs, IsRep + State) == 1) {
					if (RangeDecoder.BitDecode(probs, IsRepG0 + State) == 0) {
						if (RangeDecoder.BitDecode(probs, IsRep0Long + (State << CRangeDecoder.kNumPosBitsMax) + posState) == 0) {

							if ((uncompressed_size + GlobalPos) == 0) {
								throw new LzmaException ("LZMA : Data Error");
							}
							State = State < 7 ? 9 : 11;

							int pos = dictionaryPos - rep0;
							if (pos < 0)
								pos += dictionarySize;
							previousbyte = dictionary[pos];
							dictionary[dictionaryPos] = previousbyte;
							if (++dictionaryPos == dictionarySize)
								dictionaryPos = 0;

							uncompressed_buffer[uncompressed_size++] = previousbyte;
							continue;
						}
					} else {
						int distance;
						if(RangeDecoder.BitDecode(probs, IsRepG1 + State) == 0)
							distance = rep1;
						else {
							if(RangeDecoder.BitDecode(probs, IsRepG2 + State) == 0)
								distance = rep2;
							else {
								distance = rep3;
								rep3 = rep2;
							}
							rep2 = rep1;
						}
						rep1 = rep0;
						rep0 = distance;
					}
					RemainLen = RangeDecoder.LzmaLenDecode(probs, RepLenCoder, posState);
					State = State < 7 ? 8 : 11;
				} else {
					rep3 = rep2;
					rep2 = rep1;
					rep1 = rep0;
					State = State < 7 ? 7 : 10;
					RemainLen = RangeDecoder.LzmaLenDecode(probs, LenCoder, posState);
					int posSlot = RangeDecoder.BitTreeDecode(probs , PosSlot +
					                                     ((RemainLen < kNumLenToPosStates ? RemainLen : kNumLenToPosStates - 1) <<
					                                      kNumPosSlotBits), kNumPosSlotBits);
					if (posSlot >= kStartPosModelIndex) {
						int numDirectBits = ((posSlot >> 1) - 1);
						rep0 = ((2 | (posSlot & 1)) << numDirectBits);
						if (posSlot < kEndPosModelIndex) {
							rep0 += RangeDecoder.ReverseBitTreeDecode(
							            probs, SpecPos + rep0 - posSlot - 1, numDirectBits);
						} else {
							rep0 += RangeDecoder.DecodeDirectBits(
							            numDirectBits - kNumAlignBits) << kNumAlignBits;
							rep0 += RangeDecoder.ReverseBitTreeDecode(probs, Align, kNumAlignBits);
						}
					} else
						rep0 = posSlot;
					rep0++;
				}
				if (rep0 == 0) {

					RemainLen = -1;
					break;
				}
				if (rep0 > uncompressed_size

				        + GlobalPos

				   ) {
					throw new LzmaException ("LZMA : Data Error");
				}
				RemainLen += kMatchMinLen;
				do {

					int pos = dictionaryPos - rep0;
					if (pos < 0)
						pos += dictionarySize;
					previousbyte = dictionary[pos];
					dictionary[dictionaryPos] = previousbyte;
					if (++dictionaryPos == dictionarySize)
						dictionaryPos = 0;

					uncompressed_buffer[uncompressed_size++] = previousbyte;
					RemainLen--;
				} while(RemainLen > 0 && uncompressed_size < outSize);
			}
		}

		GlobalPos = GlobalPos + uncompressed_size;
	}

	private void fill_buffer() throws IOException {
		if (GlobalNowPos < GlobalOutSize) {
			uncompressed_offset = 0;
			long lblockSize = GlobalOutSize - GlobalNowPos;
			int blockSize;
			if (lblockSize > kBlockSize)
				blockSize = kBlockSize;
			else
				blockSize = (int)lblockSize;

			LzmaDecode(blockSize);

			if (uncompressed_size == 0) {
				GlobalOutSize = GlobalNowPos;
			} else {
				GlobalNowPos += uncompressed_size;
			}
		}
	}

	private void readHeader() throws IOException {
		byte [] properties = new byte[5];

		if (5 != in.read(properties))
			throw new LzmaException ("LZMA header corrupted : Properties error");

		GlobalOutSize = 0;
		for (int ii = 0; ii < 8; ii++) {
			int b = in.read();
			if (b == -1)
				throw new LzmaException ("LZMA header corrupted : Size error");
			GlobalOutSize += ((long)b) << (ii * 8);
		}

                if (GlobalOutSize == -1) GlobalOutSize=Long.MAX_VALUE;

		int prop0 = properties[0] & 0xFF;
		if (prop0 >= (9*5*5)) {
			throw new LzmaException ("LZMA header corrupted : Properties error");
		}

		for (pb = 0; prop0 >= (9 * 5); pb++, prop0 -= (9 * 5))
			;
		for (lp = 0; prop0 >= 9; lp++, prop0 -= 9)
			;
		lc = prop0;

		int lzmaInternalSize = (LZMA_BASE_SIZE + (LZMA_LIT_SIZE << (lc + lp)));

		probs = new int[lzmaInternalSize];

		dictionarySize = 0;
		for (int i = 0; i < 4; i++)
			dictionarySize += (properties[1 + i]&0xFF) << (i * 8);
		dictionary = new byte[dictionarySize];
		if (dictionary == null) {
			throw new LzmaException ("LZMA : can't allocate");
		}

		int numProbs = Literal + (LZMA_LIT_SIZE << (lc + lp));

		RangeDecoder = new CRangeDecoder(in);
		dictionaryPos = 0;
		GlobalPos = 0;
		rep0 = rep1 = rep2 = rep3 = 1;
		State = 0;
		PreviousIsMatch = false;
		RemainLen = 0;
		dictionary[dictionarySize - 1] = 0;
		for (int i = 0; i < numProbs; i++)
			probs[i] = CRangeDecoder.kBitModelTotal >> 1;

		uncompressed_buffer = new byte [kBlockSize];
		uncompressed_size = 0;
		uncompressed_offset = 0;

		GlobalNowPos = 0;
	}

	public int read (byte[] buf, int off, int len) throws IOException {
		if (isClosed)
			throw new IOException ("stream closed");

		if ((off | len | (off + len) | (buf.length - (off + len))) < 0) {
			throw new IndexOutOfBoundsException();
		}
		if (len == 0)
			return 0;

		if (uncompressed_offset == uncompressed_size)
			fill_buffer();
		if (uncompressed_offset == uncompressed_size)
			return -1;

		int l = Math.min(len,uncompressed_size-uncompressed_offset);
		System.arraycopy (uncompressed_buffer, uncompressed_offset, buf, off, l);
		uncompressed_offset += l;
		return l;
	}

	public void close () throws IOException {
		isClosed = true;
		super.close ();
	}
}
