/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.util.HashMap;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class SubmitAtomAtomMappingJob extends SubmitJob{
   
public String createCommand(String uuid, String path){
    ConfigParser config = new ConfigParser();
    HashMap nfsConfig = config.getFarmConfig();   
    if (nfsConfig!=null){
        this.command = (String) nfsConfig.get("atomAtomMappingCommand"); 
        this.command = this.command + " --uuid=" + uuid + " --path=" + path;
        return this.command;
    }
    else{
        return null;
    }
    }
    

    
}
