package uk.ac.ebi.ecblast.ecblastWS.jobshandler;

import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.config.Configuration;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 *
 * @author saket
 */
public class SubmitTransformationJob extends SubmitJob {

    public String createCommand(String uuid, String directory, String queryFileType, String query, String c, String transformType) {
        ConfigParser config = new ConfigParser();
        //config.setFilePath(Configuration.getInstance().getPath().getRealPath("WEB-INF/config.ini"));

        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("transform_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory + " --Q=" + queryFileType + " --q=\"" + query + "\" --c=" + c + " --type=" + transformType;
        return this.command;

    }

    public String createCommandStrict(String uuid, String directory, String queryFileType, String query, String c, String transformType) {
        ConfigParser config = new ConfigParser();
        config.setFilePath(Configuration.getInstance().getPath().getRealPath("WEB-INF/config.ini"));

        Properties prop = config.getConfig();

        this.command = (String) prop.getProperty("transform_strict_cmd");
        this.command = this.command + " --uuid=" + uuid + " --directory=" + directory + " --Q=" + queryFileType + " --q=\"" + query + "\" --c=" + c + " --type=" + transformType;
        return this.command;

    }

}
