/*
 * This file is licensed under the MIT license.
 * See the LICENSE file in the project root for more information.
 */

package de.lette.mensa;

import com.esotericsoftware.minlog.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDB {

    private Connection dbConnection = null;
    private String dbHost = "";
    private String dbPort = "";
    private String dbName = "";
    private String dbInstance = "";
    private String dbUser = "";
    private String dbPass = "";

    private Properties properties;

    /**
     * sets the database connection variables by the given parameters
     *
     * @param dbHost     IP Address/Host of the Database
     * @param dbPort     Port of the database
     * @param dbName     The name of the database
     * @param dbUser     User used for the database
     * @param dbPassword Password for the database user
     */
    private void initConnectionDetails(String dbHost, String dbInstance, String dbPort, String dbName, String dbUser, String dbPassword) {
        this.dbHost = dbHost;
        this.dbInstance = dbInstance;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPass = dbPassword;
    }

    /**
     * This method reads the connection details from a configuration file
     * usually /WEB-INF/db.cfg
     *
     * @param cfgPath Path to the configuration file
     */
    public void init(String cfgPath) {
        properties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream(cfgPath);
            properties.load(input);
            initConnectionDetails(properties.getProperty("dbHost"), properties.getProperty("dbInstance"), properties.getProperty("dbPort"),
                    properties.getProperty("dbName"), properties.getProperty("dbUsername"),
                    properties.getProperty("dbPassword"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Creates a database connection object by the given connection details
     *
     * @return The database connection object
     */
    public Connection connectDB() throws ClassNotFoundException, SQLException {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        StringBuilder sb = new StringBuilder();
        sb.append("jdbc:sqlserver://");
        sb.append(dbHost);
        if (dbPort != null && !dbPort.isEmpty()) {
            sb.append(":");
            sb.append(dbPort);
        }
        sb.append("\\");
        sb.append(dbInstance);
        sb.append(";databaseName=");
        sb.append(dbName);

        Log.info(sb.toString());
        dbConnection = DriverManager.getConnection(sb.toString(), dbUser, dbPass);
        return dbConnection;
    }

    public void closeDBConnection() {
        try {
            dbConnection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getDbConnection() {
        return dbConnection;
    }
}
