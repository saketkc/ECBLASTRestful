/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import uk.ac.ebi.ecblast.ecblastWS.config.Configuration;
import uk.ac.ebi.ecblast.ecblastWS.webserver.ECBlastResource;

/**
 *
 * @author saket
 */
public class SubmitJob {

    protected String command;
    private String response;
//    public String configFile;
            
    public SubmitJob() {
        this.command = "";
        this.response = "error";
        //ECBlastResource eb = new ECBlastResource();
//        this.configFile = eb.getConfigLocation();
        
    }

    public String executeCommand() {

        StringBuilder output = new StringBuilder();

        Process p;
        try {
            p = Runtime.getRuntime().exec(this.command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            this.response = "success";

        } catch (IOException e) {
            this.response = "error";
        } catch (InterruptedException e) {
            this.response = "error";
        }

        return output.toString();

    }

   

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
