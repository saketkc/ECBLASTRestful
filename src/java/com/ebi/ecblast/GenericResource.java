/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebi.ecblast;

import com.ebi.ecblast.db.DatabaseConfiguration;
import com.ebi.ecblast.db.JobsQueryWrapper;
import java.io.InputStream;
import javax.ws.rs.Consumes;
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
import com.ebi.ecblast.utility.APIResponse;
import com.ebi.ecblast.utility.SubmitJob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST Web Service
 *
 * @author saket
 */
@Path("/")
public class GenericResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of GenericResource
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse getHtml() {
        //TODO return proper representation object
        APIResponse response = new APIResponse();
        response.setResponse("test");
        response.setMessage("test");

        return response;
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
     * PUT method for updating or creating an instance of GenericResource
     *
     * @param jobID
     * @return an HTTP response with content of the updated or created resource.
     * @throws com.ebi.ecblast.ErrorInfo
     */
    @Path("/status/{jobID}")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse getJobStatus(@PathParam("jobID") String jobID) throws ErrorInfo {
        try {
            Integer.parseInt(jobID);
        } catch (NumberFormatException e) {
            throw new ErrorInfo(Status.BAD_REQUEST, jobID + " is not a valid jobID", "error");
        }
        APIResponse response = new APIResponse();
        if (!"123".equals(jobID)) {

            response.setResponse("test");
            response.setMessage("test");
            return response;
        } else {
            throw new ErrorInfo(Status.BAD_REQUEST, jobID + " is not a valid jobID", "error");
        }
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/query.*")
    public APIResponse getQuery() {
        throw new ErrorInfo(Status.BAD_REQUEST, " No Params supplied", "error");
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
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Connection connect = addJob.connect();
        } catch (SQLException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GenericResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
           boolean b = addJob.insertJob("DSADAD", 123);
           if (b==true){
        
        response.setResponse("done");
        response.setMessage("error");
           }
           else{
        response.setResponse("errir");
        response.setMessage("eoor");
               
           }
        return response;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("{subResources:.*}")
    public APIResponse getStuff() {
        throw new ErrorInfo(Status.NOT_FOUND, "URL NOT FOUND", "error");
    }

}
