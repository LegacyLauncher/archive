package LZMA;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

class LzmaInputStream extends FilterInputStream {
   private boolean isClosed = false;
   private CRangeDecoder RangeDecoder;
   private byte[] dictionary;
   private int dictionarySize;
   private int dictionaryPos;
   private int GlobalPos;
   private int rep0;
   private int rep1;
   private int rep2;
   private int rep3;
   private int lc;
   private int lp;
   private int pb;
   private int State;
   private boolean PreviousIsMatch;
   private int RemainLen;
   private int[] probs;
   private byte[] uncompressed_buffer;
   private int uncompressed_size;
   private int uncompressed_offset;
   private long GlobalNowPos;
   private long GlobalOutSize;
   private static final int LZMA_BASE_SIZE = 1846;
   private static final int LZMA_LIT_SIZE = 768;
   private static final int kBlockSize = 65536;
   private static final int kNumStates = 12;
   private static final int kStartPosModelIndex = 4;
   private static final int kEndPosModelIndex = 14;
   private static final int kNumFullDistances = 128;
   private static final int kNumPosSlotBits = 6;
   private static final int kNumLenToPosStates = 4;
   private static final int kNumAlignBits = 4;
   private static final int kAlignTableSize = 16;
   private static final int kMatchMinLen = 2;
   private static final int IsMatch = 0;
   private static final int IsRep = 192;
   private static final int IsRepG0 = 204;
   private static final int IsRepG1 = 216;
   private static final int IsRepG2 = 228;
   private static final int IsRep0Long = 240;
   private static final int PosSlot = 432;
   private static final int SpecPos = 688;
   private static final int Align = 802;
   private static final int LenCoder = 818;
   private static final int RepLenCoder = 1332;
   private static final int Literal = 1846;

   public LzmaInputStream(InputStream in) throws IOException {
      super(in);
      this.readHeader();
      this.fill_buffer();
   }

   private void LzmaDecode(int outSize) throws IOException {
      int posStateMask = (1 << this.pb) - 1;
      int literalPosMask = (1 << this.lp) - 1;
      this.uncompressed_size = 0;
      if (this.RemainLen != -1) {
         int posState;
         for(; this.RemainLen > 0 && this.uncompressed_size < outSize; --this.RemainLen) {
            posState = this.dictionaryPos - this.rep0;
            if (posState < 0) {
               posState += this.dictionarySize;
            }

            this.uncompressed_buffer[this.uncompressed_size++] = this.dictionary[this.dictionaryPos] = this.dictionary[posState];
            if (++this.dictionaryPos == this.dictionarySize) {
               this.dictionaryPos = 0;
            }
         }

         byte previousbyte;
         if (this.dictionaryPos == 0) {
            previousbyte = this.dictionary[this.dictionarySize - 1];
         } else {
            previousbyte = this.dictionary[this.dictionaryPos - 1];
         }

         while(this.uncompressed_size < outSize) {
            posState = this.uncompressed_size + this.GlobalPos & posStateMask;
            int pos;
            int pos;
            if (this.RangeDecoder.BitDecode(this.probs, 0 + (this.State << 4) + posState) == 0) {
               pos = 1846 + 768 * (((this.uncompressed_size + this.GlobalPos & literalPosMask) << this.lc) + ((previousbyte & 255) >> 8 - this.lc));
               if (this.State < 4) {
                  this.State = 0;
               } else if (this.State < 10) {
                  this.State -= 3;
               } else {
                  this.State -= 6;
               }

               if (this.PreviousIsMatch) {
                  pos = this.dictionaryPos - this.rep0;
                  if (pos < 0) {
                     pos += this.dictionarySize;
                  }

                  byte matchbyte = this.dictionary[pos];
                  previousbyte = this.RangeDecoder.LzmaLiteralDecodeMatch(this.probs, pos, matchbyte);
                  this.PreviousIsMatch = false;
               } else {
                  previousbyte = this.RangeDecoder.LzmaLiteralDecode(this.probs, pos);
               }

               this.uncompressed_buffer[this.uncompressed_size++] = previousbyte;
               this.dictionary[this.dictionaryPos] = previousbyte;
               if (++this.dictionaryPos == this.dictionarySize) {
                  this.dictionaryPos = 0;
               }
            } else {
               this.PreviousIsMatch = true;
               if (this.RangeDecoder.BitDecode(this.probs, 192 + this.State) == 1) {
                  if (this.RangeDecoder.BitDecode(this.probs, 204 + this.State) == 0) {
                     if (this.RangeDecoder.BitDecode(this.probs, 240 + (this.State << 4) + posState) == 0) {
                        if (this.uncompressed_size + this.GlobalPos == 0) {
                           throw new LzmaException("LZMA : Data Error");
                        }

                        this.State = this.State < 7 ? 9 : 11;
                        pos = this.dictionaryPos - this.rep0;
                        if (pos < 0) {
                           pos += this.dictionarySize;
                        }

                        previousbyte = this.dictionary[pos];
                        this.dictionary[this.dictionaryPos] = previousbyte;
                        if (++this.dictionaryPos == this.dictionarySize) {
                           this.dictionaryPos = 0;
                        }

                        this.uncompressed_buffer[this.uncompressed_size++] = previousbyte;
                        continue;
                     }
                  } else {
                     if (this.RangeDecoder.BitDecode(this.probs, 216 + this.State) == 0) {
                        pos = this.rep1;
                     } else {
                        if (this.RangeDecoder.BitDecode(this.probs, 228 + this.State) == 0) {
                           pos = this.rep2;
                        } else {
                           pos = this.rep3;
                           this.rep3 = this.rep2;
                        }

                        this.rep2 = this.rep1;
                     }

                     this.rep1 = this.rep0;
                     this.rep0 = pos;
                  }

                  this.RemainLen = this.RangeDecoder.LzmaLenDecode(this.probs, 1332, posState);
                  this.State = this.State < 7 ? 8 : 11;
               } else {
                  this.rep3 = this.rep2;
                  this.rep2 = this.rep1;
                  this.rep1 = this.rep0;
                  this.State = this.State < 7 ? 7 : 10;
                  this.RemainLen = this.RangeDecoder.LzmaLenDecode(this.probs, 818, posState);
                  pos = this.RangeDecoder.BitTreeDecode(this.probs, 432 + ((this.RemainLen < 4 ? this.RemainLen : 3) << 6), 6);
                  if (pos >= 4) {
                     pos = (pos >> 1) - 1;
                     this.rep0 = (2 | pos & 1) << pos;
                     if (pos < 14) {
                        this.rep0 += this.RangeDecoder.ReverseBitTreeDecode(this.probs, 688 + this.rep0 - pos - 1, pos);
                     } else {
                        this.rep0 += this.RangeDecoder.DecodeDirectBits(pos - 4) << 4;
                        this.rep0 += this.RangeDecoder.ReverseBitTreeDecode(this.probs, 802, 4);
                     }
                  } else {
                     this.rep0 = pos;
                  }

                  ++this.rep0;
               }

               if (this.rep0 == 0) {
                  this.RemainLen = -1;
                  break;
               }

               if (this.rep0 > this.uncompressed_size + this.GlobalPos) {
                  throw new LzmaException("LZMA : Data Error");
               }

               this.RemainLen += 2;

               while(true) {
                  pos = this.dictionaryPos - this.rep0;
                  if (pos < 0) {
                     pos += this.dictionarySize;
                  }

                  previousbyte = this.dictionary[pos];
                  this.dictionary[this.dictionaryPos] = previousbyte;
                  if (++this.dictionaryPos == this.dictionarySize) {
                     this.dictionaryPos = 0;
                  }

                  this.uncompressed_buffer[this.uncompressed_size++] = previousbyte;
                  --this.RemainLen;
                  if (this.RemainLen <= 0 || this.uncompressed_size >= outSize) {
                     break;
                  }
               }
            }
         }

         this.GlobalPos += this.uncompressed_size;
      }
   }

   private void fill_buffer() throws IOException {
      if (this.GlobalNowPos < this.GlobalOutSize) {
         this.uncompressed_offset = 0;
         long lblockSize = this.GlobalOutSize - this.GlobalNowPos;
         int blockSize;
         if (lblockSize > 65536L) {
            blockSize = 65536;
         } else {
            blockSize = (int)lblockSize;
         }

         this.LzmaDecode(blockSize);
         if (this.uncompressed_size == 0) {
            this.GlobalOutSize = this.GlobalNowPos;
         } else {
            this.GlobalNowPos += (long)this.uncompressed_size;
         }
      }

   }

   private void readHeader() throws IOException {
      byte[] properties = new byte[5];
      if (5 != this.in.read(properties)) {
         throw new LzmaException("LZMA header corrupted : Properties error");
      } else {
         this.GlobalOutSize = 0L;

         int prop0;
         int lzmaInternalSize;
         for(prop0 = 0; prop0 < 8; ++prop0) {
            lzmaInternalSize = this.in.read();
            if (lzmaInternalSize == -1) {
               throw new LzmaException("LZMA header corrupted : Size error");
            }

            this.GlobalOutSize += (long)lzmaInternalSize << prop0 * 8;
         }

         if (this.GlobalOutSize == -1L) {
            this.GlobalOutSize = Long.MAX_VALUE;
         }

         prop0 = properties[0] & 255;
         if (prop0 >= 225) {
            throw new LzmaException("LZMA header corrupted : Properties error");
         } else {
            for(this.pb = 0; prop0 >= 45; prop0 -= 45) {
               ++this.pb;
            }

            for(this.lp = 0; prop0 >= 9; prop0 -= 9) {
               ++this.lp;
            }

            this.lc = prop0;
            lzmaInternalSize = 1846 + (768 << this.lc + this.lp);
            this.probs = new int[lzmaInternalSize];
            this.dictionarySize = 0;

            int numProbs;
            for(numProbs = 0; numProbs < 4; ++numProbs) {
               this.dictionarySize += (properties[1 + numProbs] & 255) << numProbs * 8;
            }

            this.dictionary = new byte[this.dictionarySize];
            if (this.dictionary == null) {
               throw new LzmaException("LZMA : can't allocate");
            } else {
               numProbs = 1846 + (768 << this.lc + this.lp);
               this.RangeDecoder = new CRangeDecoder(this.in);
               this.dictionaryPos = 0;
               this.GlobalPos = 0;
               this.rep0 = this.rep1 = this.rep2 = this.rep3 = 1;
               this.State = 0;
               this.PreviousIsMatch = false;
               this.RemainLen = 0;
               this.dictionary[this.dictionarySize - 1] = 0;

               for(int i = 0; i < numProbs; ++i) {
                  this.probs[i] = 1024;
               }

               this.uncompressed_buffer = new byte[65536];
               this.uncompressed_size = 0;
               this.uncompressed_offset = 0;
               this.GlobalNowPos = 0L;
            }
         }
      }
   }

   public int read(byte[] buf, int off, int len) throws IOException {
      if (this.isClosed) {
         throw new IOException("stream closed");
      } else if ((off | len | off + len | buf.length - (off + len)) < 0) {
         throw new IndexOutOfBoundsException();
      } else if (len == 0) {
         return 0;
      } else {
         if (this.uncompressed_offset == this.uncompressed_size) {
            this.fill_buffer();
         }

         if (this.uncompressed_offset == this.uncompressed_size) {
            return -1;
         } else {
            int l = Math.min(len, this.uncompressed_size - this.uncompressed_offset);
            System.arraycopy(this.uncompressed_buffer, this.uncompressed_offset, buf, off, l);
            this.uncompressed_offset += l;
            return l;
         }
      }
   }

   public void close() throws IOException {
      this.isClosed = true;
      super.close();
   }
}
