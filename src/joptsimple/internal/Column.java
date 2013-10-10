package joptsimple.internal;

import java.text.BreakIterator;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class Column {
   static final Comparator BY_HEIGHT = new Comparator() {
      public int compare(Column first, Column second) {
         if (first.height() < second.height()) {
            return -1;
         } else {
            return first.height() == second.height() ? 0 : 1;
         }
      }
   };
   private final String header;
   private final List data;
   private final int width;
   private int height;

   Column(String header, int width) {
      this.header = header;
      this.data = new LinkedList();
      this.width = Math.max(width, header.length());
      this.height = 0;
   }

   int addCells(Object cellCandidate) {
      int originalHeight = this.height;
      String source = String.valueOf(cellCandidate).trim();
      String[] arr$ = source.split(System.getProperty("line.separator"));
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String eachPiece = arr$[i$];
         this.processNextEmbeddedLine(eachPiece);
      }

      return this.height - originalHeight;
   }

   private void processNextEmbeddedLine(String line) {
      BreakIterator words = BreakIterator.getLineInstance(Locale.US);
      words.setText(line);
      StringBuilder nextCell = new StringBuilder();
      int start = words.first();

      for(int end = words.next(); end != -1; end = words.next()) {
         nextCell = this.processNextWord(line, nextCell, start, end);
         start = end;
      }

      if (nextCell.length() > 0) {
         this.addCell(nextCell.toString());
      }

   }

   private StringBuilder processNextWord(String source, StringBuilder nextCell, int start, int end) {
      StringBuilder augmented = nextCell;
      String word = source.substring(start, end);
      if (nextCell.length() + word.length() > this.width) {
         this.addCell(nextCell.toString());
         augmented = (new StringBuilder("  ")).append(word);
      } else {
         nextCell.append(word);
      }

      return augmented;
   }

   void addCell(String newCell) {
      this.data.add(newCell);
      ++this.height;
   }

   void writeHeaderOn(StringBuilder buffer, boolean appendSpace) {
      buffer.append(this.header).append(Strings.repeat(' ', this.width - this.header.length()));
      if (appendSpace) {
         buffer.append(' ');
      }

   }

   void writeSeparatorOn(StringBuilder buffer, boolean appendSpace) {
      buffer.append(Strings.repeat('-', this.header.length())).append(Strings.repeat(' ', this.width - this.header.length()));
      if (appendSpace) {
         buffer.append(' ');
      }

   }

   void writeCellOn(int index, StringBuilder buffer, boolean appendSpace) {
      if (index < this.data.size()) {
         String item = (String)this.data.get(index);
         buffer.append(item).append(Strings.repeat(' ', this.width - item.length()));
         if (appendSpace) {
            buffer.append(' ');
         }
      }

   }

   int height() {
      return this.height;
   }
}
