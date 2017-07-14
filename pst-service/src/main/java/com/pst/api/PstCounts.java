package com.pst.api;

import com.pst.api.PstJson;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;

public class PstCounts {
  private final HashMap<String, Integer> counts;

  public PstCounts(List<PstJson> results) {
    this.counts = calcStats(results);
  }
  
  public HashMap<String, Integer> calcStats(List<PstJson> list) {
    HashMap<String, Integer> c = new HashMap<String, Integer>();

    for (int i = 0; i < list.size(); i++) {
      PstJson pstj = list.get(i);
      String mc = pstj.getMessageClass();
      if (c.containsKey(mc) == true) {
        c.put(mc, c.get(mc) + 1);
      } else {
        c.put(mc, 1);
      }
    }

    return c;
  }

  @JsonProperty
  public HashMap<String, Integer> getCounts() {
    return counts;
  }
}