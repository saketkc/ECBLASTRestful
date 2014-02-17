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
   
@Override
public String createCommand(String fileName){
    this.command = "bsub -q research-rh6  \"" ;
    String stdoutLog = fileName + "-stdout.log";
    String stderrLog = fileName + "-stderr.log";
    ConfigParser config = new ConfigParser();
    HashMap nfsConfig = config.getFarmConfig();
    if (nfsConfig!=null){
     this.command = this.command +
             nfsConfig.get("javaHome") +  " -jar " + nfsConfig.get("rxnDecoderJar") + "  " +
             nfsConfig.get("NFSUploadPath") + fileName + "1> " + stdoutLog + " 2> " + stderrLog +
             "\"" ;
     return this.command;       
    }
    else{
        return null;
    }
    }
    

    
}
