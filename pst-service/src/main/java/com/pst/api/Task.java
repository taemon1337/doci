package com.pst.api;

import com.pst.api.PstJson;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Task extends PstJson {

  public Task(String folder, String subject, String messageClass, Date datetime) {
    super(folder, subject, messageClass, datetime);
  }
}