package ru.turikhay.tlauncher.ui.swing.extended;

import javax.swing.JTextArea;
import javax.swing.text.Document;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;

public class ExtendedTextArea extends JTextArea {
   protected Document createDefaultModel() {
      return new ExtendedTextArea.ContentDocument();
   }

   public class SequenceGapContent extends GapContent implements CharSequence {
      private int checkIndex(int index, String s) {
         if (index < 0) {
            throw new IndexOutOfBoundsException(s + " is too small: " + index);
         } else if (index >= this.length()) {
            throw new IndexOutOfBoundsException(s + " equals or above length:" + index);
         } else {
            return index;
         }
      }

      public char charAt(int index) {
         return ((char[])((char[])this.getArray()))[this.checkIndex(index, "index")];
      }

      public CharSequence subSequence(int start, int end) {
         return new ExtendedTextArea.SequenceGapContent.SubSequence(start, end);
      }

      public class SubSequence implements CharSequence {
         private final int start;
         private final int end;

         SubSequence(int start, int end) {
            this.start = SequenceGapContent.this.checkIndex(start, "start");
            this.end = SequenceGapContent.this.checkIndex(end, "end");
         }

         public int length() {
            return this.end - this.start + 1;
         }

         public char charAt(int index) {
            return SequenceGapContent.this.charAt(this.start + index);
         }

         public CharSequence subSequence(int start, int end) {
            return SequenceGapContent.this.new SubSequence(this.start + start, this.start + end);
         }
      }
   }

   public class ContentDocument extends PlainDocument {
      public ContentDocument() {
         super(ExtendedTextArea.this.new SequenceGapContent());
      }

      public ExtendedTextArea.SequenceGapContent accessContent() {
         return (ExtendedTextArea.SequenceGapContent)this.getContent();
      }
   }
}
