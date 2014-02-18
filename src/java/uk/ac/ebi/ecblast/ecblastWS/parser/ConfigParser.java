/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author saket
 */
public class ConfigParser {

    Properties prop = new Properties();
    InputStream input = null;
    String filePath = null;

    public ConfigParser() {
        this.filePath = "config.ini";
    }

    public HashMap getDatabaseConfig() {
        try {
            input = new FileInputStream(this.filePath);
            try {
                prop.load(input);
            } catch (IOException ex) {
                return null;
            }
            HashMap hm = new HashMap();
            hm.put("dbName", prop.getProperty("db_name"));
            hm.put("dbUser", prop.getProperty("db_user"));
            hm.put("dbHost", prop.getProperty("dbHost"));
            hm.put("dbPort", prop.getProperty("dbPort"));            
            return hm;
        } catch (FileNotFoundException ex) {

            return null;
        }

    }
    
    public HashMap getFarmConfig(){
        try {
            input = new FileInputStream(this.filePath);
            try {
                prop.load(input);
            } catch (IOException ex) {
                return null;
            }
            HashMap hm = new HashMap();
            hm.put("javaHome", prop.getProperty("java_home"));
            hm.put("localUploadPath", prop.getProperty("local_upload_path"));
            hm.put("rxnDecoderJar", prop.getProperty("rxn_decoder_location"));
            hm.put("NFSUploadPath", prop.getProperty("nfs_upload_path"));
            hm.put("atomAtomMappingCommand", prop.getProperty("atom_atom_mapping_cmd"));
            return hm;
        } catch (FileNotFoundException ex) {

            return null;
        }
    }
}
