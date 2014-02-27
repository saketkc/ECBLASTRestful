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
public class SubmitSearchJob extends SubmitJob {
        public String createCommand(String uuid, String directory, String fileFormat, String query,  String searchType, String c) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("search_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory +  " --Q=" + fileFormat + " --s=" + searchType + " --q=\"" +query + "\" --c="+c;
        return this.command;

    }

   
    
}
