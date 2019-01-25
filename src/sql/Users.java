/**
 * @author Ethan Prentice
 * 
 */


package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.ArrayList;

import adt.AuthHeader;
import adt.User;
import adt.UserImage;
import constants.ServerConst;
import tables.PartyTable;
import tables.UserImgTable;
import tables.UserTable;
import tables.UserType;

public class Users extends SQLModifier {
	
	private final static String ADD_TO_PARTY_SQL   = String.format("INSERT INTO %s (%s,%s,%s,%s,%s,%s,%s,%s,%s) VALUES (?,?,?,?,?,?,?,?,?)", 
																   UserTable.getTableName(), UserTable.ID, UserTable.PARTY, UserTable.ACCESS_TOK, UserTable.REFRESH_TOK, UserTable.SCOPES, UserTable.TOK_TYPE, UserTable.EXPIRES, UserTable.D_NAME, UserTable.STATUS);
	
	private final static String ADD_USER_IMGS_SQL  = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES(?, ?, ?, ?)", UserImgTable.getTableName(), UserImgTable.USER, UserImgTable.URL, UserImgTable.WIDTH, UserImgTable.HEIGHT);
	private final static String UPDATE_USER_SQL    = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=? AND %s=?", 
			                                                       UserTable.getTableName(), UserTable.ACCESS_TOK, UserTable.REFRESH_TOK, UserTable.SCOPES, UserTable.TOK_TYPE, UserTable.EXPIRES, UserTable.D_NAME, UserTable.PARTY, UserTable.ID);
	
	private final static String RMV_FROM_PARTY     = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", UserTable.getTableName(), UserTable.PARTY, UserTable.ID);
	private final static String RMV_ALL_FROM_PARTY = String.format("DELETE FROM %s WHERE %s = ?", UserTable.getTableName(), UserTable.PARTY);
	private final static String DEL_BY_USERNAME    = String.format("DELETE FROM %s WHERE %s = ?", UserTable.getTableName(), UserTable.ID);
	private final static String CHECK_EXISTS_SQL   = String.format("SELECT %s FROM %s WHERE %s=? LIMIT 1", UserTable.ID, UserTable.getTableName(), UserTable.ID);
	private final static String GET_ALL_IN_PARTY_SQL      = String.format("SELECT * FROM %s WHERE %s=?", UserTable.getTableName(), UserTable.PARTY);
	
	private final static String GET_USER_IMGS 	   = String.format("SELECT * FROM %s WHERE %s = ?", UserImgTable.getTableName(), UserImgTable.USER);
	private final static String RMV_USER_IMGS 	   = String.format("DELETE FROM %s WHERE %s = ?", UserImgTable.getTableName(), UserImgTable.USER);
	
	private final static String GET_USER_PARTIES   = String.format("SELECT %s FROM %s WHERE %s = ?", UserTable.PARTY, UserTable.getTableName(), UserTable.ID);
	private final static String USER_IN_PARTY 	   = String.format("SELECT %s FROM %s WHERE %s = ?", UserTable.PARTY, UserTable.getTableName(), UserTable.ID);
	
	private final static String IS_OWNER 		   = String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", PartyTable.OWNER, PartyTable.getTableName(), PartyTable.OWNER, PartyTable.ID);
	private final static String IS_VALID		   = String.format("SELECT %s FROM %s WHERE %s =? and %s = ?", UserTable.ID, UserTable.getTableName(), UserTable.ID, UserTable.ACCESS_TOK);
	

	public static void addUserToParty(String partyID, User user) throws SQLException {		
		String query = ADD_TO_PARTY_SQL;
		    
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, user.username);
		pStmt.setString(2, partyID);
		pStmt.setString(3, user.token);
		pStmt.setString(4, user.refreshToken);
		pStmt.setString(5, user.scope);
		pStmt.setString(6, user.tokenType);
		pStmt.setString(7, user.expiresAt.toString());
		pStmt.setString(8, user.displayName);
		pStmt.setInt(9, UserType.USER.getValue());
			    
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
		
		if (user.images != null) {
			removeUserImages(user.username);
			addUserImages(user);
		}
	}
	
	public static void addUserImages(User user) throws SQLException {
		String query = ADD_USER_IMGS_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		for (UserImage image : user.images) {
			pStmt.setString(1, user.username);
			pStmt.setString(2, image.url);
			pStmt.setInt(3, image.width);
			pStmt.setInt(4, image.height);
			    
			// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
			pStmt.executeUpdate();
		}
	}
	
	public static void updateUser(String partyID, User user) throws SQLException {
		String query = UPDATE_USER_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		pStmt.setString(1, user.token);
		pStmt.setString(2, user.refreshToken);
		pStmt.setString(3, user.scope);
		pStmt.setString(4, user.tokenType);
		pStmt.setTimestamp(5, user.expiresAt);
		pStmt.setString(6, user.displayName);
		
		pStmt.setString(7, partyID);
		pStmt.setString(8, user.username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
		
		removeUserImages(user.username);
		addUserImages(user);
	}
	
	public static void removeUserFromParty(String partyID, String username) throws SQLException {	
		removeUserImages(username);
		
		String query = RMV_FROM_PARTY;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		pStmt.setString(2, username);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static void deleteUsersByPartyID(String id) throws SQLException {
		String query = RMV_ALL_FROM_PARTY;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, id);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static void deleteUserByUsername(String username) throws SQLException {
		if (usersParties(username).length == 1) { 
			removeUserImages(username);
		}
		
		String query = DEL_BY_USERNAME;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
			
		// ServerConst.LOGGER.log(Level.INFO, "Deleting user with ID '" + username + "'");
		pStmt.executeUpdate();
	}
	
	public static boolean userExists(User user) throws SQLException{
		String query = CHECK_EXISTS_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, user.username);
		
		ResultSet rs = pStmt.executeQuery();
		return rs.next();
	}
	
	public static User[] getUsersInParty(String partyID) throws SQLException {
		ArrayList<User> users = new ArrayList<User>();
		
		String query = GET_ALL_IN_PARTY_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing Query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		while (rs.next()) {
			User user = new User();
			user.username 		= rs.getString(UserTable.ID.getColumnName());		
			user.token 			= rs.getString(UserTable.ACCESS_TOK.getColumnName());
			user.refreshToken 	= rs.getString(UserTable.REFRESH_TOK.getColumnName());
			user.scope 			= rs.getString(UserTable.SCOPES.getColumnName());
			user.tokenType 		= rs.getString(UserTable.TOK_TYPE.getColumnName());
			user.expiresAt 		= rs.getTimestamp(UserTable.EXPIRES.getColumnName());
			user.displayName 	= rs.getString(UserTable.D_NAME.getColumnName());
			user.type 			= getType(rs.getInt(UserTable.STATUS.getColumnName()));
			user.images 		= getUserImages(user.username);
			user.owner 			= isUserOwner(partyID, user.username);
			
			users.add(user);
		}
		
		return users.toArray(new User[users.size()]);
	}
	
	public static UserType getType(int typeID) throws SQLException {
		for (UserType userType : UserType.values()) {
			if (userType.getValue() == typeID) {
				return userType;
			}
		}
		return null;
	}
	
	public static UserImage[] getUserImages(String username) throws SQLException {
		ArrayList<UserImage> images = new ArrayList<UserImage>();
		
		String query = GET_USER_IMGS;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			UserImage image = new UserImage();
			image.url    = rs.getString(UserImgTable.URL.getColumnName());
			image.width  = rs.getInt(UserImgTable.WIDTH.getColumnName());
			image.height = rs.getInt(UserImgTable.HEIGHT.getColumnName());
			
			images.add(image);
		}
		
		return images.toArray(new UserImage[images.size()]);
	}
	
	public static void removeUserImages(String username) throws SQLException {
		String query = RMV_USER_IMGS;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static String[] usersParties(String username) throws SQLException {
		ArrayList<String> parties = new ArrayList<String>();
		
		String query = GET_USER_PARTIES;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			parties.add( rs.getString(UserTable.PARTY.getColumnName()) );
		}
			
		return parties.toArray(new String[parties.size()]);
		
	}
	
	public static boolean isUserInParty(String partyID, User user) throws SQLException {
		String query = USER_IN_PARTY;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, user.username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			if (rs.getString(UserTable.PARTY.getColumnName()) == partyID) {
				return true;
			}
		}
			
		return false;
	}
	
	public static boolean isUserInParty(String partyID, String username) throws SQLException {
		String query = USER_IN_PARTY;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		while (rs.next()) {
			if (rs.getString(UserTable.PARTY.getColumnName()).equals(partyID)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isUserOwner(String partyID, String username) throws SQLException {
		String query = IS_OWNER;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		pStmt.setString(2, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		return rs.next();
	}
	
	public static boolean isValidUser(AuthHeader authHeader) throws SQLException {
		String query = IS_VALID;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		pStmt.setString(1, authHeader.username);
		pStmt.setString(2, authHeader.token);
		
		ResultSet rs = pStmt.executeQuery();
		return rs.next();
	}
	
}
