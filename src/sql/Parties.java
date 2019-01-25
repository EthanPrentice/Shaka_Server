/**
 * @author Ethan Prentice
 * 
 */


package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;

import adt.Party;
import adt.User;
import constants.ServerConst;
import tables.PartyTable;

public class Parties extends SQLModifier {
	
	final static String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	private final static String ADD_PARTY_ID_SQL 	= String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)", 	PartyTable.getTableName(), PartyTable.ID, PartyTable.STATUS, PartyTable.OWNER, PartyTable.NAME);
	private final static String DEL_PARTY_SQL 		= String.format("DELETE FROM %s WHERE %s = ?", 							PartyTable.getTableName(), PartyTable.ID);
	private final static String CHANGE_OWNER_SQL 	= String.format("UPDATE %s SET %s = ? WHERE %s = ?", 					PartyTable.getTableName(), PartyTable.OWNER, PartyTable.ID);
	private final static String GET_PARTY_IDS_SQL	= String.format("SELECT %s FROM %s", 									PartyTable.ID, PartyTable.getTableName());
	private final static String GET_PARTY_SQL 		= String.format("SELECT * FROM %s WHERE %s = ?", 						PartyTable.getTableName(), PartyTable.ID);
	private final static String GET_NEARBY_SQL 		= String.format("SELECT * FROM %s WHERE (%s between ? and ?) and (%s between ? and ?)", PartyTable.getTableName(), PartyTable.LAT, PartyTable.LONG);
	private final static String UPDATE_LOC_SQL 		= String.format("UPDATE %s SET %s=?, %s=? WHERE %s=?", 					PartyTable.getTableName(), PartyTable.LAT, PartyTable.LONG, PartyTable.OWNER);
	
			
	public static void addPartyID(User auth, String partyID) throws SQLException {		
		String query = ADD_PARTY_ID_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		pStmt.setString(2, "empty");
		pStmt.setString(3, auth.username);
		pStmt.setString(4, auth.displayName.split(" ")[0] + "'s Party");
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
		
		Users.addUserToParty(partyID, auth);
		changeOwner(partyID, auth.username);
		
		Players.createPlayer(partyID);
	}
	
	
	public static void deleteParty(String id) throws SQLException {
		
		// Delete queues -> users -> party because of fk constraints
		Users.deleteUsersByPartyID(id);
			
		String query = DEL_PARTY_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, id);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing update " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static void changeOwner(String partyID, String username) throws SQLException {
		String query = CHANGE_OWNER_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, username);
		pStmt.setString(2, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static ArrayList<String> getPartyIDs() throws SQLException {
		String query = GET_PARTY_IDS_SQL;
		ArrayList<String> idParties = new ArrayList<String>();
		    
		try {
		    Statement stmt = SQLUtils.getConnection().createStatement();
		    ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		        idParties.add(rs.getString(PartyTable.ID.getColumnName()));
		    }
		} catch (SQLException e ) {
		    e.printStackTrace();
		    return null;
		}
		
		return idParties;
	}
	
	public static Party getParty(String partyID) throws SQLException {
		String query = GET_PARTY_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		if (rs.next()) {
			return new Party(
					rs.getString("ID"),
					rs.getString("Status"),
					rs.getString("Name"),
					rs.getString("Owner"),
					rs.getDouble("Latitude"),
					rs.getDouble("Longitude"));	
		} else {
			return null;
		}
	}
	
	// Generates random length 6 alphanumeric string
	public static String generatePartyID() throws SQLException {
		StringBuilder builder = new StringBuilder();
		
		do {
			int count = 6;
			while (count-- != 0) {
				int character = (int)(Math.random() * ALPHA_NUMERIC_STRING.length());
				builder.append(ALPHA_NUMERIC_STRING.charAt(character));
			}
		} while (getPartyIDs().contains(builder.toString()));
		
		return builder.toString();
	}
	
	public static Party[] getNearbyParties(double lat, double lng) throws SQLException {
		String query = GET_NEARBY_SQL;
		ArrayList<Party> parties = new ArrayList<Party>();
		float radius = 0.005f;
		    
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setDouble(1, lat-radius);
		pStmt.setDouble(2, lat+radius);
		pStmt.setDouble(3, lng-radius);
		pStmt.setDouble(4, lng+radius);

		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);

		ResultSet rs = pStmt.executeQuery();
		while (rs.next()) {			
			float d = distFrom(lat, lng, rs.getFloat(PartyTable.LAT.getColumnName()), rs.getFloat(PartyTable.LONG.getColumnName()));
			
			if (d < 200) {
				parties.add(
					new Party(
						rs.getString(PartyTable.ID.getColumnName()),
						rs.getString(PartyTable.STATUS.getColumnName()),
						rs.getString(PartyTable.NAME.getColumnName()),
						rs.getString(PartyTable.OWNER.getColumnName()),
						rs.getDouble(PartyTable.LAT.getColumnName()),
						rs.getDouble(PartyTable.LONG.getColumnName())
					)
				);
			}
		}
		
		return parties.toArray(new Party[parties.size()]);
	}
	
	public static void updateLocation(String username, double lat, double lng) throws SQLException {
		String query = UPDATE_LOC_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setDouble(1, lat);
		pStmt.setDouble(2, lng);
		pStmt.setString(3, username);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update " + pStmt);
		pStmt.executeUpdate();		
	}
	
	// Haversine formula
	private static float distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 6371000;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		           Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
		           Math.sin(dLng/2) * Math.sin(dLng/2);
		
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		float dist = (float) (earthRadius * c);
		
		return dist;
	 }
	
}
