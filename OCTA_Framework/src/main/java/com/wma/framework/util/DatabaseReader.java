package com.wma.framework.util;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

/**
 * <p>This class can be used to create database connect connections<br/>
 * Before using this class, please follow below instructions</p>
 * 
 *  <ul>
 *  <li>Download sqljdbc_auth.dll</li>
 *  <li><a href='https://www.microsoft.com/en-us/download/details.aspc?id-11774>Click here</a> to download</li> 
 *  <li>Download the file.tar.gz and extract it, you will get the dll file</li>
 *  <li>Copy it in the project home directory
 *  which can accessed by <code>System.getproperty("user.dir")</code></li>
 *  </ul>
 */
public class DatabaseReader {
	
	Connection DBconn = null;
	Statement DBstmt = null;
	ResultSet DBrs = null;
	
	/**
	 * To create and get the connection with the SQL Database through jdbc driver
	 * @param serverName
	 * @return Return a connection object, which points to the sql database
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Connection getSqlDbConnection(String serverName) throws SQLException, ClassNotFoundException {
		try {
			addLibraryPath(System.getProperty("user.dir"));
		} catch (Exception e) {
			System.err.println("Could not add project home directory into the Library path\n"
                    + "SQL connections might not work");
			e.printStackTrace();
		}
		String driver = "com.mysql.jdbc.Driver";
		Class.forName(driver);
		
		String connectionUrl = "jdbc:sqlserver://localhost;serverName" + serverName + ";integratedSecurity=true";
		return DriverManager.getConnection(connectionUrl);
	}
	
	/**
	 * To create and get the connections with the Excel Database/Workbook through jdbc driver 
	 * @param serverName
	 * @return Return a connection objects, qwhich points to the excel database 
	 * @throws SQLException
	 * @throwsClassNotFoundException
	 */
	public Connection getExcelConnection(String expectedDataSheetPath) throws Exception {
		String driver = "com.googlecode.sqlsheet.driver";
		Class.forName(driver);
		return DriverManager.getConnection("jdbc:xls:file:" + expectedDataSheetPath);
	}
	
	private void addLibraryPath(String pathToAdd) throws Exception {
		Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);
		
		String[] path = (String[]) usrPathsField.get(null);
		
		for (String path1 : path)
			if (path1.equals(pathToAdd))
				return;
	    
		String[] newPath = Arrays.copyOf(path, path.length + 1);
		newPath[newPath.length - 1] = pathToAdd;
		usrPathsField.set(null, newPath);
	}
}

 