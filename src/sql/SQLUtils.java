/**
 * @author Ethan Prentice
 * 
 */


package sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringJoiner;
import java.util.logging.Level;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import constants.ServerConst;


public class SQLUtils implements ServletContextListener {
	
	private static Connection connection;
	
	private final static String USER = "shaka";
	private final static String PASSWORD = "Pa$$phras3_123";
	
	public static void createConnection(String username, String password) throws SQLException {
		String dbURL = "jdbc:mysql://localhost:3306/shaka";
		
		StringJoiner opts = new StringJoiner("&", "?", "");
		opts.add("useUnicode=false");
		opts.add("useJDBCCompliantTimezoneShift=true");
		opts.add("useLegacyDatetimeCode=false");
		opts.add("useLegacyDatetimeCode=false");
		opts.add("serverTimezone=UTC");
		opts.add("useSSL=false");
		
	    Connection conn = DriverManager.getConnection(dbURL + opts, username, password);
	    ServerConst.LOGGER.log(Level.INFO, "Connected to database.");
	    
	    connection = conn;
	}
	
	@Override
	public void contextInitialized(final ServletContextEvent context) {
		try {
			SQLUtils.createConnection(USER, PASSWORD);
			ServerConst.LOGGER.log(Level.INFO, "Connection initialized!");
		} catch (SQLException e) {
		 e.printStackTrace();
		}
	}
	
	public static boolean isConnectionValid() {
		try {
			return connection.isValid(3);
		} catch (SQLException e) {
			return false;
		}
	}
	
	public static Connection getConnection() {
		return connection;
	}
}
