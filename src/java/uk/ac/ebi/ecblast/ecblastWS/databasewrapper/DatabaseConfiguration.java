package uk.ac.ebi.ecblast.ecblastWS.databasewrapper;

import java.util.Properties;
import uk.ac.ebi.ecblast.ecblastWS.parser.ConfigParser;

/**
 * Database Configuration Class 
 * Inputs taken from config line
 *
 */
public class DatabaseConfiguration {

    private final String driver;
    private final String dbName;
    private final String connectionString;
    private final String dbuserName;
    private final String dbpassword;

    public DatabaseConfiguration() {
        ConfigParser parser = new ConfigParser();
        Properties prop = parser.getConfig();

        driver = prop.getProperty("driver");
        dbName = prop.getProperty("db_name");
        connectionString = prop.getProperty("connection_string");
        dbuserName = prop.getProperty("db_username");
        dbpassword = prop.getProperty("db_password");
    }

    /**
     * @return the driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @return the dbName
     */
    public String getDBName() {
        return dbName;
    }

    /**
     * @return the connectionString
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * @return the dbuserName
     */
    public String getDBUserName() {
        return dbuserName;
    }

    /**
     * @return the dbpassword
     */
    public String getDBPassword() {
        return dbpassword;
    }
}
