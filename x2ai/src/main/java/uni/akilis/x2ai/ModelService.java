package uni.akilis.x2ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import uni.akilis.helper.LoggerX;

/**
 * 
 * @author leo
 *
 */
@Path("/modelService")
public class ModelService {
	public static final String LOG_TAG = ModelService.class.getName();
	
	private XMan xMan = new Wolverine();
	private XETLer xEt = new DatumET();
	@Context 
	private ServletContext servletContext;
	
	@GET
	@Path("/welcome")
	public String welcome(){
		return "welcome to use our model service!";
	}
	@POST
	@Path("/train")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response trainModel(@FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition info
            , @FormDataParam("action") String action
            , @FormDataParam("algoName") String algoName) throws IOException {
 			if((action == null || "".equals(action)) &&
 					(algoName == null || "".equals(algoName))){
 				LoggerX.println(LOG_TAG, "action and algoName cannot both be empty!");
 				return Response.status(Status.BAD_REQUEST).entity("unkown attempt!").build();
 			}
 			if(action == null || "".equals(action)){
 				action = XConstant.ACTION_TRAIN;
 			}
 			else{
 				algoName = "all";
 			}
 			LoggerX.println(LOG_TAG, "action: " + action + ", algo: " + algoName);
 	 		String webRoot = this.servletContext.getRealPath("/") == null? "" : this.servletContext.getRealPath("/");
 			String resourceDir = webRoot + XConstant.RESOURCE_DIR;
 			File resourceDirFile = new File(resourceDir);
 			if(!resourceDirFile.exists()){
 				if(!resourceDirFile.mkdirs()){
 					LoggerX.error(LOG_TAG, "cannot mkdir for warehouse!");
 	    			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, service crash! Please require later!").build();
 				}
 			}
 	 		File upload = new File(resourceDir + info.getFileName());
 			LoggerX.println(LOG_TAG, "file path: " + upload.getAbsolutePath());
 			if (upload.exists()){
 	            String message = "file: " + upload.getName() + " already exists."
 	            		+ "\ndelete the old version.";
 	            LoggerX.println(LOG_TAG, message);
 	            upload.delete();
 	        } 
 	        try{
 	        	Files.copy(in, upload.toPath());
 	        	String url = upload.getAbsolutePath();
 	        	/*
 	        	 * supply service with respect to the action. 
 	        	 */
 	        	switch(action){
 	        	case XConstant.ACTION_TRAIN:{
 	        		if(!this.xMan.train(algoName, url, webRoot)){
 	        			String err = "training model failed!";
 	        			LoggerX.println(LOG_TAG, err);
 	    	        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
 	        		}
 	        		else{
 	        			return Response.status(Status.OK).entity("success").build();
 	        		}
 	        	}
 	        	case XConstant.ACTION_TRAIN_ALL:{
 	        		if(!this.xMan.trainAll(url, webRoot)){
 	        			String err = "training model failed!";
 	        			LoggerX.println(LOG_TAG, err);
 	    	        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
 	        		}
 	        		else{
 	        			return Response.status(Status.OK).entity("success").build();
 	        		}
 	        	}
 	        	default: return Response.status(Status.BAD_REQUEST).entity("unkown action!").build();
 	        	}
 	        }
 	        catch(IOException e){
 	        	String err = "file upload failed!";
 	        	return Response.status(Status.INTERNAL_SERVER_ERROR).entity(err).build();
 	        }
 		}
	
	@POST
	@Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@FormDataParam("file") InputStream in,
            @FormDataParam("file") FormDataContentDisposition info
            , @FormDataParam("action") String action
            , @FormDataParam("algoName") String algoName) throws IOException {
 		if(action == null || "".equals(action)){
			LoggerX.println(LOG_TAG, "action is necessary!");
			return Response.status(Status.BAD_REQUEST).entity("unkown action!").build();
		}
		else{
			LoggerX.println(LOG_TAG, "action: " + action);
		}
		
 		if(algoName == null || "".equals(algoName)){
			LoggerX.println(LOG_TAG, "algorithm is necessary!.");
			return Response.status(Status.BAD_REQUEST).entity("unkown algorithm!").build();
 		}
 		else{
			LoggerX.println(LOG_TAG, "algo: " + algoName);
 		}
 		String webRoot = this.servletContext.getRealPath("/") == null? "" : this.servletContext.getRealPath("/");
		String resourceDir = webRoot + XConstant.RESOURCE_DIR;
		File resourceDirFile = new File(resourceDir);
		if(!resourceDirFile.exists()){
			if(!resourceDirFile.mkdirs()){
				LoggerX.error(LOG_TAG, "cannot mkdir for warehouse!");
    			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, service crash! Please require later!").build();
			}
		}
 		File upload = new File(resourceDir + info.getFileName());
		LoggerX.println(LOG_TAG, "file path: " + upload.getAbsolutePath());
		if (upload.exists()){
            String message = "file: " + upload.getName() + " already exists."
            		+ "\ndelete the old version.";
            LoggerX.println(LOG_TAG, message);
            upload.delete();
        } 
        try{
        	Files.copy(in, upload.toPath());
        	String url = upload.getAbsolutePath();
        	/*
        	 * supply service with respect to the action. 
        	 */
        	switch(action){
        	case XConstant.ACTION_PREDICT:{
        		String examples = this.xEt.cook(url);
        		if(examples == null){
        			LoggerX.println(LOG_TAG, "data ETL failed!");
        			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, service crash! Please require later!").build();
        		}
        		String outputDir = resourceDir + XConstant.recommendation_dir;
        		File outputDirFile = new File(outputDir);
        		if(!outputDirFile.exists()){
        			if(!outputDirFile.mkdirs()){
        				LoggerX.println(LOG_TAG, "cannot mkdir for output!");
            			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, service crash! Please require later!").build();
        			}
        		}
        		String rankingsUrl = this.xMan.predict(algoName, examples, outputDir, webRoot); 
        		if(rankingsUrl == null){
        			LoggerX.println(LOG_TAG, "prediction failed!");
        			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Sorry, service crash! Please require later!").build();
        		}
        		else{
        			Map<Integer, String> itemMap = new HashMap<>();
        			BufferedReader itemReader = new BufferedReader(new FileReader(upload));
        			String line;
        			int cnt = 0;
        			while((line = itemReader.readLine()) != null){
        				line = line.trim();
        				if(!"".equals(line))
        					itemMap.put(cnt++, line);
        			}
        			itemReader.close();
        			BufferedReader rankings = new BufferedReader(new FileReader(rankingsUrl));
        			File rst = new File(outputDir + String.valueOf(System.currentTimeMillis()) + "_" + algoName + "_" + info.getFileName());
        			BufferedWriter rstWriter = new BufferedWriter(new FileWriter(rst));
        			while((line = rankings.readLine()) != null){
        				String[] words = line.split("\\s+");
        				if(words.length != 2){
        					continue;
        				}
        				// TODO
        				int ind = Integer.parseInt(words[0]);
        				double score = Double.parseDouble(words[1]);
        				if(!itemMap.containsKey(ind)){
        					continue;
        				}
        				String item = itemMap.get(ind);
        				rstWriter.write(item + "\t" + score);
        				rstWriter.newLine();
        			}
        			rankings.close();
        			rstWriter.close();
        			return Response.status(Status.OK).entity(rst.getAbsolutePath()).build();
        		}
        	}
        	default: return Response.status(Status.BAD_REQUEST).entity("unkown action!").build();
        	}
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
	public List<RecommendationResultDTO> showRecommendationResults(@QueryParam("resultUrl") String resultUrl){
		if(resultUrl != null){
			LoggerX.println(LOG_TAG, "recommendations url: " + resultUrl);
			// test
/*			List<RecommendationResultDTO> recomms = new ArrayList<>();
			recomms.add(new RecommendationResultDTO("9090950", 1.0));
			recomms.add(new RecommendationResultDTO("9527", 0.99));
			recomms.add(new RecommendationResultDTO("5972", 0.59));
			return recomms;
*/		
			// read from file.
			File rstFile = new File(resultUrl);
			if(!rstFile.exists()){
				LoggerX.println(LOG_TAG, "invalid URL!");
			}
			List<RecommendationResultDTO> recomms = new ArrayList<>();
			try {
				BufferedReader bfr = new BufferedReader(new FileReader(rstFile));
				String line;
				while((line = bfr.readLine()) != null){
					String[] items = line.split("\\s+");
					if(items.length != 2){
						continue;
					}
					recomms.add(new RecommendationResultDTO(items[0], Double.valueOf(items[1])));
				}
				bfr.close();
				return recomms;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return new ArrayList<>();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return new ArrayList<>();
			}
				
		}
		else{
			LoggerX.println(LOG_TAG, "URL is null!");
			return new ArrayList<>();
		}
	}
}
