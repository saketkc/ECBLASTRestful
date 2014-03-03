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
public class SubmitTransformationJob extends SubmitJob {
        public String createCommand(String uuid, String directory, String queryFileType, String query, String c, String transformType) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("transform_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory + " --Q="+ queryFileType + " --q=\""+query + "\" --c=" + c;
        return this.command;

    }
         public String createCommandStrict(String uuid, String directory, String queryFileType, String query, String c, String transformType) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("transform_strict_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory + " --Q="+ queryFileType + " --q=\""+query + "\" --c=" + c;
        return this.command;

    }
    
}
