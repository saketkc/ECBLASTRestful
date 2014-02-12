/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ebi.ecblast.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author saket
 */
public class DatabaseWrapper {

    private String dbName;
    private String connectionString;
    private String userName;
    private String password;
    protected Connection connection = null;
    private boolean connected = false;
    private boolean classLoaded = false;

    /**
     * Make a wrapper around this connection.
     *     
* @param connection
     */
    public DatabaseWrapper(Connection connection) {
        this.connection = connection;
        if (connection != null) {
            classLoaded = true; // XXX?
            connected = true;
        }
    }

    /**
     * Make a wrapper with these connection parameters.
     *     
* @param driver
     * @param connectionString
     * @param dbName
     * @param userName
     * @param password
     * @throws ClassNotFoundException
     */
    public DatabaseWrapper(String driver,
            String connectionString,
            String dbName,
            String userName,
            String password) throws ClassNotFoundException {
        this.connectionString = connectionString;
        this.userName = userName;
        this.dbName = dbName;
        this.password = password;
        if (!classLoaded) {
            Class.forName(driver);
            classLoaded = true;
        }
    }

    public Statement createStatement() throws SQLException {
        return connection.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    public boolean isClassLoaded() {
        return classLoaded;
    }

    public boolean isConnected() {
        return connected;
    }

    public Connection connect() throws SQLException, ClassNotFoundException {
        if (!connected) {
            connection = getConnection();
        }
        connected = true;
        return connection;
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            connected = false;
            connection = null;
        }
    }

    private Connection getConnection() throws SQLException {
        System.out.println("CALLING CONNECTION");
        return DriverManager.getConnection(connectionString + "/" + dbName, userName, password);
    }

}
