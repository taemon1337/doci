package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Activity extends PstJson {

  public Activity(String folder, String subject, String messageClass, Date starttime) {
    super(folder, subject, messageClass, starttime);
  }
}