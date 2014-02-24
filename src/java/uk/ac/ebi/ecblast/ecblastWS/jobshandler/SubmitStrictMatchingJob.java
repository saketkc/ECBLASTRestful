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
public class SubmitStrictMatchingJob extends SubmitJob{
        public String createCommand(String uuid, String queryFileType, String queryFilePath) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("strict_matching_cmd");
        this.command = this.command + " --uuid=" + uuid + " --Q="+ queryFileType + " --q="+queryFilePath;
        return this.command;

    }
}
