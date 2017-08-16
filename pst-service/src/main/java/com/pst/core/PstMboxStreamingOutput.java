package com.pst.core;

import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import javax.ws.rs.WebApplicationException;
import java.util.concurrent.TimeUnit;
import java.io.BufferedInputStream;

// import org.reflections.ReflectionUtils.getAllMethods;
// import org.reflections.ReflectionUtils.withModifier;
// import org.reflections.ReflectionUtils.withPrefix;

import com.pff.PSTFile;
import com.pff.PSTObject;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTException;
import com.pff.PSTAttachment;

import java.io.File;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.util.Vector;
import java.util.Date;

public class PstMboxStreamingOutput implements StreamingOutput {
  private final String filepath;
  
  public PstMboxStreamingOutput(String filepath) {
    this.filepath = filepath;
  }

  public void write(OutputStream outputStream) throws IOException, WebApplicationException {
    try {
      Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));

      // open the PST file
      System.out.println("INFO: parsing PST file at" + this.filepath);
      PSTFile pstfile = new PSTFile(this.filepath);

      // process the root folder
      parseFolder(pstfile.getRootFolder(), ".", writer);
    } catch (PSTException e) {
      e.printStackTrace();
    }
  }

  public void parseFolder(PSTFolder folder, String path, Writer writer) {
    path = path + "." + folder.getDisplayName();
    
    if (folder.hasSubfolders()) {
      try {
        Vector<PSTFolder> childFolders = folder.getSubFolders();
        for (PSTFolder childFolder : childFolders) {
          parseFolder(childFolder, path, writer);
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        System.out.println(String.format("Error parsing folder %s: %s", path, e.toString()));
      }
    }
    
    if (folder.getContentCount() > 0) {
      try {
        PSTObject psto = folder.getNextChild();
        while (psto != null) {
          parsePstObject(psto, path, writer);
          // TimeUnit.SECONDS.sleep(1); // sleep to test streaming
          psto = folder.getNextChild();
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        System.out.println(String.format("Error parsing folder %s: %s", path, e.toString()));
      }
    }
  }
  
  public void parsePstObject(PSTObject obj, String path, Writer writer) {
    try {
      PSTMessage msg = (PSTMessage)obj;

      String messageClass = msg.getMessageClass();
      String messageId = msg.getInternetMessageId();
      String subject = msg.getSubject();
      String nodeId = String.format("%d", msg.getDescriptorNodeId());
      int attachmentCount = msg.getNumberOfAttachments();
      String transportHeaders = msg.getTransportMessageHeaders();
      String body = msg.getBody();
      String html = msg.getBodyHTML();
      String rtf = msg.getRTFBody();
      String s = "----------";

      writer.write(s + " PST HEADERS " + s + "\n");
      writer.write("Subject: " + subject + "\n");
      writer.write("Message Class: " + messageClass + "\n");
      writer.write("Message ID: " + messageId + "\n");
      writer.write("PST Folder Path: " + path + "\n");
      writer.write("PST Node Id: " + nodeId + "\n");
      writer.write("PST Attachment Count: " + String.format("%d", attachmentCount) + "\n");
      writer.write(s + " SMTP HEADERS " + s + "\n");
      writer.write(transportHeaders + "\n");
      writer.write(s + " BODY " + s + "\n");
      writer.write(body + "\n");
      writer.write(s + " HTML " + s + "\n");
      writer.write(html + "\n");
      writer.write(s + " RICH TEXT FORMAT BODY " + s + "\n");
      writer.write(rtf + "\n");
      // writer.write(s + " ATTACHMENTS " + s + "\n");
      // writeAttachments(msg, writer);
      writer.flush();
    } catch (IOException | PSTException e) {
      e.printStackTrace();
      System.out.println("Error parsing PST message");
    }
  }
  
  public void writeAttachments(PSTMessage msg, Writer writer) throws PSTException, IOException {
    int numberOfAttachments = msg.getNumberOfAttachments();
    for (int x = 0; x < numberOfAttachments; x++) {
      PSTAttachment attach = msg.getAttachment(x);

      String filename = attach.getLongFilename();
      if (filename.isEmpty()) {
          filename = attach.getFilename();
      }
      
      try {
        writer.write("Attachment Number: " + String.format("%d", x) + "\n");
        writer.write("Attachment Filename: " + filename + "\n");
        writer.write("Attachment Content-Type: base64 \n");
        writer.write(getBase64Attachment(attach) + "\n");
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Could not read attachment " + String.format("%d", x) + "; " + filename);
      }
    }
  }

  public String getBase64Attachment(PSTAttachment attachment) throws Exception {
    int length = attachment.getSize();
    InputStream inputStream = attachment.getFileInputStream();
    BufferedInputStream reader = new BufferedInputStream(inputStream);
    byte[] bytes = new byte[length];
    reader.read(bytes, 0, length);
    reader.close();
    return new String(bytes);
  }

}
