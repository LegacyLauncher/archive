package joptsimple.internal;

class ColumnWidthCalculator {
   int calculate(int totalWidth, int numberOfColumns) {
      if (numberOfColumns == 1) {
         return totalWidth;
      } else {
         int remainder = totalWidth % numberOfColumns;
         return remainder == numberOfColumns - 1 ? totalWidth / numberOfColumns : totalWidth / numberOfColumns - 1;
      }
   }
}
