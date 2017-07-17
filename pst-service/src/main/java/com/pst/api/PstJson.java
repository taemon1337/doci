package com.pst.api;

import com.pff.PSTObject;
import com.pff.PSTMessage;

import com.pst.util.Gzipper;

import org.apache.commons.lang3.StringUtils;
import java.util.Date;
import java.util.Base64;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.security.MessageDigest;
import java.math.BigInteger;

public class PstJson {
  public final String messageId;
  public final long nodeId;
  public final String folder;
  public final String messageClass;
  public final String subject;
  public final String receiver;
  public final String sender;
  public final String body;
  public final String html;
  public final int bodySize;
  public final String sha256;
  private final Date datetime;
  public final int attachmentCount;

  public PstJson(PSTObject obj, String folder) {
    PSTMessage msg = (PSTMessage)obj;
    this.messageId = msg.getInternetMessageId();
    this.nodeId = obj.getDescriptorNodeId();
    this.folder = folder;
    this.messageClass = msg.getMessageClass();
    this.subject = msg.getSubject();
    this.receiver = msg.getReceivedByName() + "; " + msg.getReceivedByAddress();
    // this.sender = msg.getSentRepresentingName() + "; " + msg.getSentRepresentingAddressType() + "; " + msg.getSentRepresentingEmailAddress();
    this.sender = msg.getSenderName() + "; " + msg.getSenderEmailAddress();
    this.attachmentCount = msg.getNumberOfAttachments();
    this.datetime = msg.getMessageDeliveryTime();
    this.body = msg.getBody();
    this.html = b64gzip(msg.getBodyHTML());
    this.bodySize = this.body.length() > 0 ? this.body.length() : this.html.length();
    this.sha256 = calcSha256(this.messageId, this.subject, this.sender, this.receiver);
  }
  
  private String b64gzip(String text) {
    try {
      byte[] compressedBytes = Gzipper.compress(text);
      Base64.Encoder encoder = Base64.getEncoder();
      byte[] encoded = encoder.encode(compressedBytes);
      return new String(encoded);
    } catch (java.io.IOException err) {
      return err.toString();
    }
  }
  
  private String escapeHtml(String str) {
    return StringUtils.replaceEach(str, new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
  }
  
  private String calcSha256(String ... args) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(String.join("", args).getBytes("UTF-8"));
      byte[] digest = md.digest();
      return String.format("%064x", new BigInteger(1, digest));
    } catch (Exception err) {
      return err.toString();
    }
  }

  @JsonProperty
  public String getMessageId() {
    return messageId; // internet standard message id
  }
  
  @JsonProperty
  public long getNodeId() {
    return nodeId; // PSTObject.detectAndLoadPSTObject(pstFile, nodeId);
  }

  @JsonProperty
  public String getFolder() {
    return folder; // path in pst
  }
  
  @JsonProperty
  public String getMessageClass() {
    return messageClass; // IPM.Note or IPM.Appointment or IPM.Note.SMIME, etc...
  }

  @JsonProperty
  public String getSubject() {
    return subject;
  }
  
  @JsonProperty
  public String getSender() {
    return sender;
  }
  
  @JsonProperty
  public String getReceiver() {
    return receiver;
  }
  
  @JsonProperty
  public String getBody() {
    return body;
  }
  
  @JsonProperty
  public int getBodySize() {
    return bodySize;
  }
  
  @JsonProperty
  public String getHtml() {
    return html;
  }
  
  @JsonProperty
  public Date getDatetime() {
    return datetime;
  }
  
  @JsonProperty
  public int getAttachmentCount() {
    return attachmentCount;
  }
  
  @JsonProperty
  public String getSha256() {
    return sha256;
  }
}