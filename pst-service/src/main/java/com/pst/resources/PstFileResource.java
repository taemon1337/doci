package com.pst.resources;

import com.pst.util.FileWriter;
import com.pst.core.PstStreamReader;
import com.pst.core.PstStreamWriter;
import com.pst.core.PstStreamingOutput;

import java.io.Writer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import javax.ws.rs.WebApplicationException;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import com.codahale.metrics.annotation.Timed;

@Path("/pst")
@Produces("application/octet-stream")
public class PstFileResource {
  
  @POST
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Timed
  public Response PstFileResource(@FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail) throws IOException {
    String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();

    // save uploaded file to tmp directory
		FileWriter.save(uploadedInputStream, uploadedFileLocation);

  //   PipedOutputStream outputStream = new PipedOutputStream();
  //   PipedInputStream inputStream = new PipedInputStream(outputStream);
  //   PstStreamWriter writer = new PstStreamWriter(outputStream);
  //   PstStreamReader reader = new PstStreamReader(inputStream);

  //   writer.startParsingPstFile(uploadedFileLocation);
  //   reader.startReading();

    // StreamingOutput stream = new StreamingOutput() {
    //   @Override
    //   public void write(OutputStream os) throws IOException, WebApplicationException {
    //     Writer writer = new BufferedWriter(new OutputStreamWriter(os));
    //     writer.write("test");
    //     writer.flush();
    //   }
    // };
    
    PstStreamingOutput stream = new PstStreamingOutput(uploadedFileLocation);
    return Response.ok(stream).build();
  }
}
