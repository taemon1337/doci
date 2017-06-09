package com.pst.resources;

import com.pst.api.FileUpload;
import com.pst.api.PstFileUpload;
import com.pst.api.PstJson;
import com.pst.api.Email;
import com.pst.api.Contact;
import com.pst.api.Attachment;
import com.pst.api.Task;
import com.pst.api.Activity;
import com.pst.api.Appointment;

import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTAttachment;
import com.pff.PSTAppointment;
import com.pff.PSTActivity;
import com.pff.PSTTask;
import com.pff.PSTException;
import com.pff.PSTContact;
import com.pff.PSTObject;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.io.ByteArrayOutputStream;

@Path("/upload")
@Produces(MediaType.APPLICATION_JSON)
public class FileUploadResource {
  private final String message;
  private final AtomicLong counter;
  
  public FileUploadResource(String message) {
    this.message = message;
    this.counter = new AtomicLong();
  }
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  public PstFileUpload uploadPSTFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
    String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();

		writeToFile(uploadedInputStream, uploadedFileLocation);
		List results = parsePst(uploadedFileLocation);
		File file = new File(uploadedFileLocation);

		return new PstFileUpload(counter.incrementAndGet(), uploadedFileLocation, fileDetail.getFileName(), file.length(), message, results);
  }
  
  private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation) {
		try {
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<PstJson> parsePst(String inputPath) {
	  List items = new ArrayList();
		try {
			try {
				PSTFile pstFile = new PSTFile(inputPath);
    		System.out.println(pstFile.getMessageStore().getDisplayName());
    		return processFolder(pstFile.getRootFolder());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (PSTException | IOException e) {
			e.printStackTrace();
		}
		return items;
	}
	
	int depth = -1;

  public List<PstJson> processFolder(PSTFolder folder) throws PSTException, IOException {
      List results = new ArrayList();
      depth++;
      // the root folder doesn't have a display name
      if (depth > 0) {
          printDepth();
          System.out.println(folder.getDisplayName());
      }

      // go through the folders...
      if (folder.hasSubfolders()) {
          Vector<PSTFolder> childFolders = folder.getSubFolders();
          for (PSTFolder childFolder : childFolders) {
            List<PstJson> children = processFolder(childFolder);
            results.add(children);
          }
      }

      if (folder.getContentCount() > 0) {
          depth++;

          PSTObject psto = folder.getNextChild();
          while (psto != null) {
            printDepth();
            results.add(processMessage(psto));
            psto = folder.getNextChild();
          }
          depth--;
      }
      depth--;
      
      return results;
  }
  
  public PstJson processMessage(PSTObject obj) {
    if (obj instanceof PSTContact) {
      PSTContact contact = (PSTContact)obj;
      return new Contact(contact.getGivenName());
    } else if (obj instanceof PSTAppointment) {
      PSTAppointment appt = (PSTAppointment)obj;
      return new Appointment()
      json.setSubject(appt.getSubject());
    } else if (obj instanceof PSTActivity) {
      PSTActivity activity = (PSTActivity)obj;
      json.setType("Activity");
      json.setSubject(activity.getSubject());
    } else if (obj instanceof PSTTask) {
      PSTTask task = (PSTTask)obj;
      json.setType("Task");
      json.setSubject(task.getSubject());
    } else if (obj instanceof PSTMessage) {
      PSTMessage msg = (PSTMessage)obj;
        json.setType("Message");
        json.setSubject(msg.getSubject());
    } else if (obj instanceof PSTAttachment) {
      PSTAttachment attachment = (PSTAttachment)obj;
      json.setType("Attachment");
      json.setSubject(attachment.getFilename());
    } else {
      json.setType("Unknown");
      json.setSubject("?");
    }
    return json;
  }

  public void printDepth() {
      for (int x = 0; x < depth-1; x++) {
          System.out.print(" | ");
      }
      System.out.print(" |- ");
  }
}