package uni.akilis.x2ai;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import uni.akilis.help.LoggerX;
/**
 * 
 * @author leo
 *
 */
@Path("/modelService")
public class ModelService {
	public static final String LOG_TAG = ModelService.class.getName();
	
	@POST
	@Path("/train")
	public String trainModel(){
		return null;
	}
	
	@POST
	@Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition info) throws IOException {
		File upload = new File(info.getFileName());
		if (upload.exists()){
            String message = "file: " + upload.getName() + " already exists."
            		+ "\ndelete the old version.";
            LoggerX.println(LOG_TAG, message);
            upload.delete();
        } 
        try{
        	Files.copy(in, upload.toPath());
        	String url = "url_" + upload.getName();
            return Response.status(Status.OK).entity(url).build();
        	}
        catch(IOException e){
        	String err = "file upload failed!";
        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
        }
	}
	
	@GET
	@Path("/recommendationResults")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	List<RecommendationResultDTO> showRecommendationResults(@QueryParam("resultUrl") String resultUrl){
		if(resultUrl != null){
			LoggerX.println(LOG_TAG, "recommendations url: " + resultUrl);
			// test
			List<RecommendationResultDTO> recomms = new ArrayList<>();
			recomms.add(new RecommendationResultDTO("9090950", 1.0));
			recomms.add(new RecommendationResultDTO("9527", 9.9));
			recomms.add(new RecommendationResultDTO("5972", 0.59));
			return recomms;
		}
		else{
			LoggerX.println(LOG_TAG, "invalid URL!");
			return new ArrayList<>();
		}
	}
}
