package net.minecraft.launcher_.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtractRules
{
  private List<String> exclude = new ArrayList<String>();

  public ExtractRules() {
  }

  public ExtractRules(String[] exclude) {
    if (exclude != null) Collections.addAll(this.exclude, exclude); 
  }

  public List<String> getExcludes()
  {
    return this.exclude;
  }

  public boolean shouldExtract(String path) {
    if (this.exclude != null) {
      for (String rule : this.exclude) {
        if (path.startsWith(rule)) return false;
      }
    }

    return true;
  }
}
