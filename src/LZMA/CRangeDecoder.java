package LZMA;

import java.io.InputStream;
import java.io.IOException;

class CRangeDecoder {

	final static int kNumTopBits = 24;
	final static int kTopValue = (1 << kNumTopBits);
	final static int kTopValueMask = ~(kTopValue-1);

	final static int kNumBitModelTotalBits = 11;
	final static int kBitModelTotal = (1 << kNumBitModelTotalBits);
	final static int kNumMoveBits = 5;

	InputStream inStream;

	int Range;
	int Code;

	byte buffer[];
	int buffer_size;
	int buffer_ind;

	CRangeDecoder( InputStream iStream ) throws IOException {
		this.buffer = new byte[1<<14];
		this.inStream = iStream;
		this.Code = 0;
		this.Range = -1; // 0xFFFFFFFFL;
		for(int i = 0; i < 5; i++)
			this.Code = (this.Code << 8) | (Readbyte());
	}

	int Readbyte() throws IOException {
		if (buffer_size == buffer_ind) {
			buffer_size = inStream.read(buffer);
			buffer_ind = 0;

			if (buffer_size < 1)
				throw new LzmaException ("LZMA : Data Error");
		}
		return buffer[buffer_ind++] & 0xFF;
	}

	int DecodeDirectBits(int numTotalBits) throws IOException {
		int result = 0;
		for (int i = numTotalBits; i > 0; i--) {
			Range >>>= 1;
			int t = ((Code - Range) >>> 31);
			Code -= Range & (t - 1);
			result = (result << 1) | (1 - t);
            
			if (Range < kTopValue) // because of "Range >>>= 1",   0 <= Range <= 0x7FFFFFFF
			{
				Code = (Code << 8) | Readbyte();
				Range <<= 8;
			}
		}
		return result;
	}

	int BitDecode(int prob[],int index) throws IOException {
		int newBound = (this.Range >>> kNumBitModelTotalBits) * prob[index];
		if ((this.Code & 0xFFFFFFFFL) < (newBound & 0xFFFFFFFFL)) // unsigned comparison
		{
			this.Range = newBound;
			prob[index] += (kBitModelTotal - prob[index]) >>> kNumMoveBits;
			// if ((this.Range & 0xFFFFFFFFL) < Decoder.kTopValue)
			if ((this.Range & kTopValueMask) == 0) {
				this.Code = (this.Code << 8) | Readbyte();
			this.Range <<= 8;
			}
			return 0;
		} else {
			this.Range -= newBound;
			this.Code -= newBound;
			prob[index] -= (prob[index]) >>> kNumMoveBits;
			// if ((this.Range & 0xFFFFFFFFL) < Decoder.kTopValue)
			if ((this.Range & kTopValueMask) == 0) {
				this.Code = (this.Code << 8) | this.Readbyte();
				this.Range <<= 8;
			}
			return 1;
		}
	}

	int BitTreeDecode(int probs [], int index , int numLevels) throws IOException {
		int mi = 1;
		for(int i = numLevels; i > 0; i--) {
			mi = (mi + mi) + BitDecode(probs, index + mi);
		}
		return mi - (1 << numLevels);
	}

	int ReverseBitTreeDecode(int probs[] ,int index, int numLevels) throws IOException {
		int mi = 1;
		int symbol = 0;

		for(int i = 0; i < numLevels; i++) {
			int bit = BitDecode(probs, index + mi);
			mi = mi + mi + bit;
			symbol |= (bit << i);
		}
		return symbol;
	}

	byte LzmaLiteralDecode(int probs[],int index) throws IOException {
		int symbol = 1;
		do {
			symbol = (symbol + symbol) | BitDecode(probs, index + symbol);
		} while (symbol < 0x100);

		return (byte)symbol;
	}

	byte LzmaLiteralDecodeMatch(int probs [], int index, byte matchbyte) throws IOException {
		int symbol = 1;
		do {
			int matchBit = (matchbyte >> 7) & 1;
			matchbyte <<= 1;
			int bit = BitDecode(probs , index + ((1 + matchBit) << 8) + symbol);
			symbol = (symbol << 1) | bit;

			if (matchBit != bit) {
				while (symbol < 0x100) {
					symbol = (symbol + symbol) | BitDecode(probs , index + symbol);
				}
				break;
			}
		} while (symbol < 0x100);

		return (byte)symbol;
	}

	final static int kNumPosBitsMax = 4;
	final static int kNumPosStatesMax = (1 << kNumPosBitsMax);

	final static int kLenNumLowBits = 3;
	final static int kLenNumLowSymbols = (1 << kLenNumLowBits);
	final static int kLenNumMidBits = 3;
	final static int kLenNumMidSymbols = (1 << kLenNumMidBits);
	final static int kLenNumHighBits = 8;
	final static int kLenNumHighSymbols = (1 << kLenNumHighBits);

	final static int LenChoice = 0;
	final static int LenChoice2 = (LenChoice + 1);
	final static int LenLow = (LenChoice2 + 1);
	final static int LenMid = (LenLow + (kNumPosStatesMax << kLenNumLowBits));
	final static int LenHigh = (LenMid + (kNumPosStatesMax << kLenNumMidBits));
	final static int kNumLenProbs = (LenHigh + kLenNumHighSymbols);

	int LzmaLenDecode(int probs[], int index, int posState) throws IOException {
		if(BitDecode(probs, index + LenChoice) == 0)
			return BitTreeDecode(probs, index + LenLow +
			                     (posState << kLenNumLowBits), kLenNumLowBits);

		if(BitDecode(probs, index + LenChoice2) == 0)
			return kLenNumLowSymbols + BitTreeDecode(probs, index + LenMid +
			        (posState << kLenNumMidBits), kLenNumMidBits);

		return kLenNumLowSymbols + kLenNumMidSymbols +
		       BitTreeDecode(probs, index + LenHigh, kLenNumHighBits);
	}
}
