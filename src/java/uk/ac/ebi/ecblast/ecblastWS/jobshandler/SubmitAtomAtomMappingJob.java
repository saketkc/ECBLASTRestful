/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.util.HashMap;
import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class SubmitAtomAtomMappingJob extends SubmitJob {

    public String createCommandRXN(String uuid, String userDirectory, String userFilePath) {
        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();
        this.command = (String) prop.getProperty("rxn_atom_atom_mapping_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + userDirectory + " --file=" + userFilePath ;
        return this.command;

    }

    public String createCommandSMI(String uuid, String userDirectory, String smileQuery) {

        ConfigParser config = new ConfigParser();
        Properties prop = config.getConfig();
        this.command = (String) prop.getProperty("smi_atom_atom_mapping_cmd");

        this.command = this.command + " --uuid=" + uuid + " --directory=" + userDirectory + " --query=" + "\"" + smileQuery +"\"";
        return this.command;
    }

}
