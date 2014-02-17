/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.utility;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FileUploadUtility enables user uploads to the EBI server
 * These uploads are then moved by the Python utility to the server for running *
 * 
 * @author saket
 */
public class FileUploadUtility {

    public final String uploadDirectory = "/home/saket/UPLOADS/";
    public String fileName;

    /* Constructors, Getters and Setters Begin*/
    public FileUploadUtility(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    /* Constructors, Getters and Setters End*/
    
    /* This function returns the absolute upload path
    for the user uploaded file
    Some special characters are removed to keep consistency
    TODO: Add a custom function to clean filenames?
    */
    public String getFileLocation() {
      
        return this.uploadDirectory + this.fileName;
    }

    /*This function is the main function
    that carries out the write operation to file on the server
    using an inputstream as passed from the ECBlastResource.java
    The try catch statements are purposely nested to return a boolean
    as to avoid any kind of exception being raised
    If the file write is successfull it send s True otherwise it sends False
    */
    public boolean writeToFile(InputStream uploadedInputStream) {
        boolean returnStatus = false;
        try {

            OutputStream out;

            out = new FileOutputStream(new File(getFileLocation()));

            int read = 0;
            byte[] bytes = new byte[1024];
            try {
                while ((read = uploadedInputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }
            } catch (IOException ex) {
                return returnStatus;

            }
            try {
                out.flush();
            } catch (IOException ex) {
                return returnStatus;
            }
            try {
                out.close();
            } catch (IOException ex) {
                return returnStatus;
            }
            returnStatus = true;
            return returnStatus;
        } catch (FileNotFoundException ex) {
            return returnStatus;
        }

    }
}


