package com.pst.api;

import com.pff.PSTObject;
import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Email extends PstJson {

  public Email(PSTObject obj, String folder) {
    super(obj, folder);
  }
}