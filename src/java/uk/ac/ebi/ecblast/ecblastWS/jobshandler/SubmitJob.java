/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author saket
 */
public class SubmitJob {

    protected String command;
    private String response;

    public SubmitJob() {
        this.command = "";
        this.response = "error";
    }

    public String executeCommand() {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(this.command);
            p.waitFor();
            BufferedReader reader
                    = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }
            this.response = "success";

        } catch (Exception e) {
            this.response = "error";
        }

        return output.toString();

    }

    public String createCommand(String args) {
        String cmd = "cat /home/saket/test.txt";
        //cmd+=args;
        this.command = cmd;
        return this.command;
                
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
