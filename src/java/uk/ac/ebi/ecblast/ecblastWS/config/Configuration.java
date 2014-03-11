/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.ecblast.ecblastWS.config;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

/**
 *
 * @author saket
 */
public class Configuration {

    @Context
    public static ServletContext servletContext;

    /**
     * @return the path
     */
    public ServletContext getPath() {
        return servletContext;
    }

    public Configuration() {
    }

    public static Configuration getInstance() {
        return ConfigHolder.INSTANCE;
    }

    public void setPath(ServletContext servletContext) {
        Configuration.servletContext = servletContext;
    }

    private static class ConfigHolder {

        private static final Configuration INSTANCE = new Configuration();
    }
}
