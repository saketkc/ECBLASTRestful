/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.webserver;

import java.awt.Image;
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
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitJob;
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
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitGenericMatchingJob;
import uk.ac.ebi.ecblast.ecblastWS.parser.AtomAtomMappingParser;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;
import uk.ac.ebi.ecblast.ecblastWS.utility.AtomAtomMappingResponse;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailResults;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailText;
import uk.ac.ebi.ecblast.ecblastWS.utility.GenericResponse;

/**
 * REST Web Service
 *
 * @author saket
 */
@Path("/")
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

    /**
     * PUT method for updating or creating an instance of ECBlastResource
     *
     * @param jobID
     * @return an HTTP response with content of the updated or created resource.
     */
    @Path("/status/{jobID}")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
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
        if (status == "pending") {

            response.setResponse("Job ID " + uniqueID + "is pending");

        } else if (status == "fail") {

            response.setResponse("Job ID " + uniqueID + " failed");
        } else if (status == "done") {
            response.setAtomAtomMappingTextLink("/result/" + uniqueID + "/text");
            response.setAtomAtomMappingImageLink("/result/" + uniqueID + "/image");
            response.setAtomAtomMappingXMLLink("/result/" + uniqueID + "/xml");

        }
        response.setStatus(status);

        return response;

    }

    @Path("/result/{jobID}/text")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getResultText(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        GenericResponse response = new GenericResponse();
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + uniqueID + "__stdout.log";
        AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
        String contents = parser.readFileInString();
        response.setAtomatomMappingResultText(contents);
        return response;
    }

    @Path("/result/{jobID}/xml")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getResultXml(@PathParam("jobID") String uniqueID) throws ErrorResponse {
        GenericResponse response = new GenericResponse();
        ConfigParser configparser = new ConfigParser();
        Properties prop = configparser.getConfig();
        String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + uniqueID + "-stdout.log";
        AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
        String contents = parser.readFileInString();
        String[] contentsSplit = parser.getAllSections(contents);
        response.setBondChangeFingerprint(contentsSplit[0]);
        response.setReactionCenterFingerprint(contentsSplit[1]);
        response.setReactionCenterChanges(contentsSplit[2]);
        response.setReactionCentreTransformationPairs(contentsSplit[3]);
        response.setMoleculeTransformationPairs(contentsSplit[4]);

        //response.setAtomatomMappingResultText(contents);
        return response;
    }

    @Path("/result/{jobID}/image")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getResultImage(@PathParam("jobID") String uniqueID) throws ErrorResponse {
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
            imgFileName = "ECBLAST" + filePrefix + "_rxn.png";
        } else if ("atom_atom_mapping_smi".equals(jobType)) {
            filePrefix = job.getQueryFileName(uniqueID);
            imgFileName = "ECBLAST" + filePrefix + "_smiles.png";
        }

        try {
            BufferedImage img = null;
            ConfigParser config = new ConfigParser();
            Properties prop = config.getConfig();
            try {
                img = ImageIO.read(new File(imgFileName));
            } catch (IOException e) {
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

    @Path("/result/{jobID}/.*")
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})

    public GenericResponse getResultIamge(@PathParam("jobID") String uniqueID) throws ErrorResponse {

        return null;
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            Connection connect = job.connect();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);

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
                String files[] = {"/home/saket/INCOMINGUPLOADS/" + uniqueID + ".zip"};
                System.out.println("EMAIL  " + email);
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
            Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/aam")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse atomAtomMappingRXN(
            @FormDataParam("q") InputStream uploadedInputStreamRXN,
            @FormDataParam("q") FormDataContentDisposition fileDetailRXN,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String fileFormat,
            @FormDataParam("email") String emailID) {

        if (fileDetailRXN == null && uploadedInputStreamRXN == null && smileQuery == null) {
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
            FileUploadUtility uploadFile = new FileUploadUtility(fileDetailRXN.getFileName(), uniqueID);
            boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStreamRXN);
            userDirectory = uploadFile.getUserDirectory();
            String userFilePath = uploadFile.getFileLocation();
            if (!uploadedSucessful) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            }

            rxnMappingJob.createCommandRXN(uniqueID, userDirectory, userFilePath);

        }

        /* Submit job to farm
         TODO: Check if rxn file is all balanced!
         */
        if ("SMI".equals(fileFormat)) {
            FileUploadUtility uploadFile = new FileUploadUtility(uniqueID);
            userDirectory = uploadFile.getUserDirectory();
            rxnMappingJob.createCommandSMI(uniqueID, userDirectory, smileQuery);
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
                b = addJob.insertJob(uniqueID, jobID, fileDetailRXN.getFileName(), targetFileName, emailID, "atom_atom_mapping");
                if (b >= 1) {
                    response.setMessage(rxnMappingJob.getResponse());
                    response.setJobID(uniqueID);
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/compare/reactions")
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
                && fileDetailTarget == null)) {
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
        if ("RXN".equals(queryFormat)) {
            FileUploadUtility uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);
            boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
            userFilePathQuery = uploadFileQuery.getFileLocation();
            if (!uploadedSucessfulQuery) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");

            }
        }

        if ("RXN".equals(targetFormat)) {

            FileUploadUtility uploadFileTarget = new FileUploadUtility(fileDetailTarget.getFileName(), uniqueID);
            boolean uploadedSucessfulTarget = uploadFileTarget.writeToFile(uploadedInputStreamTarget);
            userFilePathTarget = uploadFileTarget.getFileLocation();
            if (!uploadedSucessfulTarget) {
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");

            }
        }
        String query=null;
        String target = null;
        if ("RXN".equals(queryFormat)){
            query = fileDetailQuery.getFileName();
        }
        else if ("SMI".equals(queryFormat)){
            query = smileQuery;
        }
        if ("RXN".equals(targetFormat)){
            target = fileDetailTarget.getFileName();
        }
        else if ("SMI".equals(targetFormat)){
            target = smileQuery;
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
            System.out.println("*****************************" + compareJob.getCommand());
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + compareJob.getCommand());

        }
        int jobID;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            System.out.println("*****************************" + compareJob.getCommand());

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
                b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), "dummy target.txt",
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/transform/generic")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse genericMapping(
            @FormDataParam("q") InputStream uploadedInputStreamQuery,
            @FormDataParam("q") FormDataContentDisposition fileDetailQuery,
            @FormDataParam("Q") String queryFormat,
            @FormDataParam("c") String c,
            @FormDataParam("email") String emailID
    ) {
        if (fileDetailQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "No reaction File");
        }
        if (uploadedInputStreamQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Reaction File");
        }

        String fileFormat = "RXN";
        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        FileUploadUtility uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);

        boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
        String userDirectory = uploadFileQuery.getUserDirectory();
        String userFilePathQuery = uploadFileQuery.getFileLocation();

        if (uploadedSucessfulQuery) {
            /* Submit job to farm
             TODO: Check if rxn file is all balanced!
             */
            SubmitGenericMatchingJob matchingJob = new SubmitGenericMatchingJob();

            matchingJob.createCommand(uniqueID, userDirectory, queryFormat, userFilePathQuery, c);
            String jID = matchingJob.executeCommand();
            jID = jID.trim();
            jID = jID.replace("\"\'", "");
            if (jID == null || jID == "") {
                System.out.println("*****************************" + matchingJob.getCommand());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");

            }
            int jobID;

            try {
                jobID = Integer.parseInt(jID);
            } catch (NumberFormatException ex) { // handle your exception
                System.out.println("*****************************" + matchingJob.getCommand());

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
                    b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), "dummytarget.rxn", emailID, "generic_matching");
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

        } else {
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");
        }

    }

    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/transform/strict")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse strictMapping(
            @FormDataParam("q") InputStream uploadedInputStreamQuery,
            @FormDataParam("q") FormDataContentDisposition fileDetailQuery,
            @FormDataParam("Q") String queryFormat,
            @FormDataParam("c") String c,
            @FormDataParam("email") String emailID
    ) {
        if (fileDetailQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "No reaction File");
        }
        if (uploadedInputStreamQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Reaction File");
        }

        String fileFormat = "RXN";
        GenericResponse response = new GenericResponse();
        response.setJobID(null);
        String uniqueID = UUID.randomUUID().toString();
        FileUploadUtility uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);

        boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
        String userDirectory = uploadFileQuery.getUserDirectory();
        String userFilePathQuery = uploadFileQuery.getFileLocation();

        if (uploadedSucessfulQuery) {
            /* Submit job to farm
             TODO: Check if rxn file is all balanced!
             */
            SubmitGenericMatchingJob matchingJob = new SubmitGenericMatchingJob();

            matchingJob.createCommand(uniqueID, userDirectory, queryFormat, userFilePathQuery, c);
            String jID = matchingJob.executeCommand();
            jID = jID.trim();
            jID = jID.replace("\"\'", "");
            if (jID == null || jID == "") {
                System.out.println("*****************************" + matchingJob.getCommand());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");

            }
            int jobID;

            try {
                jobID = Integer.parseInt(jID);
            } catch (NumberFormatException ex) { // handle your exception
                System.out.println("*****************************" + matchingJob.getCommand());

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
                    b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), "dummytarget.smiles",
                            emailID, "strict_matching");
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

        } else {
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");
        }

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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
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
