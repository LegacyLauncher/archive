package ru.turikhay.tlauncher.exceptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IOExceptionList extends IOException {
   private final List list;
   private final List _list;

   public IOExceptionList(List l) {
      super(describe(l));
      this.list = l;
      this._list = l == null ? Collections.unmodifiableList(Collections.EMPTY_LIST) : Collections.unmodifiableList(new ArrayList(l));
   }

   private static String describe(List list) {
      if (list != null && !list.isEmpty()) {
         StringBuilder b = (new StringBuilder("(")).append(list.size()).append("): [");

         for(Iterator var2 = list.iterator(); var2.hasNext(); b.append("; ")) {
            IOException ioE = (IOException)var2.next();
            b.append(ioE.getMessage());
            if (ioE.getCause() != null) {
               b.append(" (cause: ").append(ioE.getCause()).append(")");
            }
         }

         return b.append("]").toString();
      } else {
         return "unknown";
      }
   }
}
