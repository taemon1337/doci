package com.pst.resources;

import com.pst.api.FileUpload;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
// import org.glassfish.jersey.media.multipart.MultiPartFeature;
import java.util.concurrent.atomic.AtomicLong;

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
  public FileUpload uploadFile(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) {
		String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();

		writeToFile(uploadedInputStream, uploadedFileLocation);

		return new FileUpload(counter.incrementAndGet(), uploadedFileLocation, fileDetail.getFileName(), fileDetail.getSize(), fileDetail.getType(), message);
  }
  
  private void writeToFile(InputStream uploadedInputStream,
		String uploadedFileLocation) {

		try {
			OutputStream out = new FileOutputStream(new File(
					uploadedFileLocation));
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
}