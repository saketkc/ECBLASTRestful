package uk.ac.ebi.ecblast.ecblastWS.webserver;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import uk.ac.ebi.ecblast.ecblastWS.databasewrapper.DatabaseConfiguration;
import uk.ac.ebi.ecblast.ecblastWS.databasewrapper.JobsQueryWrapper;
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
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import uk.ac.ebi.ecblast.ecblastWS.config.Configuration;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitAtomAtomMappingJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitCompareReactionsJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitTransformationJob;
import uk.ac.ebi.ecblast.ecblastWS.jobshandler.SubmitSearchJob;
import uk.ac.ebi.ecblast.ecblastWS.parser.AtomAtomMappingParser;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailResults;
import uk.ac.ebi.ecblast.ecblastWS.utility.EmailText;
import uk.ac.ebi.ecblast.ecblastWS.utility.GenericResponse;
import uk.ac.ebi.ecblast.ecblastWS.utility.LogFileWriter;

/**
 * REST Web Service
 *
 * @author Saket Choudhary <saketkc@gmail.com>
 */
/* This path defines the relative location where the webservice will run
 * For.eg. Changing the @Path("/") to @Path("/test") will cause
 * all URLs to be called with respect to /test say /test/aam instead of /aam
 */
@Path("/")

public class ECBlastResource {

    @Context
    public ServletContext servletContext;

    public void setECBlastResource(ServletContext servletContext) {
        Configuration.getInstance().setPath(servletContext);
    }

    // This method is called if HTML is request
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayHtmlHello() {
        return "<html> " + "<title>" + "Hello Jersey" + "</title>"
                + "<body><h1>" + "Hello Jersey" + "</body></h1>" + "</html> ";
    }
    /*
     No Functiopn to call here, hence returne forbidden
     */

    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse putHtml() {
        throw new ErrorResponse(Status.FORBIDDEN, "Forbidden");
    }

    /*
     No Functiopn to call here, hence returne forbidden
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public APIResponse postHtml() {
        throw new ErrorResponse(Status.FORBIDDEN, "Forbidden");
    }

    /*
     Submit Atom Atom Mapping Job
     @param 'q' : String | FiileuploadInputStream
     -- Fileupload stream or string Smiles based query
     @param 'Q' : String
     -- Query format 
    
     @returns 'response' : Response object
     -- Error Response with error message OR Success response with job_ID
    
     Details:
    
     Each job is checked for valid query Format [SMI/RXN]. A uniqueID uis generated
     and a folder created on the tomcat sever for this job. Depending on whether a RXN file is uploaded
     or a Smiles query has been requested, the file would need to be uploaded on tomcat. Only RXN files need 
     to be uploaded on tomcat. 
     Once this operation is complete the job is submitted to the farm using 'python_job_submitter/atom_atom_mapping.py' byu
     generating
     the following inputs for the atom_atom_mapping.py file:
     --uuid      	      unique ID
     --directory               Path location where user uploads sit
     --Q Q                     Query Type
     --q Q                     SMILES string or absolute path to RXN file
     */
    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/aam")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public GenericResponse atomAtomMappingRXN(
            @DefaultValue("") @FormDataParam("q") InputStream uploadedInputStreamRXN,
            @DefaultValue("") @FormDataParam("q") FormDataContentDisposition fileDetailRXN,
            @DefaultValue("") @FormDataParam("q") String smileQuery,
            @DefaultValue("") @FormDataParam("Q") String fileFormat,
            @FormDataParam("email") String emailID) throws IOException, SQLException {
        setECBlastResource(servletContext);

        /* Check for File Formats being SMI or RXN */
        if (!"RXN".equals(fileFormat) && !"SMI".equals(fileFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + fileFormat);
        }

        /* Return error if empty file inputs */
        if ("RXN".equals(fileFormat)) {
            if (fileDetailRXN == null || uploadedInputStreamRXN == null) {
                throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Inputs");
            }
        }

        /* Return error if empty file inputs */
        if ("SMI".equals(fileFormat)) {
            if (smileQuery == null || "".equals(smileQuery)) {
                throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Inputs");
            }

        }

        GenericResponse response = new GenericResponse();
        /* Generate a unique ID for job */
        String uniqueID = UUID.randomUUID().toString();

        /* Create Atom Atom Mapping Job */
        SubmitAtomAtomMappingJob rxnMappingJob = new SubmitAtomAtomMappingJob();
        String jID;
        String userDirectory;
        LogFileWriter logWriter = new LogFileWriter(uniqueID);

        /*Upload file to Tomcat server if a RXN file was submitted */
        if ("RXN".equals(fileFormat)) {
            logWriter.WriteToFile("Uploading user RXN file " + fileDetailRXN.getFileName());
            FileUploadUtility uploadFile = new FileUploadUtility(fileDetailRXN.getFileName(), uniqueID);
            boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStreamRXN);
            userDirectory = uploadFile.getUserDirectory();
            String userFilePath = uploadFile.getFileLocation();
            if (!uploadedSucessful) {
                logWriter.WriteToFile("ERROR uploading user RXN file " + fileDetailRXN.getFileName());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            } else {
                logWriter.WriteToFile("Successfully uploaded user RXN file " + fileDetailRXN.getFileName());
            }
            /*Create command using parameters to be submitted to the farm*/
            rxnMappingJob.createCommand(uniqueID, userDirectory, fileFormat, userFilePath);
            logWriter.WriteToFile("Executing ppython command: " + rxnMappingJob.getCommand());
        }

        /*If SMI reaction was submitted, the file is not uploaded as such on the form
         however we use the same wrapper to remain consistent and the whole of the job involves creating a directory 
         with folder name as uniqueID
         */
        if ("SMI".equals(fileFormat)) {
            logWriter.WriteToFile("SMI reaction submitted: " + smileQuery);
            FileUploadUtility uploadFile = new FileUploadUtility(uniqueID);
            userDirectory = uploadFile.getUserDirectory();
            rxnMappingJob.createCommand(uniqueID, userDirectory, fileFormat, smileQuery);
            logWriter.WriteToFile("Executing python command: " + rxnMappingJob.getCommand());
        }

        /* Submit job to farm
         TODO: Check if rxn file is all balanced!
         */
        jID = rxnMappingJob.executeCommand();
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        logWriter.WriteToFile("***Response from FARM: " + jID);
        /*If the job submission was succesfull the output should be a jobID as string which
         can be converted to INteger. If it fails, it essentially means,
         the job failed and an erro is written to the log file
         */
        if (jID == null || "".equals(jID)) {
            System.out.println(rxnMappingJob.getCommand());
            logWriter.WriteToFile("***EMPTY RESPONSE from FARM: " + jID);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node");

        }
        int jobID = 0;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            logWriter.WriteToFile("***ERROR from FARM: " + jID);
            System.out.println(rxnMappingJob.getCommand());
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + jID);
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
            Connection connect = null;
            try {
                connect = addJob.connect();
                int b;
                String targetFileName = null;
                String jobType = "atom_atom_mapping" + "_" + fileFormat.toLowerCase();
                b = addJob.insertJob(uniqueID, jobID, fileDetailRXN.getFileName(), targetFileName, emailID, jobType);
                if (b >= 1) {
                    response.setMessage(rxnMappingJob.getResponse());
                    response.setJobID(uniqueID);
                    //addJob.disconnect();

                } else {
                    //addJob.disconnect();

                    logWriter.WriteToFile("Error writing to database");
                    response.setMessage("error");
                    response.setResponse("erro submitting to database");
                }
            } catch (SQLException ex) {
                addJob.disconnect();
                logWriter.WriteToFile("SQLException: " + ex.toString());
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                logWriter.WriteToFile("Database error" + ex.toString());
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                response.setMessage("Error in submitting job");
                return response;
            } finally {
                try {
                    if (connect != null || !connect.isClosed()) {
                        connect.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return response;

    }

    /*
     Submit Atom Atom Mapping Job
     @param 'q' : String | FiileuploadInputStream
     -- Fileupload stream or string Smiles based query
     @param 'Q' : String
     -- Query format 
     @param 't': String | FiileuploadInputStream
     -- Target Fileinput stream or Target smiles string
     @param 'T': String
     -- Target Format   
    
    
     @returns 'response' : Response object
     -- Error Response with error message OR Success response with job_ID
    
     Details:
    
     Each job is checked for valid query Format [SMI/RXN]. A uniqueID uis generated
     and a folder created on the tomcat sever for this job. Depending on whether a RXN file is uploaded
     or a Smiles query has been requested, the file would need to be uploaded on tomcat. Only RXN files need 
     to be uploaded on tomcat. 
     Once this operation is complete the job is submitted to the farm using 'python_job_submitter/compare.py' by
     generating
     the following inputs for the atom_atom_mapping.py file:
     --uuid      	       unique ID
     --directory               Path location where user uploads sit
     --Q                       Query Format[SMI/RXN]
     --q                       Query SMILES string or absolute path to RXN file
     --T                       Target Format[SMI/RXN]
     --t                       Target SMILES string or absolute path to RXN file
     */
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
    ) throws IOException {

        /* Check for query to be non null*/
        if ((uploadedInputStreamQuery == null && smileQuery == null && fileDetailQuery == null) || (uploadedInputStreamTarget == null && smileTarget == null
                && fileDetailTarget == null && smileTarget == null)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Empty Reaction File");
        }

        /* Check for query format */
        if (!"RXN".equals(queryFormat) && !"SMI".equals(queryFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + queryFormat);
        }
        /* Check for target format */
        if (!"RXN".equals(targetFormat) && !"SMI".equals(targetFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "File Format Not Supported" + targetFormat);
        }

        GenericResponse response = new GenericResponse();
        /*Generate uniqueID*/
        String uniqueID = UUID.randomUUID().toString();
        LogFileWriter logWriter = new LogFileWriter(uniqueID);
        FileUploadUtility fileUpload = new FileUploadUtility(uniqueID);
        String userDirectory = fileUpload.getUserDirectory();
        String userFilePathQuery = null;
        String userFilePathTarget = null;
        FileUploadUtility uploadFileTarget = null;
        FileUploadUtility uploadFileQuery = null;

        /*If Query is RXN file, upload it to the tomcat*/
        if ("RXN".equals(queryFormat)) {
            logWriter.WriteToFile("INFO Uploading user QUERY RXN file " + fileDetailQuery.getFileName());

            uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);
            boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
            userFilePathQuery = uploadFileQuery.getFileLocation();
            if (!uploadedSucessfulQuery) {
                logWriter.WriteToFile("ERROR uploading user QUERY RXN file " + fileDetailQuery.getFileName());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");
            } else {
                logWriter.WriteToFile("SUCCESS uploading user QUERY RXN file " + fileDetailQuery.getFileName());
            }
        }

        /*If Target is RXN file, upload it to the tomcat*/
        if ("RXN".equals(targetFormat)) {
            logWriter.WriteToFile("INFO Uploading user TARGET RXN file " + fileDetailTarget.getFileName());
            uploadFileTarget = new FileUploadUtility(fileDetailTarget.getFileName(), uniqueID);
            boolean uploadedSucessfulTarget = uploadFileTarget.writeToFile(uploadedInputStreamTarget);
            userFilePathTarget = uploadFileTarget.getFileLocation();
            if (!uploadedSucessfulTarget) {
                logWriter.WriteToFile("ERROR uploading user TARGET RXN file " + fileDetailTarget.getFileName());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Could not upload file");
            } else {
                logWriter.WriteToFile("SUCCESS uploading user TARGET RXN file " + fileDetailTarget.getFileName());
            }
        }

        String query = null;
        String target = null;

        //A bit of hacking in here
        /*
         If the query is SMILES, sent it do python_job_submitter/compare_reactions.py as it is , else 
         submmit the path locationof the uploaded file on tomcat server
         */
        if ("RXN".equals(queryFormat)) {
            query = uploadFileQuery.getFileLocation();
        } else if ("SMI".equals(queryFormat)) {
            query = smileQuery;
        }

        //A bit of hacking in here
        /*
         If the target is SMILES, sent it do python_job_submitter/compare_reactions.py as it is , else 
         submmit the path locationof the uploaded file on tomcat server
         */
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
        logWriter.WriteToFile("INFO: Command submitted to farm: " + compareJob.getCommand());
        String jID = compareJob.executeCommand();
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || "".equals(jID)) {
            logWriter.WriteToFile("ERROR Empty response from farm ");
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + compareJob.getCommand());

        }
        int jobID;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception                         

            logWriter.WriteToFile("ERROR from FARM: " + jID);
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
                logWriter.WriteToFile("DB ERROR " + ex.toString());
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            Connection connect = null;
            try {
                connect = addJob.connect();
                int b;
                b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), null,
                        emailID, "compare_reactions");
                if (b >= 1) {
                    response.setMessage(compareJob.getResponse());
                    response.setJobID(uniqueID);

                }
                return response;

            } catch (SQLException ex) {
                logWriter.WriteToFile("SQL Exception " + ex.toString());
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            } catch (ClassNotFoundException ex) {
                logWriter.WriteToFile("DB ERROR " + ex.toString());
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
                response.setMessage(
                        "Error in submitting job");
                return response;
            } finally {
                try {
                    if (connect != null || !connect.isClosed()) {
                        connect.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return null;

    }

    /*
     Submit Transform Job
     @param 'q' : String | FiileuploadInputStream
     -- Fileupload stream or string Smiles based query
     @param 'Q' : String
     -- Query format 
     @param 'type' String 
     -- Transform Type : generic/strict
     @param 'c' String/Int
     -- Number of hits, defaults to 10
     
     @returns 'response' : Response object
     -- Error Response with error message OR Success response with job_ID
    
     Details:
    
     Each job is checked for valid query Format [SMI/RXN]. A uniqueID uis generated
     and a folder created on the tomcat sever for this job. Depending on whether a RXN file is uploaded
     or a Smiles query has been requested, the file would need to be uploaded on tomcat. Only RXN files need 
     to be uploaded on tomcat. 
     Once this operation is complete the job is submitted to the farm using 'python_job_submitter/transform.py' by
     generating
     the following inputs for the atom_atom_mapping.py file:
     --uuid UUID           Unique ID
     --directory           Absolute path to user uploaded stuff
     --q Q                 Smiles query or reaction file path as on tomcat
     --Q Q                 Query format[SMI/RXN]
     --c C                 No. of hits
     --type                Type[strict/generic]

     */
    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/transform")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse genericMapping(
            @FormDataParam("q") InputStream uploadedInputStreamQuery,
            @FormDataParam("q") FormDataContentDisposition fileDetailQuery,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String queryFormat,
            @DefaultValue("strict")
            @FormDataParam("type") String transformType,
            @DefaultValue("10") @FormDataParam("c") String c,
            @FormDataParam("email") String emailID
    ) throws IOException, SQLException {
        if (fileDetailQuery == null && uploadedInputStreamQuery == null && smileQuery == null) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "No reaction File");
        }
        if (!"RXN".equals(queryFormat) && !"SMI".equals(queryFormat)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "Only RXN,SMI are allowed");
        }
        if (c == null || "".equals(c)) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "c required");
        }
        Integer hits;
        try {
            hits = Integer.parseInt(c);
        } catch (NumberFormatException ex) {
            throw new ErrorResponse(Response.Status.BAD_REQUEST, "c should be integer");
        }

        GenericResponse response = new GenericResponse();
        String uniqueID = UUID.randomUUID().toString();
        LogFileWriter logWriter = new LogFileWriter(uniqueID);
        String query = null;
        FileUploadUtility uploadFileQuery = new FileUploadUtility(uniqueID);
        if ("RXN".equals(queryFormat)) {
            logWriter.WriteToFile("INFO: Uploading  to farm: " + fileDetailQuery.getFileName());
            uploadFileQuery = new FileUploadUtility(fileDetailQuery.getFileName(), uniqueID);
            query = uploadFileQuery.getFileLocation();
            boolean uploadedSucessfulQuery = uploadFileQuery.writeToFile(uploadedInputStreamQuery);
            if (!uploadedSucessfulQuery) {
                logWriter.WriteToFile("ERROR: Uploading  to farm failed : " + fileDetailQuery.getFileName());
                throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error uploading file");
            } else {
                logWriter.WriteToFile("SUCCESS: Uploading  to farm succeded : " + fileDetailQuery.getFileName());
            }
        }
        /*If ?SMI was submitted it should be passed to farm like that
        
         */
        if ("SMI".equals(queryFormat)) {
            logWriter.WriteToFile("INFO: SMILES Query: " + smileQuery);
            query = smileQuery;
        }

        String userDirectory = uploadFileQuery.getUserDirectory();
        String jID = null;
        SubmitTransformationJob matchingJob = new SubmitTransformationJob();
        if (transformType == null || "".equals(transformType)) {
            transformType = "strict";
        }
        if ("strict".equals(transformType)) {

            matchingJob.createCommandStrict(uniqueID, userDirectory, queryFormat, query, c, transformType);

            jID = matchingJob.executeCommand();
        } else if ("generic".equals(transformType)) {

            matchingJob.createCommand(uniqueID, userDirectory, queryFormat, query, c, transformType);

            jID = matchingJob.executeCommand();
        }
        logWriter.WriteToFile("INFO: Python command line:  " + matchingJob.getCommand());
        jID = jID.trim();
        jID = jID.replace("\"\'", "");
        if (jID == null || ("").equals(jID)) {
            logWriter.WriteToFile("EMPTY Response from Farm");
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + matchingJob.getCommand());

        }
        int jobID;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exception
            logWriter.WriteToFile("ERROR: Error on FARM:  " + jID);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node" + matchingJob.getCommand());

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
                logWriter.WriteToFile("DB ERROR " + ex.toString());
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

            Connection connect = null;
            try {
                connect = addJob.connect();
                System.out.println("Connected " + connect.isClosed());
                int b;
                b = addJob.insertJob(uniqueID, jobID, fileDetailQuery.getFileName(), null, emailID, transformType);
                System.out.println("Connected " + connect.isClosed());
                if (b >= 1) {

                    response.setMessage(matchingJob.getResponse());
                    response.setJobID(uniqueID);

                }
                System.out.println("Connected " + connect.isClosed());
            } catch (SQLException ex) {

                logWriter.WriteToFile("ERROR: SQLEXception" + ex.toString());
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
            } finally {
                try {
                    if (connect != null || !connect.isClosed()) {
                        connect.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return response;

    }

    /*
     Submit Search Job
     @param 'q' : String | FiileuploadInputStream
     -- Fileupload stream or string Smiles based query
     @param 'Q' : String
     -- Query format 
     @param 's' String 
     -- Search Type : bond/
     @param 'c' String/Int
     -- Number of hits, defaults to 10
     
     @returns 'response' : Response object
     -- Error Response with error message OR Success response with job_ID
    
     Details:
    
     Each job is checked for valid query Format [SMI/RXN]. A uniqueID uis generated
     and a folder created on the tomcat sever for this job. Depending on whether a RXN file is uploaded
     or a Smiles query has been requested, the file would need to be uploaded on tomcat. Only RXN files need 
     to be uploaded on tomcat. 
     Once this operation is complete the job is submitted to the farm using 'python_job_submitter/transform.py' by
     generating
     the following inputs for the atom_atom_mapping.py file:
     --uuid UUID           Unique ID
     --directory           Absolute path to user uploaded stuff
     --q                   Smiles query or reaction file path as on tomcat
     --Q                   Query format[SMI/RXN]
     --s                   Searhc type bond/centre/structure
     --c                   No. of hits[DEfaults to 10]
     --type                Type[strict/generic]

     */
    @POST
    @Produces({MediaType.APPLICATION_XML})
    @Path("/search")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public GenericResponse search(@FormDataParam("q") InputStream uploadedInputStreamRXN,
            @FormDataParam("q") FormDataContentDisposition fileDetailRXN,
            @FormDataParam("q") String smileQuery,
            @FormDataParam("Q") String fileFormat,
            @FormDataParam("s") String searchType,
            @DefaultValue("10")
            @FormDataParam("c") String c,
            @FormDataParam("email") String emailID) throws IOException, SQLException {

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
        LogFileWriter logWriter = new LogFileWriter(uniqueID);
        SubmitSearchJob searchJob = new SubmitSearchJob();
        String jID;
        String userDirectory = null;
        String query = null;
        FileUploadUtility uploadFile = null;
        if ("RXN".equals(fileFormat)) {
            logWriter.WriteToFile("INFO: Uploading RXN: " + fileDetailRXN.getFileName());
            uploadFile = new FileUploadUtility(fileDetailRXN.getFileName(), uniqueID);
            boolean uploadedSucessful = uploadFile.writeToFile(uploadedInputStreamRXN);

            String userFilePath = uploadFile.getFileLocation();
            if (!uploadedSucessful) {
                logWriter.WriteToFile("ERROR uploading failed" + fileDetailRXN.getFileName());
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
            logWriter.WriteToFile("ERROR: Smty response from Farm");
            System.out.println(searchJob.getCommand());
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, "Error submitting job to node " + searchJob.getCommand());

        }
        int jobID = 0;

        try {
            jobID = Integer.parseInt(jID);
        } catch (NumberFormatException ex) { // handle your exceptio
            logWriter.WriteToFile("ERROR: from farm :" + jID);
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

                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            Connection connect = null;
            try {
                connect = addJob.connect();
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
            } finally {
                try {
                    if (connect != null || !connect.isClosed()) {
                        connect.close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        return response;

    }

    /*
     Get Status of job
     @param jobID:
     -- job ID(unique ID)
     @returns:
     XML response status: done/failed/pending/running
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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error connecting to the databse");
        }
        Connection connect = null;
        try {
            connect = job.connect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error connecting to the databse");
        }
        Connection connect = null;
        try {
            connect = job.connect();

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
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
     @param jobID:
     jobID
     @returns:
     Xml response: Text output of job
     */
    @Path("/result/{jobID}/text")
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public GenericResponse getResultText(@PathParam("jobID") String uniqueID) throws ErrorResponse {
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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error connecting to the databse");
        }
        Connection connect = null;
        try {
            connect = job.connect();
            String jobStatus = job.getJobStatus(uniqueID);

            if (jobStatus == null) {
                throw new ErrorResponse(Status.BAD_REQUEST, "No Job found");
            }
            jobStatus = jobStatus.trim().toLowerCase().toString();
            if ("failed".equals(jobStatus)) {

                throw new ErrorResponse(Status.BAD_REQUEST, "Job Failed!" + jobStatus);
            } else if ("running".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Running!");
            } else if ("pending".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Pending!");
            }
            String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/Result.txt";
            //String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + uniqueID + "__text.log";
            AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
            String contents = parser.readFileInString();
            response.setAtomatomMappingResultText(contents + filepath);

            return response;

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /*Send back mapped reaction file
     @param jobID
     @return XMLS response containing the Mapped reaction file
     */
    @Path("/result/{jobID}/mapped")
    @GET
    @Produces({MediaType.APPLICATION_XML})
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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            throw new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    "Error connecting to the databse");
        }
        Connection connect = null;

        try {
            connect = job.connect();
            String jobStatus = job.getJobStatus(uniqueID);
            if ("failed".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Failed!" + jobStatus);
            } else if ("running".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Running!");
            } else if ("pending".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Pending!");
            }
            String jobType = job.getJobType(uniqueID);
            String filepath = null;
            AtomAtomMappingParser parser;
            String contents;
            if ("atom_atom_mapping_rxn".equals(jobType)) {
                String fileName = job.getQueryFileName(uniqueID);
                String[] splitT = fileName.split("\\.");
                fileName = "ECBLAST_" + uniqueID + "__" + splitT[0];
                filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/"
                        + fileName + "_Query" + ".rxn";
                parser = new AtomAtomMappingParser(filepath);
                contents = parser.readFileInString();
                response.setAtomatomMappingResultText(contents);

            } else if ("atom_atom_mapping_smi".equals(jobType)) {
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
                    fileNameQuery = "ECBLAST" + "_smiles";
                } else {
                    String[] splitQ = fileNameQuery.split("\\.");
                    fileNameQuery = "ECBLAST_" + uniqueID + "__" + splitQ[0];
                }

                if (fileNameTarget == null) {
                    fileNameTarget = "ECBLAST" + "_smiles";
                } else {
                    String[] splitT = fileNameTarget.split("\\.");
                    fileNameTarget = "ECBLAST_" + uniqueID + "__" + splitT[0];
                }
                String filepathQuery = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + fileNameQuery + "_Query" + ".rxn";
                parser = new AtomAtomMappingParser(filepathQuery);
                String queryContents = parser.readFileInString();

                String filepathTarget = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/" + fileNameTarget + "_Target" + ".rxn";
                parser = new AtomAtomMappingParser(filepathTarget);
                String targetContents = parser.readFileInString();
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

        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Path("/result/{jobID}/xml")
    @GET
    @Produces({MediaType.APPLICATION_XML})
    public Response getResultXml(@PathParam("jobID") String uniqueID) throws ErrorResponse {
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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        Connection connect = null;

        try {
            connect = job.connect();
            String jobStatus = job.getJobStatus(uniqueID);
            if ("failed".equals(jobStatus)) {

                throw new ErrorResponse(Status.BAD_REQUEST, "Job Failed!" + jobStatus);
            } else if ("running".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Running!");
            } else if ("pending".equals(jobStatus)) {
                throw new ErrorResponse(Status.BAD_REQUEST, "Job Pending!");
            }

            String filepath = prop.getProperty("results_upload_directory") + "/" + uniqueID + "/Result.xml";
            //+ "/" + uniqueID + "__xml.log";
            AtomAtomMappingParser parser = new AtomAtomMappingParser(filepath);
            String contents = parser.readFileInString();

            return Response.ok(contents).build();
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

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
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        Connection connect = null;

        try {
            connect = job.connect();
            String jobType = job.getJobType(uniqueID);
            String filePrefix;
            String imgFileName = null;
            if ("atom_atom_mapping_rxn".equals(jobType)) {

                filePrefix = job.getQueryFileName(uniqueID);
                imgFileName = userFolder + "ECBLAST" + "_" + filePrefix + "_rxn.png";
            } else if ("atom_atom_mapping_smi".equals(jobType)) {

                imgFileName = userFolder + "ECBLAST" + "_" + "smiles_Query" + "_rxn.png";
            } else if ("compare_reactions".equals(jobType)) {

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
                Logger.getLogger(ECBlastResource.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

    }
    /*Returns a JSON formatted list of pending jobs
     Ideally should not be publically accessible and can be changed to something complicated
     */

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
        Connection connect = null;
        try {
            connect = jobWrapper.connect();
            String pendingJobs = jobWrapper.getPendingJobIDs();
            response.setMessage("success");
            response.setResponse(pendingJobs);
            return response;
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @GET
    @Path("/queued_jobs")
    @Produces({MediaType.APPLICATION_JSON})
    public APIResponse getQueued() {
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
        Connection connect = null;
        try {
            connect = jobWrapper.connect();
            String pendingJobs = jobWrapper.getQueuedJobIDs();
            response.setMessage("success");
            response.setResponse(pendingJobs);
            return response;
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return response;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /* Updates the status of given jobId
     @param uniqueID:
     --uniqueId of job
     @param status:
     -- update the status to running/failed/done
     @return null
     */
    @Produces({MediaType.APPLICATION_XML})
    @Path("/updateJobStatus/{uniqueID}/{status}")
    public String updateStatus(@PathParam("uniqueID") String uniqueID, @PathParam("status") String status) {
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
        Connection connect = null;
        try {
            connect = job.connect();
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

            return null;
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

    }

    @GET
    @Path("/test")
    public String getConfigLocation() {
        return servletContext.getRealPath("WEB-INF/config.ini");
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
        Connection connect = null;

        try {
            connect = job.connect();
            String status = job.getJobStatus(uniqueID);
            return status;
        } catch (SQLException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ECBlastResource.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        } finally {
            try {
                if (connect != null || !connect.isClosed()) {
                    connect.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ECBlastResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
