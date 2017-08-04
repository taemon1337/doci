package com.pst.core;

import com.pst.api.PstJson;

import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import javax.ws.rs.WebApplicationException;

import com.pff.PSTFile;
import com.pff.PSTObject;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTException;

import java.io.File;
import java.util.concurrent.Executors;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.util.Vector;
import java.util.Date;

public class PstStreamingOutput implements StreamingOutput {
  private final String filepath;
  
  public PstStreamingOutput(String filepath) {
    this.filepath = filepath;
  }

  public void writeOLD(OutputStream outputStream) throws IOException, WebApplicationException {
    try (JsonGenerator generator = Json.createGenerator(outputStream)) {
      try {
        // open the PST file
        System.out.println("INFO: parsing PST file at" + this.filepath);
        PSTFile pstfile = new PSTFile(this.filepath);
        
        generator.writeStartObject().write("filepath", this.filepath);

        // open array
        JsonGenerator arrayGenerator = generator.writeStartArray("records");

        // process the root folder
        processFolder(pstfile.getRootFolder(), "/", arrayGenerator);
        
        // close array, object, and close
        arrayGenerator.writeEnd();
        generator.writeEnd().close();
      } catch (PSTException | IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void write(OutputStream outputStream) throws IOException, WebApplicationException {
    try {
      Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
      
      // open the PST file
      System.out.println("INFO: parsing PST file at" + this.filepath);
      PSTFile pstfile = new PSTFile(this.filepath);
  
      // process the root folder
      writer.write("[\n");
      parseFolder(pstfile.getRootFolder(), ".", writer);
      writer.write("{}\n]");
      
      writer.flush();
      writer.close();
    } catch (PSTException e) {
      e.printStackTrace();
    }
  }
  
  public void processFolder(PSTFolder folder, String path, JsonGenerator generator) {
    path = path + "/" + folder.getDisplayName();
    
    if (folder.hasSubfolders()) {
      try {
        Vector<PSTFolder> childFolders = folder.getSubFolders();
        for (PSTFolder childFolder : childFolders) {
          processFolder(childFolder, path, generator);
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        generator
          .writeStartObject()
          .write("folder-path", path)
          .write("error", e.toString())
        .writeEnd();
      }
    }
    
    if (folder.getContentCount() > 0) {
      try {
        PSTObject psto = folder.getNextChild();
        while (psto != null) {
          processPstObject(psto, path, generator);
          psto = folder.getNextChild();
        }
      } catch (PSTException | IOException e) {
        e.printStackTrace();
        generator
          .writeStartObject()
          .write("folder-path", path)
          .write("error", e.toString())
        .writeEnd();
      }
    }
  }
  
  public void processPstObject(PSTObject obj, String path, JsonGenerator generator) {
    PSTMessage msg = (PSTMessage)obj;

    String messageClass = msg.getMessageClass();
    String messageId = msg.getInternetMessageId();
    String sender = msg.getSenderName() + "; " + msg.getSenderEmailAddress();
    String receiver = msg.getReceivedByName() + "; " + msg.getReceivedByAddress();
    String subject = msg.getSubject();
    String datetime = msg.getMessageDeliveryTime().toString();
    String nodeId = String.format("%d", msg.getDescriptorNodeId());
    
    generator
      .writeStartObject()
      .write("folder-path", path)
      .write("message-class", messageClass)
      .write("message-Id", messageId)
      .write("node-Id", nodeId)
      .write("sender", sender)
      .write("receiver", receiver)
      .write("subject", subject)
      .write("datetime", datetime)
    .writeEnd();
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
        try {
          e.printStackTrace();
          writer.write(String.format("{ \"%s\": \"%s\"}", "Error", e.toString()));
          writer.flush();
        } catch (IOException ee) {
          ee.printStackTrace();
        }
      }
    }
    
    if (folder.getContentCount() > 0) {
      try {
        PSTObject psto = folder.getNextChild();
        while (psto != null) {
          PstJson pj = parsePstObject(psto, path);
          writer.write(pj.toString() + ",\n");
          writer.flush();
          psto = folder.getNextChild();
        }
      } catch (PSTException | IOException e) {
        try {
          e.printStackTrace();
          writer.write(String.format("{ \"%s\": \"%s\"}", "Error", e.toString()));
          writer.flush();
        } catch (IOException ee) {
          ee.printStackTrace();
        }
      }
    }
  }
  
  public PstJson parsePstObject(PSTObject obj, String path) {
    return new PstJson(obj, path);
  }

}
