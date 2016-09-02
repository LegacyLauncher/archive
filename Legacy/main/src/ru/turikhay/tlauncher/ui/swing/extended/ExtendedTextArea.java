package ru.turikhay.tlauncher.ui.swing.extended;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;

public class ExtendedTextArea extends JTextArea {
    protected Document createDefaultModel() {
        return new ContentDocument();
    }

    public class ContentDocument extends PlainDocument {
        public ContentDocument() {
            super(new SequenceGapContent());
        }

        public SequenceGapContent accessContent() {
            return (SequenceGapContent) getContent();
        }
    }

    public class SequenceGapContent extends GapContent implements CharSequence {
        private int checkIndex(int index, String s) {
            if (index < 0) {
                throw new IndexOutOfBoundsException(s + " is too small: " + index);
            }
            if (index >= length()) {
                throw new IndexOutOfBoundsException(s + " equals or above length:" + index);
            }
            return index;
        }

        @Override
        public char charAt(int index) {
            return ((char[]) getArray())[checkIndex(index, "index")];
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return new SubSequence(start, end);
        }

        public class SubSequence implements CharSequence {
            private final int start, end;

            SubSequence(int start, int end) {
                this.start = checkIndex(start, "start");
                this.end = checkIndex(end, "end");
            }

            @Override
            public int length() {
                return end - start + 1;
            }

            @Override
            public char charAt(int index) {
                return SequenceGapContent.this.charAt(start + index);
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new SequenceGapContent.SubSequence(this.start + start, this.start + end);
            }
        }
    }
}
