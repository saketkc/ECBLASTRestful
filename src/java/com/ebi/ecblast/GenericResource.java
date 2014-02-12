/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebi.ecblast;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import utility.APIResponse;
import utility.SubmitJob;

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
    public APIResponse getQuery(){
            throw new ErrorInfo(Status.BAD_REQUEST,  " No Params supplied", "error");
    }
    
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/query/kegg/{keggID}")
    public APIResponse keggQuery(@PathParam("keggID") String keggID){
        
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
    @Path("{subResources:.*}")
    public APIResponse getStuff() {
        throw new ErrorInfo(Status.NOT_FOUND, "URL NOT FOUND", "error");
    }

}
