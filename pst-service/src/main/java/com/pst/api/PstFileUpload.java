package com.pst.api;

import com.pst.api.PstJson;
import com.pst.api.PstCounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

public class PstFileUpload {
  private final long id;
  private final String filepath;
  private final String filename;
  private final long filesize;
  private final String message;
  private final List emails;
  private final List appointments;
  private final long emailCount;
  private final long appointmentCount;
  private final PstCounts counts;

  public PstFileUpload(long id, String filepath, String filename, long filesize, String message, List<PstJson> results) {
    this.id = id;
    this.filepath = filepath;
    this.filename = filename;
    this.filesize = filesize;
    this.message = message;
    this.emails = filterToEmails(results);
    this.emailCount = this.emails.size();
    this.appointments = filterToAppointments(results);
    this.appointmentCount = this.appointments.size();
    this.counts = new PstCounts(results);
  }
  
  private List<PstJson> filterToEmails(List<PstJson> list) {
    List<PstJson> emails = new ArrayList();

    for(int i = 0; i < list.size(); i++) {
      PstJson item = list.get(i);
      if (item.getMessageClass().startsWith("IPM.Note")) {
        emails.add(item);
      }
    }

    return emails;
  }
  
  private List<PstJson> filterToAppointments(List<PstJson> list) {
    List<PstJson> appts = new ArrayList();

    for(int i = 0; i < list.size(); i++) {
      PstJson item = list.get(i);
      if (item.getMessageClass().startsWith("IPM.Appointment")) {
        appts.add(item);
      }
    }

    return appts;
  }

  @JsonProperty
  public long getId() {
    return id;
  }

  @JsonProperty
  public String getContent() {
    return message + ": " + filepath;
  }
  
  @JsonProperty
  public String getFilename() {
    return filename;
  }
  
  @JsonProperty
  public long getFilesize() {
    return filesize;
  }

  @JsonProperty
  public List getEmails() {
    return emails;
  }

  @JsonProperty
  public long getEmailCount() {
    return emailCount;
  }

  @JsonProperty
  public List getAppointments() {
    return appointments;
  }

  @JsonProperty
  public long getAppointmentCount() {
    return appointmentCount;
  }
  
  @JsonProperty
  public PstCounts getCounts() {
    return counts;
  }
}