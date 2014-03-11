package uk.ac.ebi.ecblast.ecblastWS.parser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import uk.ac.ebi.ecblast.ecblastWS.webserver.ECBlastResource;

/**
 *
 * @author saket
 */
public class ConfigParser {

    Properties prop = new Properties();
    InputStream input = null;
    public String filePath = null;
    @Context
    public ServletContext servletContext;

    public ConfigParser() {
        //ECBlastResource et = new ECBlastResource();
        //this.filePath = servletContext.getRealPath("WEB-INF/config.ini");
        this.filePath = "/home/saket/ECBLAST/ECBLASTRESTful/web/WEB-INF/config.ini";
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public HashMap getFarmConfig() {
        System.out.println("this is test");

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

    public Properties getConfig() {
        try {
            input = new FileInputStream(this.filePath);
            try {
                prop.load(input);
            } catch (IOException ex) {
                Logger.getLogger(ConfigParser.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (FileNotFoundException ex) {
            return null;

        }
        return prop;
    }
}
