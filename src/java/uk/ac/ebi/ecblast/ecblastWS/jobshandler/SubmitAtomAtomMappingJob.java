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
public class SubmitAtomAtomMappingJob extends SubmitJob {

    public String createCommand(String uuid, String userDirectory, String fileFormat, String userFilePath) {
        ConfigParser config = new ConfigParser();
        //config.setFilePath(Configuration.getInstance().getPath().getRealPath("WEB-INF/config.ini"));
        Properties prop = config.getConfig();
        this.command = (String) prop.getProperty("atom_atom_mapping_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + userDirectory + " --Q=" + fileFormat + " --q=\"" + userFilePath + "\"" ;
        return this.command;

    }    

}
