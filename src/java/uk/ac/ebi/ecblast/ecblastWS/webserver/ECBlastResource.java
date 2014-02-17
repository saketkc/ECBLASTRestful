/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.webserver;

import uk.ac.ebi.ecblast.ecblastWS.databasewrapper.DatabaseConfiguration;
import uk.ac.ebi.ecblast.ecblastWS.databasewrapper.JobsQueryWrapper;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import uk.ac.ebi.ecblast.ecblastWS.utility.APIResponse;
import uk.ac.ebi.ecblast.ecblastWS.utility.FileUploadUtility;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.ebi.ecblast.ecblastWS.parser.AtomAtomMappingParser;

/**
 * REST Web Service
 *
 * @author saket
 */
@Path("/")
public class ECBlastResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of ECBlastResource
     *
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ErrorResponse getHtml() {
        //TODO return proper representation object
        ErrorResponse response = new ErrorResponse();
        response.setStatus(Response.Status.FORBIDDEN);
        response.setReason("test");
        response.setMessage("test");
        //return response;
        throw  new ErrorResponse(Status.NOT_FOUND, "URL NOT FOUND", "error");

    }

    @PUT
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse putHtml() {
        //TODO return proper representation object
        APIResponse response = new APIResponse();
        response.setResponse("test");
        response.setMessage("test");

        return response;
    }

    /**
     * PUT method for updating or creating an instance of ECBlastResource
     *
     * @param jobID
     * @return an HTTP response with content of the updated or created resource.
     * @throws com.ebi.ecblast.ErrorResponse
     */
    @Path("/status/{jobID}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse getJobStatus(@PathParam("jobID") String jobID) throws ErrorResponse {
        try {
            Integer.parseInt(jobID);
        } catch (NumberFormatException e) {
            throw new ErrorResponse(Status.BAD_REQUEST, jobID + " is not a valid jobID", "error");
        }
        APIResponse response = new APIResponse();
        if (!"123".equals(jobID)) {

            response.setResponse("test");
            response.setMessage("test");
            return response;
        } else {
            throw new ErrorResponse(Status.BAD_REQUEST, jobID + " is not a valid jobID", "error");
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/query.*")
    public APIResponse getQuery() {
        throw new ErrorResponse(Status.BAD_REQUEST, " No Params supplied", "error");
    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/query/kegg/{keggID}")
    public APIResponse keggQuery(@PathParam("keggID") String keggID) {

        SubmitJob job = new SubmitJob();
        job.createCommand(keggID);
        String output = job.executeCommand();
        APIResponse response = new APIResponse();
        response.setResponse(job.getResponse());
        response.setMessage(output);
        return response;

    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/add")
    public APIResponse addJob() {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper addJob = null;
        APIResponse response = new APIResponse();
        response.setResponse("error");
        response.setMessage("error");

        try {
            addJob = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Connection connect = addJob.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
        boolean b = addJob.insertJob("DSADAD", 123);
        if (b == true) {

            response.setResponse("done");
            response.setMessage("error");
        } else {
            response.setResponse("errir");
            response.setMessage("eoor");

        }
        return response;
    }

    @POST
    @Path("/atom_atom_mapping")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public APIResponse uploadFile(
            @FormDataParam("rxn") InputStream uploadedInputStream,
            @FormDataParam("rxn") FormDataContentDisposition fileDetail) {

        FileUploadUtility uploadFile = new FileUploadUtility(fileDetail.getFileName());
        APIResponse response = new APIResponse();
        //response.setMessage(null);
        //response.
        // save it
        boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStream);
        String filePath = uploadFile.getFileLocation();
        SubmitJob rxnMappingJob = new SubmitJob();
        if (uploadedSucessful) {
            /* Submit job to farm
            TODO: Check if rxn file is all balanced!
            */
            
            AtomAtomMappingParser parser = new AtomAtomMappingParser(filePath);
            String readFile = parser.readFileInString();
            if (readFile!=null){
                String[] pars = parser.getAllSections(readFile);
                response.setMessage("read");
                response.setResponse("read");
                return response;
            }
            else{
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,"REad emtpy ", "error");
            }
        }
        else {
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,"Could not upload file", "error");
        }

    }

  
   

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{subResources:.*}")
    public APIResponse getStuff() {
        throw new ErrorResponse(Status.NOT_FOUND, "URL NOT FOUND", "error");
    }

}
