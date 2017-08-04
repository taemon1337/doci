package com.pst.api;

import com.pff.PSTObject;
import com.pff.PSTMessage;
import com.pff.PSTException;

import com.pst.util.Gzipper;

import javax.json.Json;
import org.apache.commons.lang3.StringUtils;
import java.io.IOException;
import java.util.Date;
import java.util.List;
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
  public final String from;
  public final String to;
  public final String body;
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
    this.from = parseSender(msg);
    this.to = parseReceiver(msg);
    this.attachmentCount = msg.getNumberOfAttachments();
    this.datetime = msg.getMessageDeliveryTime();
    this.body = parseBody(msg);
    this.bodySize = this.body.length();
    this.sha256 = calcSha256(this.messageId, this.subject, this.from, this.to);
  }

  private String parseSender(PSTMessage msg) {
    String from = "";
    String s1 = msg.getSenderName();
    String s2 = msg.getSenderEmailAddress();
    if (s1 != "") {
      from += s1;
    }
    if (s2 != "") {
      from += s2;
    }
    return from;
  }
  
  private String parseReceiver(PSTMessage msg) {
    String to = "";
    String r1 = msg.getReceivedByName();
    String r2 = msg.getReceivedByAddress();
    if (r1 != "") {
      to += r1;
    }
    if (r2 != "") {
      to += r2;
    }
    return to;
  }

  private String parseBody(PSTMessage msg) {
    try {
      String body = msg.getBody();
      String html = msg.getBodyHTML();
      String rtfb = msg.getRTFBody();
      int bl = body.length();
      int hl = html.length();
      int rl = rtfb.length();
      if (bl > hl && bl > rl) {
        return base64(body);
      } else if (rl > hl && rl > bl) {
        return base64(rtfb);
      } else {
        return base64(html);
      }
    } catch (PSTException e) {
      e.printStackTrace();
      return e.toString();
    } catch (IOException e) {
      e.printStackTrace();
      return e.toString();
    }
  }
  
  private String base64(String text) {
    Base64.Encoder encoder = Base64.getEncoder();
    byte[] encoded = encoder.encode(text.getBytes());
    return new String(encoded);
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
  public String getFrom() {
    return from;
  }
  
  @JsonProperty
  public String getTo() {
    return to;
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
  
  public String toString() {
    return Json.createObjectBuilder()
      .add("folder-path", folder)
      .add("message-class", messageClass)
      .add("message-id", messageId)
      .add("node-id", String.format("%d", nodeId))
      .add("from", from)
      .add("to", to)
      .add("subject", subject)
      .add("datetime", datetime.toString())
      .build()
      .toString()
    ;
  }
}