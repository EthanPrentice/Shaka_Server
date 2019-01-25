package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import adt.PlayerData;
import adt.Track;
import constants.ServerConst;
import tables.PlayerStatus;
import tables.PlayerTable;
import tables.SongTable;

public class Players extends SQLModifier {
	
	private final static String CREATE_PLAYER_SQL  	   = String.format("INSERT INTO %s (%s, %s) VALUES (?, %s)", 			PlayerTable.getTableName(), PlayerTable.PARTY, PlayerTable.STATUS, PlayerStatus.STOPPED.getValue());
	private final static String UPDATE_STATUS_SQL      = String.format("UPDATE %s SET %s = ? WHERE %s = ?", 				PlayerTable.getTableName(), PlayerTable.STATUS, PlayerTable.PARTY);
	private final static String SET_CURR_TRACK_SQL 	   = String.format("UPDATE %s SET %s = ? WHERE %s = ?", 				PlayerTable.getTableName(), PlayerTable.CURR_TRACK, PlayerTable.PARTY);
	private final static String GET_PLAYER_SQL     	   = String.format("SELECT * FROM %s WHERE %s = ?", 					PlayerTable.getTableName(), PlayerTable.PARTY);
	private final static String GET_CURR_TRACK_SQL 	   = String.format("SELECT T.* FROM %s P JOIN %s S on S.%s = P.%s", 	PlayerTable.getTableName(), SongTable.getTableName(), SongTable.ID, PlayerTable.CURR_TRACK);
	private final static String GET_CURR_TRACK_NUM_SQL = String.format("SELECT %s FROM %s S JOIN %s P ON P.%s = S.%s AND S.%s = ?", SongTable.QUEUE_POS, SongTable.getTableName(), PlayerTable.getTableName(), PlayerTable.CURR_TRACK, SongTable.ID, SongTable.PARTY);


	public static void createPlayer (String partyID) throws SQLException {
		String query = CREATE_PLAYER_SQL;
			
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	
	public static void updateStatus(String partyID, PlayerStatus status) throws SQLException {
		String query = UPDATE_STATUS_SQL;
				
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setInt(1, status.getValue());
		pStmt.setString(2, partyID);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static void setCurrTrack(String partyID, int currTrack) throws SQLException {
		String query = SET_CURR_TRACK_SQL;
				
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setInt(1, currTrack);
		pStmt.setString(2, partyID);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
	}
	
	public static PlayerData getPlayer(String partyID) throws SQLException {
		String query = GET_PLAYER_SQL;
				
	    PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		if (rs.next()) {
			PlayerData data = new PlayerData (
				rs.getString(PlayerTable.PARTY.getColumnName()),
				getStatus(rs.getInt(PlayerTable.STATUS.getColumnName())),
				rs.getInt(PlayerTable.TRACK_MS.getColumnName()),	
				rs.getInt(PlayerTable.CURR_TRACK.getColumnName())
			);
			
			data.tracks = new ArrayList<Track>(Arrays.asList( Songs.getQueue(partyID, 0, Songs.getQueueLength(partyID)-1, false) ));
			
			return data;
		}
			
		return null;
	}
	
	public static PlayerStatus getStatus(int statusID) throws SQLException {
		for (PlayerStatus status : PlayerStatus.values()) {
			if (status.getValue() == statusID) {
				return status;
			}
		}
		
		return null;
	}
	
	public static Track getCurrTrack(String partyID) throws SQLException {
		String query = GET_CURR_TRACK_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		
		ResultSet rs = pStmt.executeQuery();
		
		if (rs.next()) {
			return new Track(
				rs.getInt(SongTable.ID.getColumnName()),
				rs.getString(SongTable.USER.getColumnName()),
				rs.getString(SongTable.ARTIST_NAME.getColumnName()),
				rs.getString(SongTable.ARTIST_URI.getColumnName()),
				rs.getString(SongTable.ALBUM_NAME.getColumnName()),
				rs.getString(SongTable.ALBUM_URI.getColumnName()),
				rs.getString(SongTable.SONG_NAME.getColumnName()),
				rs.getString(SongTable.SONG_URI.getColumnName()),
				Songs.getAlbumImages(rs.getString(SongTable.ALBUM_URI.getColumnName()))
			);
		}
		
		return null;
	}
	
	public static int getCurrTrackNum(String partyID) throws SQLException {
		String query = GET_CURR_TRACK_NUM_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		
		ResultSet rs = pStmt.executeQuery();
		
		if (rs.next()) {
			return rs.getInt(SongTable.QUEUE_POS.getColumnName());
		}
		
		return -1;
	}
	
	
	
}
