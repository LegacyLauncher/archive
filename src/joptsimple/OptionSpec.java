package joptsimple;

import java.util.Collection;
import java.util.List;

public interface OptionSpec {
   List values(OptionSet var1);

   Object value(OptionSet var1);

   Collection options();
}
