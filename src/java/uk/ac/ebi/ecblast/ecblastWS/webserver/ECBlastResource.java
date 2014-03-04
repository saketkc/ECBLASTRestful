/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.webserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import java.io.InputStream;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitAtomAtomMappingJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitCompareReactionsJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitTransformationJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitSearchJob;
import uk.ac.ebi.ecblast.ecblastWS.parser.AtomAtomMappingParser;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailResults;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailText;
import uk.ac.ebi.ecblast.ecblastWS.utility.GenericResponse;

/**
 * REST Web Service
 *
 * @author saket
 */
@Path("/")
@Consumes("multipart/related")
public class ECBlastResource {

    @Context
    private UriInfo context;

    @GET
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ErrorResponse getHtml() {
        //TODO return proper representation object
        throw new ErrorResponse(Status.FORBIDDEN, "Forbidden");
    }

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse putHtml() {
        //TODO return proper representation object
        throw new ErrorResponse(Status.FORBIDDEN, "Forbidden");
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse postHtml() {
        //TODO return proper representation object
        throw new ErrorResponse(Status.FORBIDDEN, "Forbidden");
    }

    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/aam")	 	   
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public GenericResponse atomAtomMappingRXN(
            @DefaultValue("") @FormDataParam("q") InputStream uploadedInputStreamRXN,
            @DefaultValue("") @FormDataParam("q") FormDataContentDisposition fileDetailRXN,
            @DefaultValue("") @FormDataParam("q") String smileQuery,
            @DefaultValue("") @FormDataParam("Q") String fileFormat,
            @FormDataParam("email") String emailID) {

        if (fileDetailRXN == null && uploadedInputStreamRXN == null && smileQuery == "") {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Inputs");
        }
        if (!"RXN".equals(fileFormat) && !"SMI".equals(fileFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + fileFormat);
        }
        

        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        SubmitAtomAtomMappingJob rxnMappingJob = new SubmitAtomAtomMappingJob();
        String jID;
        String userDirectory;

        if ("RXN".equals(fileFormat)) {
           
            FileUploadUtility uploadFile = new FileUploadUtility( fileDetailRXN.getFileName() , uniqueID);
            boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStreamRXN);
            userDirectory = uploadFile.getUserDirectory();
            String userFilePath = uploadFile.getFileLocation();
            if (!uploadedSucessful) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            }

            rxnMappingJob.createCommand(uniqueID, userDirectory, fileFormat, userFilePath);

        }

        /* Submit job to farm
         TODO: Check if rxn file is all balanced!
         */
        if ("SMI".equals(fileFormat)) {
            FileUploadUtility uploadFile = new FileUploadUtility(uniqueID);
            userDirectory = uploadFile.getUserDirectory();
            rxnMappingJob.createCommand(uniqueID, userDirectory, fileFormat, smileQuery);
        }

        jID = rxnMappingJob.executeCommand();
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || "".equals(jID)) {
            System.out.println(rxnMappingJob.getCommand());
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");

        }
        int jobID = 0;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            System.out.println(rxnMappingJob.getCommand());

            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");
        }

        if (jobID > 0) {
            DatabaseConfiguration dbconfig = new DatabaseConfiguration();
            JobsQueryWrapper addJob = null;

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
                int b;
                String targetFileName = null;
                String jobType = "atom_atom_mapping" + "_" + fileFormat.toLowerCase();
                b = addJob.insertJob(uniqueID, jobID, fileDetailRXN.getFileName(), targetFileName, emailID, jobType);
                if (b >= 1) {
                    response.setMessage(rxnMappingJob.getResponse());
                    response.setJobID(uniqueID);
                } else {
                    response.setMessage("error");
                    response.setResponse("erro submitting to database");
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            }

        }

        return response;

    }

    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/compare")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse compareReactions(
            @FormDataParam("q") InputStream uploadedInputStreamQuery,
            @FormDataParam("q") FormDataContentDisposition fileDetailQuery,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String queryFormat,
            @FormDataParam("T") String targetFormat,
            @FormDataParam("t") InputStream uploadedInputStreamTarget,
            @FormDataParam("t") FormDataContentDisposition fileDetailTarget,
            @FormDataParam("t") String smileTarget,
            @FormDataParam("email") String emailID
    ) {

        if ((uploadedInputStreamQuery == null && smileQuery == null && fileDetailQuery == null) || (uploadedInputStreamTarget == null && smileTarget == null
                && fileDetailTarget == null  && smileTarget == null)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Reaction File");
        }

        if (!"RXN".equals(queryFormat) && !"SMI".equals(queryFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + queryFormat);
        }
        if (!"RXN".equals(targetFormat) && !"SMI".equals(targetFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + targetFormat);
        }

        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        FileUploadUtility fileUpload = new FileUploadUtility(uniqueID);
        String userDirectory = fileUpload.getUserDirectory();
        String userFilePathQuery = null;
        String userFilePathTarget = null;
        FileUploadUtility uploadFileTarget = null;
        FileUploadUtility uploadFileQuery = null;
        if ("RXN".equals(queryFormat)) {
            uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);
            boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
            userFilePathQuery = uploadFileQuery.getFileLocation();
            if (!uploadedSucessfulQuery) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");

            }
        }

        if ("RXN".equals(targetFormat)) {

            uploadFileTarget = new FileUploadUtility(fileDetailTarget.getFileName(), uniqueID);
            boolean uploadedSucessfulTarget = uploadFileTarget.writeToFile(uploadedInputStreamTarget);
            userFilePathTarget = uploadFileTarget.getFileLocation();
            if (!uploadedSucessfulTarget) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");

            }
        }
        String query = null;
        String target = null;
        if ("RXN".equals(queryFormat)) {
            query = uploadFileQuery.getFileLocation();
        } else if ("SMI".equals(queryFormat)) {
            query = smileQuery;
        }
        if ("RXN".equals(targetFormat)) {
            target = uploadFileTarget.getFileLocation();
        } else if ("SMI".equals(targetFormat)) {
            target = smileTarget;
        }

        /* Submit job to farm
         TODO: Check if rxn file is all balanced!
         */
        SubmitCompareReactionsJob compareJob = new SubmitCompareReactionsJob();

        compareJob.createCommand(uniqueID, userDirectory, queryFormat, query, targetFormat, target);
        String jID = compareJob.executeCommand();
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || "".equals(jID)) {
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + compareJob.getCommand());

        }
        int jobID;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + compareJob.getCommand());

        }

        if (jobID > 0) {
            DatabaseConfiguration dbconfig = new DatabaseConfiguration();
            JobsQueryWrapper addJob = null;

            try {
                addJob = new JobsQueryWrapper(dbconfig.getDriver(),
                        dbconfig.getConnectionString(),
                        dbconfig.getDBName(),
                        dbconfig.getDBUserName(),
                        dbconfig.getDBPassword());

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Connection connect = addJob.connect();
                int b;
                b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), null,
                        emailID, "compare_reactions");
                if (b >= 1) {
                    response.setMessage(compareJob.getResponse());
                    response.setJobID(uniqueID);

                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            }

        }

        return response;

    }

    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/transform")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse genericMapping(
            @FormDataParam("q") InputStream uploadedInputStreamQuery,
            @FormDataParam("q") FormDataContentDisposition fileDetailQuery,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String queryFormat,
            @DefaultValue("strict") @FormDataParam("type") String transformType,
            @FormDataParam("c") String c,
            @FormDataParam("email") String emailID
    ) {
        if (fileDetailQuery == null && uploadedInputStreamQuery == null && smileQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "No reaction File");
        }
        if (!"RXN".equals(queryFormat) && !"SMI".equals(queryFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Only RXN,SMI are allowed");
        }
        if (c == null || c == "") {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "c required");
        }
        Integer hits;
        try {
            hits = Integer.parseInt(c);
        } catch (NumberFormatException ex) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "c should be integer");
        }

        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        String query = null;
        FileUploadUtility uploadFileQuery = new FileUploadUtility(uniqueID);
        if ("RNX".equals(queryFormat)) {
            uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);
            query = uploadFileQuery.getFileLocation();
            boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
            if (!uploadedSucessfulQuery) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            }
        }

        if ("SMI".equals(queryFormat)) {
            query = smileQuery;
        }

        String userDirectory = uploadFileQuery.getUserDirectory();
        String jID = null;
        SubmitTransformationJob matchingJob = new SubmitTransformationJob();
        if(transformType==null || "".equals(transformType)){
            transformType = "strict";
        }
        if ("strict".equals(transformType)) {

            matchingJob.createCommandStrict(uniqueID, userDirectory, queryFormat, query, c, queryFormat);

            jID = matchingJob.executeCommand();
        } else if ("generic".equals(transformType)) {

            matchingJob.createCommand(uniqueID, userDirectory, queryFormat, query, c, queryFormat);
                        
            jID = matchingJob.executeCommand();
        }
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || jID == "") {
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + matchingJob.getCommand());

        }
        int jobID;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
          
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");

        }

        if (jobID > 0) {
            DatabaseConfiguration dbconfig = new DatabaseConfiguration();
            JobsQueryWrapper addJob = null;

            try {
                addJob = new JobsQueryWrapper(dbconfig.getDriver(),
                        dbconfig.getConnectionString(),
                        dbconfig.getDBName(),
                        dbconfig.getDBUserName(),
                        dbconfig.getDBPassword());

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Connection connect = addJob.connect();
                int b;
                String transform = "transform" + "_" + transformType;
                b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), null, emailID, transformType);
                if (b >= 1) {
                    response.setMessage(matchingJob.getResponse());
                    response.setJobID(uniqueID);

                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            }

        }

        return response;

    }

    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/search")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse search(@FormDataParam("q") InputStream uploadedInputStreamRXN,
            @FormDataParam("q") FormDataContentDisposition fileDetailRXN,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String fileFormat,
            @FormDataParam("type") String searchType,
            @DefaultValue("10") @FormDataParam("c") String c,
            @FormDataParam("email") String emailID) {

        if (fileDetailRXN == null && uploadedInputStreamRXN == null && smileQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Inputs");
        }
        if (!"RXN".equals(fileFormat) && !"SMI".equals(fileFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + fileFormat);
        }
        if (!"bond".equals(searchType) && !"centre".equals(searchType) && !("structure").equals(searchType)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Searh  Not Supported" + searchType);
        }

        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        SubmitSearchJob searchJob = new SubmitSearchJob();
        String jID;
        String userDirectory = null;
        String query = null;
        FileUploadUtility uploadFile = null;
        if ("RXN".equals(fileFormat)) {
            uploadFile = new FileUploadUtility(fileDetailRXN.getFileName(), uniqueID);
            boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStreamRXN);

            String userFilePath = uploadFile.getFileLocation();
            if (!uploadedSucessful) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            }

            query = userFilePath;

        }

        if (("SMI").equals(fileFormat)) {
            uploadFile = new FileUploadUtility(uniqueID);

            query = smileQuery;
        }
        userDirectory = uploadFile.getUserDirectory();

        /* Submit job to farm
         TODO: Check if rxn file is all balanced!
         */
        searchJob.createCommand(uniqueID, userDirectory, fileFormat, query, searchType, c);

        jID = searchJob.executeCommand();
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || "".equals(jID)) {
            System.out.println(searchJob.getCommand());
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node " + searchJob.getCommand());

        }
        int jobID = 0;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            System.out.println(searchJob.getCommand());

            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");
        }

        if (jobID > 0) {
            DatabaseConfiguration dbconfig = new DatabaseConfiguration();
            JobsQueryWrapper addJob = null;

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
                int b;
                String targetFileName = null;
                String jobType = "search";

                b = addJob.insertJob(uniqueID, jobID, fileDetailRXN.getFileName(), targetFileName, emailID, jobType);
                if (b >= 1) {
                    response.setMessage(searchJob.getResponse());
                    response.setJobID(uniqueID);
                } else {
                    response.setMessage("error");
                    response.setResponse("erro submitting to database");
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            }

        }

        return response;

    }

    /**
     * PUT method for updating or creating an instance of ECBlastResource
     *
     * @param jobID
     * @return an HTTP response with content of the updated or created resource.
     */
    @Path("/status/{jobID}")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public GenericResponse getJobStatus(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        GenericResponse response = new GenericResponse();
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error connecting to the databse");
        }

        try {
            Connection connect = job.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
        String status = job.getJobStatus(uniqueID);
        if (status == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, uniqueID + " not found");
        }
        response.setResponse(status);

        return response;

    }

    /* Send status of provided jobId
     @param jobID
     */
    @Path("/result/{jobID}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getText(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        GenericResponse response = new GenericResponse();
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error connecting to the databse");
        }

        try {
            Connection connect = job.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
        String status = job.getJobStatus(uniqueID);
        if (status == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, uniqueID + " not found");
        }
        if ("pending".equals(status)) {

            response.setResponse("Job ID " + uniqueID + "is pending");

        } else if ("fail".equals(status)) {

            response.setResponse("Job ID " + uniqueID + " failed");
        } else if ("done".equals(status)) {
            response.setAtomAtomMappingTextLink("/result/" + uniqueID + "/text");
            response.setAtomAtomMappingImageLink("/result/" + uniqueID + "/image");
            response.setAtomAtomMappingXMLLink("/result/" + uniqueID + "/xml");

        }
        response.setStatus(status);

        return response;

    }

    /*Send text response back for the result
    
     */
    @Path("/result/{jobID}/text")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getResultText(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        GenericResponse response = new GenericResponse();
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/Result.txt";
        //String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + uniqueID + "__text.log";
        AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
        String contents = parser.readFileInString();
        response.setAtomatomMappingResultText(contents);
        return response;
    }

    @Path("/result/{jobID}/mapped")
    @GET
    
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getMappedText(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        GenericResponse response = new GenericResponse();
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error connecting to the databse");
        }

        try {
            Connection connect = job.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            return response;
        }
        String jobType = job.getJobType(uniqueID);
        String filepath = null;
        AtomAtomMappingParser parser;
        String contents;
        if ("atom_atom_mapping_rxn".equals(jobType) || "compare_rxn".equals(jobType)) {
            String fileName = job.getQueryFileName(uniqueID);
            filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + "ECBLAST" + "_"
                    + fileName + "_Query" + ".rxn";
            parser = new AtomAtomMappingParser(filepath);
            contents = parser.readFileInString();
            response.setAtomatomMappingResultText(contents);

        } else if ("atom_atom_mapping_smi".equals(jobType) || "compare_rxn".equals(jobType)) {
            String fileName = job.getQueryFileName(uniqueID);
            filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + "ECBLAST" + "_"
                    + "smiles" + "_Query" + ".rxn";
            System.out.println("********" + filepath);
            parser = new AtomAtomMappingParser(filepath);
            contents = parser.readFileInString();
            response.setAtomatomMappingResultText(contents);
        } else if ("compare_reactions".equals(jobType)) {
            String fileNameQuery = job.getQueryFileName(uniqueID);
            String fileNameTarget = job.getTargetFileName(uniqueID);
            /* If null was returned there was no file submitted
             and most probably ythe format was smiles
             */
            if (fileNameQuery == null) {
                fileNameQuery = "smiles";
            }

            if (fileNameTarget == null) {
                fileNameTarget = "smiles";
            }
            String filepathQuery = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + "ECBLAST" + "_"
                    + "smiles" + "_Query" + ".rxn";
            parser = new AtomAtomMappingParser(filepathQuery);
            String queryContents = parser.readFileInString();

            String filepathTarget = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + "ECBLAST" + "_"
                    + "smiles" + "_Target" + ".rxn";
            parser = new AtomAtomMappingParser(filepathTarget);
            String targetContents = parser.readFileInString();
            System.out.println("***********TARGET" + targetContents);
            System.out.println("***********TARGET" + queryContents);

            response.setQueryMappedText(queryContents);
            response.setTargetMappedText(targetContents);
        } else if ("search".equals(jobType)) {
            String filepathQuery = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + "ECBLAST" + "_"
                    + "smiles" + "_Query" + ".rxn";
            parser = new AtomAtomMappingParser(filepathQuery);
            String queryContents = parser.readFileInString();
            queryContents.replaceAll("Xml File saved!s", "");
            response.setSearchMappedText(queryContents);
        }

        return response;
    }

    /**
     * ****************TODO***************************
     *
     * @param uniqueID
     * @return
     * @throws ErrorResponse
     */
    @Path("/result/{jobID}/xml")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response getResultXml(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        GenericResponse response = new GenericResponse();
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/Result.xml";
                //+ "/" + uniqueID + "__xml.log";
        AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
        String contents = parser.readFileInString();
        
        return Response.ok(contents).build();
    }

    @Path("/xml")
    @GET
    @Produces({MediaType.APPLICATION_XML})

    public Response getXML() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><EC_BLAST>\n"
                + "  <ANNOTATION>\n"
                + "    <FINGERPRINTS BC=\"1\">\n"
                + "      <FORMED_CLEAVED>[C%C:5.0, C%O:1.0, C-C:2.0, C-H:5.0, H-O:1.0]</FORMED_CLEAVED>\n"
                + "      <ORDER_CHANGED>[C%C*C=C:3.0, C-C*C=C:1.0]</ORDER_CHANGED>\n"
                + "      <STEREO_CHANGED>[C(R/S):1.0]</STEREO_CHANGED>\n"
                + "    </FINGERPRINTS>\n"
                + "    <FINGERPRINTS RC=\"2\">\n"
                + "      <CENTRE>[[#6]:24.0, [#6]-1-[#6]-[#6]-1:1.0, [#6]-1-[#6]-[#8]-1:1.0, [#6]-1-[#6]-[#8]-1&gt;&gt;[#6]-[#8]:1.0, [#6]-[#6@@H](-[#6])-[#6]:1.0, [#6]-[#6@@H](-[#6])-[#6]1-[#6]-[#6]-[#6][C@]1([#6])[#6]:1.0, [#6]-[#6@@H](-[#6])-[#8]:2.0, [#6]-[#6@@H]-1-[#6]-[#8]-1:1.0, [#6]-[#6@@H]-1-[#6]-[#8]-1&gt;&gt;[#6]-[#6@@H](-[#6])-[#8]:1.0, [#6]-[#6@H]1-[#8]C1([#6])[#6]:2.0, [#6]-[#6@H]1-[#8]C1([#6])[#6]&gt;&gt;[#6]-[#6@@H](-[#6])-[#8]:1.0, [#6]-[#6@H]1-[#8]C1([#6])[#6]&gt;&gt;[#6]-[#6](-[#6])C([#6])([#6])[#6@H](-[#6])-[#8]:1.0, [#6]-[#6]:3.0, [#6]-[#6](-[#6])-[#6]:3.0, [#6]-[#6](-[#6])=[#6]:6.0, [#6]-[#6](-[#6])=[#6]&gt;&gt;[#6]-[#6@@H](-[#6])-[#6]:1.0, [#6]-[#6](-[#6])=[#6]&gt;&gt;[#6]-[#6](-[#6])-[#6]:1.0, [#6]-[#6](-[#6])=[#6]&gt;&gt;[#6][C@@]([#6])([#6])[#6]:2.0, [#6]-[#6](-[#6])=[#6]&gt;&gt;[#6][C@@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6](-[#6])=[#6]&gt;&gt;[#6][C@@]1([#6])[#6][C@]1([#6])[#6]:1.0, [#6]-[#6](-[#6])C([#6])([#6])[#6@H](-[#6])-[#8]:1.0, [#6]-[#6](-[#6])[C@]1([#6])[#6]-[#6]-[#6][C@@]1([#6])[#6]:1.0, [#6]-[#6]-[#6@@H](-[#6])-[#6](-[#6])-[#6]:1.0, [#6]-[#6]-[#6@H](-[#8])C([#6])([#6])[#6]:1.0, [#6]-[#6]-[#6@H]1-[#8]C1([#6])[#6]:1.0, [#6]-[#6]-[#6@H]1-[#8]C1([#6])[#6]&gt;&gt;[#6]-[#6]-[#6@H](-[#8])C([#6])([#6])[#6]:2.0, [#6]-[#6]-[#6@H]1-[#8]C1([#6])[#6]&gt;&gt;[#6]-[#6]-[#6]1[C@@]2([#6]-[#6]2)[#6]-[#6]-[#6@H](-[#8])C1([#6])[#6]:1.0, [#6]-[#6]-[#6](C([#6])([#6])[#6])[C@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6]-[#6]([C@]([#6])([#6])[#6])[C@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6]-[#6]-[#6@H]1-[#8]C1([#6])[#6]&gt;&gt;[#6]-[#6]1-[#6]-[#6]-[#6]-[#6@H](-[#8])C1([#6])[#6]:1.0, [#6]-[#6]-[#6]=[#6](-[#6])-[#6]:4.0, [#6]-[#6]-[#6]=[#6](-[#6])-[#6]&gt;&gt;[#6]-[#6@@H](-[#6])-[#6]1-[#6]-[#6]-[#6][C@]1([#6])[#6]:1.0, [#6]-[#6]-[#6]=[#6](-[#6])-[#6]&gt;&gt;[#6]-[#6]-[#6](C([#6])([#6])[#6])[C@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6]-[#6]=[#6](-[#6])-[#6]&gt;&gt;[#6]-[#6][C@]1([#6])[#6](-[#6])-[#6]-[#6][C@@]1([#6])[#6]:1.0, [#6]-[#6]-[#6]=[#6](-[#6])-[#6]&gt;&gt;[#6]-[#6][C@]1([#6][C@]1([#6])[#6])[#6](-[#6])-[#6]:1.0, [#6]-[#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]-[#6]&gt;&gt;[#6]-[#6]-[#6]-[#6@@H](-[#6])-[#6]1-[#6]-[#6]-[#6][C@]1([#6])[#6]:1.0, [#6]-[#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]-[#6]&gt;&gt;[#6]-[#6]-[#6]1[C@@]2([#6]-[#6]2)[#6]-[#6][C@]2([#6])[#6](-[#6])-[#6]-[#6][C@@]12[#6]:1.0, [#6]-[#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]-[#6]&gt;&gt;[#6]-[#6][C@@]12[#6][C@@]11[#6]-[#6]-[#6]C([#6])([#6])[#6]1-[#6]-[#6]-[#6]2-[#6]:1.0, [#6]-[#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]-[#6]&gt;&gt;[#6][C@]12[#6][C@]11[#6]-[#6][C@]3([#6])[#6]-[#6]-[#6][C@@]3([#6])[#6]1-[#6]-[#6]-[#6]2:1.0, [#6]-[#6]-[#6]\\[#6]=[#6](/[#6])-[#6]-[#6]&gt;&gt;[#6]-[#6@@H](-[#6])-[#6]1-[#6]-[#6][C@@]2([#6])[#6](-[#6])-[#6]-[#6]-[#6][C@]12[#6]:1.0, [#6]-[#6]-[#6]\\[#6]=[#6](/[#6])-[#6]-[#6]&gt;&gt;[#6]-[#6]-[#6@@H](-[#6])-[#6]1-[#6]-[#6][C@@]([#6])([#6])[C@]1([#6])[#6]-[#6]:1.0, [#6]-[#6]-[#6]\\[#6]=[#6](/[#6])-[#6]-[#6]&gt;&gt;[#6]-[#6][C@]12[#6][C@]11[#6]-[#6]-[#6][C@]([#6])([#6])[#6]1-[#6]-[#6]-[#6]2-[#6]:1.0, [#6]-[#6]-[#6]\\[#6]=[#6](/[#6])-[#6]-[#6]&gt;&gt;[#6][C@@]12[#6][C@@]11[#6]-[#6]-[#6@H](-[#8])C([#6])([#6])[#6]1-[#6]-[#6]-[#6]2:1.0, [#6]-[#6]=[#6]:4.0, [#6]-[#6]=[#6]&gt;&gt;[#6]-[#6](-[#6])-[#6]:2.0, [#6]-[#6]=[#6]&gt;&gt;[#6][C@@]([#6])([#6])[#6]:1.0, [#6]-[#6]=[#6]&gt;&gt;[#6][C@@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6]&gt;&gt;[#6]-1-[#6]-[#6]-1:1.0, [#6]-[#6]&gt;&gt;[#6]-[#6]:1.0, [#6]-[#6][C@@]1([#6][C@@]1([#6])[#6])[#6](-[#6])-[#6]:1.0, [#6]-[#6][C@]1([#6])[#6](-[#6])-[#6]-[#6][C@@]1([#6])[#6]:1.0, [#6]-[#6][C@]1([#6][C@]1([#6])[#6])[#6](-[#6])-[#6]:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]:4.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6](-[#6])[C@]1([#6])[#6]-[#6]-[#6][C@@]1([#6])[#6]:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6]-[#6@@H](-[#6])-[#6](-[#6])-[#6]:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6]-[#6]([C@]([#6])([#6])[#6])[C@]1([#6])[#6]-[#6]1:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6][C@@]1([#6][C@@]1([#6])[#6])[#6](-[#6])-[#6]:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6][C@]1([#6])[#6](-[#6])-[#6]-[#6][C@@]1([#6])[#6]:1.0, [#6]-[#6]\\[#6](-[#6])=[#6]\\[#6]&gt;&gt;[#6]-[#6][C@]12[#6][C@@]1([#6]-[#6])[#6](-[#6])-[#6]-[#6]-[#6]2-[#6]:1.0, [#6]-[#8]:1.0, [#6]C([#6])([#6])[#6]:1.0, [#6]C1([#6])[#6]-[#8]1:1.0, [#6]C1([#6])[#6]-[#8]1&gt;&gt;[#6]C([#6])([#6])[#6]:1.0, [#6][C@@]([#6])([#6])[#6]:3.0, [#6][C@@]1([#6])[#6]-[#6]1:2.0, [#6][C@@]1([#6])[#6][C@]1([#6])[#6]:1.0, [#8]:2.0]</CENTRE>\n"
                + "    </FINGERPRINTS>\n"
                + "    <MAPPING STATUS=\"SELECTED\">\n"
                + "      <AAM>[H:59][C:10](=[C:9]([CH3:28])[CH2:8][CH2:7][CH:6]=[C:5]([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31])[CH2:11][CH2:12][C:13]([H:54])=[C:14]([CH3:15])[CH2:16][CH2:17][CH:18]=[C:19]([CH2:20][H:37])[CH2:21][CH2:22][CH:23]1[O:27][C:24]1([CH3:25])[CH3:26]&gt;&gt;[H:37][O:27][CH:23]1[CH2:22][CH2:21][C:19]23[CH2:20][C:13]43[CH2:12][CH2:11][C:10]5([CH3:15])[CH:6]([CH2:7][CH2:8][C:9]5([CH3:28])[C:14]4([H:54])[CH2:16][CH2:17][CH:18]2[C:24]1([CH3:26])[CH3:25])[C:5]([H:59])([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:31])[CH3:30]</AAM>\n"
                + "    </MAPPING>\n"
                + "    <MAPPING ALGORTIHM=\"Local Minimization Model\">\n"
                + "      <AAM>[H:59][C:10](=[C:9]([CH3:28])[CH2:8][CH2:7][CH:6]=[C:5]([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31])[CH2:11][CH2:12][C:13]([H:54])=[C:14]([CH:15]([H:33])[H:34])[CH2:16][CH2:17][CH:18]=[C:19]([CH3:20])[CH2:21][CH2:22][CH:23]1[O:27][C:24]1([CH3:25])[CH3:26]&gt;&gt;[H:54][O:27][CH:23]1[CH2:22][CH2:21][C:5]23[C:9]([H:33])([H:34])[C:10]43[CH2:11][CH2:12][C:13]5([CH3:28])[CH:18]([CH2:8][CH2:7][C:14]5([CH3:29])[CH:6]4[CH2:16][CH2:17][CH:15]2[C:24]1([CH3:26])[CH3:25])[C:19]([H:59])([CH3:20])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31]</AAM>\n"
                + "      <SCORE>37</SCORE>\n"
                + "      <FRAGMENTS>0</FRAGMENTS>\n"
                + "      <CHANGES>37</CHANGES>\n"
                + "      <ENERGY>9,199.00</ENERGY>\n"
                + "      <DELTA>12,600.00</DELTA>\n"
                + "    </MAPPING>\n"
                + "    <MAPPING ALGORTIHM=\"Global Maximization Model\">\n"
                + "      <AAM>[H:59][C:10](=[C:9]([CH3:28])[CH2:8][CH2:7][CH:6]=[C:5]([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31])[CH2:11][CH2:12][C:13]([H:54])=[C:14]([CH:15]([H:33])[H:34])[CH2:16][CH2:17][CH:18]=[C:19]([CH3:20])[CH2:21][CH2:22][CH:23]1[O:27][C:24]1([CH3:25])[CH3:26]&gt;&gt;[H:54][O:27][CH:23]1[CH2:22][CH2:21][C:5]23[C:9]([H:33])([H:34])[C:10]43[CH2:11][CH2:12][C:13]5([CH3:28])[CH:18]([CH2:8][CH2:7][C:14]5([CH3:29])[CH:6]4[CH2:16][CH2:17][CH:15]2[C:24]1([CH3:26])[CH3:25])[C:19]([H:59])([CH3:20])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31]</AAM>\n"
                + "      <SCORE>37</SCORE>\n"
                + "      <FRAGMENTS>0</FRAGMENTS>\n"
                + "      <CHANGES>37</CHANGES>\n"
                + "      <ENERGY>9,199.00</ENERGY>\n"
                + "      <DELTA>12,600.00</DELTA>\n"
                + "    </MAPPING>\n"
                + "    <MAPPING ALGORTIHM=\"Max-Mixture Model\">\n"
                + "      <AAM>[H:59][C:10](=[C:9]([CH3:28])[CH2:8][CH2:7][CH:6]=[C:5]([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31])[CH2:11][CH2:12][C:13]([H:54])=[C:14]([CH3:15])[CH2:16][CH2:17][CH:18]=[C:19]([CH2:20][H:37])[CH2:21][CH2:22][CH:23]1[O:27][C:24]1([CH3:25])[CH3:26]&gt;&gt;[H:37][O:27][CH:23]1[CH2:22][CH2:21][C:19]23[CH2:20][C:13]43[CH2:12][CH2:11][C:10]5([CH3:15])[CH:6]([CH2:7][CH2:8][C:9]5([CH3:28])[C:14]4([H:54])[CH2:16][CH2:17][CH:18]2[C:24]1([CH3:26])[CH3:25])[C:5]([H:59])([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:31])[CH3:30]</AAM>\n"
                + "      <SCORE>18</SCORE>\n"
                + "      <FRAGMENTS>0</FRAGMENTS>\n"
                + "      <CHANGES>18</CHANGES>\n"
                + "      <ENERGY>2,750.00</ENERGY>\n"
                + "      <DELTA>5,294.00</DELTA>\n"
                + "    </MAPPING>\n"
                + "    <MAPPING ALGORTIHM=\"Ring Conservation Model\">\n"
                + "      <AAM>[H:59][C:10](=[C:9]([CH3:28])[CH2:8][CH2:7][CH:6]=[C:5]([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:30])[CH3:31])[CH2:11][CH2:12][C:13]([H:54])=[C:14]([CH3:15])[CH2:16][CH2:17][CH:18]=[C:19]([CH2:20][H:37])[CH2:21][CH2:22][CH:23]1[O:27][C:24]1([CH3:25])[CH3:26]&gt;&gt;[H:37][O:27][CH:23]1[CH2:22][CH2:21][C:19]23[CH2:20][C:13]43[CH2:12][CH2:11][C:10]5([CH3:15])[CH:6]([CH2:7][CH2:8][C:9]5([CH3:28])[C:14]4([H:54])[CH2:16][CH2:17][CH:18]2[C:24]1([CH3:26])[CH3:25])[C:5]([H:59])([CH3:29])[CH2:4][CH2:3][CH:2]=[C:1]([CH3:31])[CH3:30]</AAM>\n"
                + "      <SCORE>18</SCORE>\n"
                + "      <FRAGMENTS>0</FRAGMENTS>\n"
                + "      <CHANGES>18</CHANGES>\n"
                + "      <ENERGY>2,750.00</ENERGY>\n"
                + "      <DELTA>5,294.00</DELTA>\n"
                + "    </MAPPING>\n"
                + "  </ANNOTATION>\n"
                + "</EC_BLAST>";
        return Response.ok(xml).build();
    }

    @Path("/result/{jobID}/image")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getResultImage(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String userFolder = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/";
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Connection connect = job.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);

        }
        String jobType = job.getJobType(uniqueID);
        String filePrefix;
        String imgFileName = null;
        if ("atom_atom_mapping_rxn".equals(jobType)) {

            filePrefix = job.getQueryFileName(uniqueID);
            imgFileName = userFolder + "ECBLAST" + "_" + filePrefix + "_rxn.png";
        } else if ("atom_atom_mapping_smi".equals(jobType)) {

            imgFileName = userFolder + "ECBLAST" + "_" + "smiles_Query" + "_rxn.png";
        }
        
        else if("compare_reactions".equals(jobType)){
            
            imgFileName = userFolder + "Target_Query_combined.png";
            
            
        }
       
        try {
            BufferedImage img = null;

            try {
                img = ImageIO.read(new File(imgFileName));
            } catch (IOException e) {
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            byte[] imageData = baos.toByteArray();
            return Response.ok(new ByteArrayInputStream(imageData)).build();

        } catch (IOException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @GET
    @Path("/pending_jobs")
    @Produces({MediaType.APPLICATION_JSON})
    public APIResponse getPending() {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper jobWrapper = null;
        APIResponse response = new APIResponse();
        response.setResponse("error");
        response.setMessage("error");

        try {
            jobWrapper = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Connection connect = jobWrapper.connect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        }
        String pendingJobs = jobWrapper.getPendingJobIDs();
        response.setMessage("success");
        response.setResponse(pendingJobs);
        return response;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/email")
    public APIResponse sendEmail() {
        APIResponse response = new APIResponse();
        EmailResults results = new EmailResults("test", "saketc@ebi.ac.uk", "subject", "message", false);
        results.sendMail();
        response.setMessage("test");
        response.setResponse("test");
        return response;
    }

    @GET
    @Produces({MediaType.APPLICATION_XML})
    @Path("/updateJobStatus/{uniqueID}/{status}")
    public void updateStatus(@PathParam("uniqueID") String uniqueID, @PathParam("status") String status) {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        GenericResponse response = new GenericResponse();
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Connection connect = job.connect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        }
        System.out.println("STATUS   " + status);
        status = status.trim();
        if ("done".equals(status)) {
            /* Send email to user with the file            
             TODO: Make the email method generic
             */
            String email = job.getEmailFromUUID(uniqueID);
            if (email != null) {

                EmailText emailText = new EmailText();

                ConfigParser configparser = new ConfigParser();
                Properties prop = configparser.getConfig();
                String directory = prop.getProperty("results_upload_directory") + "/";
                String files[] = {directory + uniqueID + ".zip"};
                EmailResults results = new EmailResults(uniqueID, email, emailText.getAtomAtomMappingSubject(), emailText.getAtomAtomMappingEmail(), true);
                results.setFilepaths(files);

                String result = results.sendMail();
                System.out.println(results.zipFilePath);

            } else {
                System.out.println("No email Supplied, No action required");
            }

        }
        boolean b = job.updateJob(uniqueID, status);
        try {
            job.disconnect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

    }

    @GET
    @Produces("image/png")
    @Path("/images")
    public Response sendImage() throws IOException {
        BufferedImage img = null;
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();
        try {
            img = ImageIO.read(new File("/home/saket/a.png"));
        } catch (IOException e) {
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] imageData = baos.toByteArray();
        return Response.ok(new ByteArrayInputStream(imageData)).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("{subResources:.*}")
    public APIResponse postStuff() {
        throw new ErrorResponse(Status.NOT_FOUND, "URL NOT FOUND");
    }

    public String jobStatus(String uniqueID) {
        DatabaseConfiguration dbconfig = new DatabaseConfiguration();
        JobsQueryWrapper job = null;
        GenericResponse response = new GenericResponse();
        try {
            job = new JobsQueryWrapper(dbconfig.getDriver(),
                    dbconfig.getConnectionString(),
                    dbconfig.getDBName(),
                    dbconfig.getDBUserName(),
                    dbconfig.getDBPassword());

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error connecting to the databse");
        }

        try {
            Connection connect = job.connect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
        String status = job.getJobStatus(uniqueID);
        return status;
    }

}
