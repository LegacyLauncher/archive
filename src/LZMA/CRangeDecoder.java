package LZMA;

import java.io.IOException;
import java.io.InputStream;

class CRangeDecoder {
   static final int kNumTopBits = 24;
   static final int kTopValue = 16777216;
   static final int kTopValueMask = -16777216;
   static final int kNumBitModelTotalBits = 11;
   static final int kBitModelTotal = 2048;
   static final int kNumMoveBits = 5;
   InputStream inStream;
   int Range;
   int Code;
   byte[] buffer = new byte[16384];
   int buffer_size;
   int buffer_ind;
   static final int kNumPosBitsMax = 4;
   static final int kNumPosStatesMax = 16;
   static final int kLenNumLowBits = 3;
   static final int kLenNumLowSymbols = 8;
   static final int kLenNumMidBits = 3;
   static final int kLenNumMidSymbols = 8;
   static final int kLenNumHighBits = 8;
   static final int kLenNumHighSymbols = 256;
   static final int LenChoice = 0;
   static final int LenChoice2 = 1;
   static final int LenLow = 2;
   static final int LenMid = 130;
   static final int LenHigh = 258;
   static final int kNumLenProbs = 514;

   CRangeDecoder(InputStream iStream) throws IOException {
      this.inStream = iStream;
      this.Code = 0;
      this.Range = -1;

      for(int i = 0; i < 5; ++i) {
         this.Code = this.Code << 8 | this.Readbyte();
      }

   }

   int Readbyte() throws IOException {
      if (this.buffer_size == this.buffer_ind) {
         this.buffer_size = this.inStream.read(this.buffer);
         this.buffer_ind = 0;
         if (this.buffer_size < 1) {
            throw new LzmaException("LZMA : Data Error");
         }
      }

      return this.buffer[this.buffer_ind++] & 255;
   }

   int DecodeDirectBits(int numTotalBits) throws IOException {
      int result = 0;

      for(int i = numTotalBits; i > 0; --i) {
         this.Range >>>= 1;
         int t = this.Code - this.Range >>> 31;
         this.Code -= this.Range & t - 1;
         result = result << 1 | 1 - t;
         if (this.Range < 16777216) {
            this.Code = this.Code << 8 | this.Readbyte();
            this.Range <<= 8;
         }
      }

      return result;
   }

   int BitDecode(int[] prob, int index) throws IOException {
      int newBound = (this.Range >>> 11) * prob[index];
      if (((long)this.Code & 4294967295L) < ((long)newBound & 4294967295L)) {
         this.Range = newBound;
         prob[index] += 2048 - prob[index] >>> 5;
         if ((this.Range & -16777216) == 0) {
            this.Code = this.Code << 8 | this.Readbyte();
            this.Range <<= 8;
         }

         return 0;
      } else {
         this.Range -= newBound;
         this.Code -= newBound;
         prob[index] -= prob[index] >>> 5;
         if ((this.Range & -16777216) == 0) {
            this.Code = this.Code << 8 | this.Readbyte();
            this.Range <<= 8;
         }

         return 1;
      }
   }

   int BitTreeDecode(int[] probs, int index, int numLevels) throws IOException {
      int mi = 1;

      for(int i = numLevels; i > 0; --i) {
         mi = mi + mi + this.BitDecode(probs, index + mi);
      }

      return mi - (1 << numLevels);
   }

   int ReverseBitTreeDecode(int[] probs, int index, int numLevels) throws IOException {
      int mi = 1;
      int symbol = 0;

      for(int i = 0; i < numLevels; ++i) {
         int bit = this.BitDecode(probs, index + mi);
         mi = mi + mi + bit;
         symbol |= bit << i;
      }

      return symbol;
   }

   byte LzmaLiteralDecode(int[] probs, int index) throws IOException {
      int symbol = 1;

      do {
         symbol = symbol + symbol | this.BitDecode(probs, index + symbol);
      } while(symbol < 256);

      return (byte)symbol;
   }

   byte LzmaLiteralDecodeMatch(int[] probs, int index, byte matchbyte) throws IOException {
      int symbol = 1;

      do {
         int matchBit = matchbyte >> 7 & 1;
         matchbyte = (byte)(matchbyte << 1);
         int bit = this.BitDecode(probs, index + (1 + matchBit << 8) + symbol);
         symbol = symbol << 1 | bit;
         if (matchBit != bit) {
            while(symbol < 256) {
               symbol = symbol + symbol | this.BitDecode(probs, index + symbol);
            }

            return (byte)symbol;
         }
      } while(symbol < 256);

      return (byte)symbol;
   }

   int LzmaLenDecode(int[] probs, int index, int posState) throws IOException {
      if (this.BitDecode(probs, index + 0) == 0) {
         return this.BitTreeDecode(probs, index + 2 + (posState << 3), 3);
      } else {
         return this.BitDecode(probs, index + 1) == 0 ? 8 + this.BitTreeDecode(probs, index + 130 + (posState << 3), 3) : 16 + this.BitTreeDecode(probs, index + 258, 8);
      }
   }
}
