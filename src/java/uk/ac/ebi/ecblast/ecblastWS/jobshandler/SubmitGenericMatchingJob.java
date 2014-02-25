/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class SubmitGenericMatchingJob extends SubmitJob {
        public String createCommand(String uuid, String directory, String queryFileType, String queryFilePath, String c) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("generic_matching_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory + " --Q="+ queryFileType + " --q="+queryFilePath + " --c=" + c;
        return this.command;

    }
    
}
