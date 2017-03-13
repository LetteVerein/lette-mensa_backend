package de.lette;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDB {

	Connection dbConnection = null;
	String dbHost = "";
	String dbPort = "";
	String dbName = "";
	String dbUser = "";
	String dbPass = "";

	Properties properties;
	
	private void initConnectionDetails(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword) {
		this.dbHost = dbHost;
		this.dbPort = dbPort;
		this.dbName = dbName;
		this.dbUser = dbUser;
		this.dbHost = dbHost;
		this.dbPass = dbPassword;
	}

	public void init(String cfgPath) {
		properties = new Properties();
		InputStream input = null;

		try {
			input = new FileInputStream(cfgPath);
			properties.load(input);
			initConnectionDetails(properties.getProperty("dbHost"), properties.getProperty("dbPort"),
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

	public Connection connectDB() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		dbConnection = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?"
				+ "user=" + dbUser + "&" + "password=" + dbPass);
		return dbConnection;
	}

	public Connection getDbConnection() {
		return dbConnection;
	}
}
