package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Appointment extends PstJson {

  public Appointment(String folder, String subject, String messageClass, Date starttime) {
    super(folder, subject, messageClass, starttime);
  }
}