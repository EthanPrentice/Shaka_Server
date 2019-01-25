/**
 * @author Ethan Prentice
 * 
 */


package sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import adt.SpotifyImage;
import adt.Track;
import constants.ServerConst;
import tables.AlbumImgTable;
import tables.SongTable;


public class Songs extends SQLModifier {
	
	private final static String GET_QUEUE_LEN_SQL  	= String.format("SELECT MAX(%s) as %s FROM %s WHERE %s = ?", 	SongTable.QUEUE_POS, SongTable.QUEUE_POS, SongTable.getTableName(), SongTable.PARTY);
	private final static String ADD_ALBUM_IMG_STR  	= "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s = ?";
	private final static String ADD_ALBUM_IMG_SQL  	= String.format(ADD_ALBUM_IMG_STR, 							AlbumImgTable.getTableName(), AlbumImgTable.URL, AlbumImgTable.ALBUM, AlbumImgTable.WIDTH, AlbumImgTable.HEIGHT, AlbumImgTable.URL);
	private final static String GET_ALBUM_IMGS_SQL 	= String.format("SELECT * FROM %s WHERE %s = ?", 			AlbumImgTable.getTableName(), AlbumImgTable.ALBUM);
	
	private final static String ADD_SONG_STR		= "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String ADD_SONG_SQL       	= String.format(ADD_SONG_STR, SongTable.getTableName(), SongTable.PARTY, SongTable.USER, SongTable.SONG_NAME, SongTable.SONG_URI, SongTable.ALBUM_NAME, SongTable.ALBUM_URI, SongTable.ARTIST_NAME, SongTable.ARTIST_URI, SongTable.QUEUE_POS);
	private final static String OFFSET_SONGS_SQL   	= String.format("UPDATE %s SET %s = %s+(?) WHERE %s between ? and ? AND %s = ? ORDER BY %s", SongTable.getTableName(), SongTable.QUEUE_POS, SongTable.QUEUE_POS, SongTable.QUEUE_POS, SongTable.PARTY, SongTable.QUEUE_POS);
	private final static String GET_TRACK_POS	   	= String.format("SELECT %s from %s where %s = ?", SongTable.QUEUE_POS, SongTable.getTableName(), SongTable.ID);
	private final static String DEL_SONG_SQL		= String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", SongTable.getTableName(), SongTable.PARTY, SongTable.QUEUE_POS);
	
	private final static String GET_QUEUE_SQL		= String.format("SELECT * FROM %s WHERE %s = ? AND %s BETWEEN ? AND ? ORDER BY %s", SongTable.getTableName(), SongTable.PARTY, SongTable.QUEUE_POS, SongTable.QUEUE_POS);
	private final static String GET_QUEUE_SHUFFLED_SQL = String.format("SELECT * FROM %s WHERE %s = ? AND %s BETWEEN ? AND ? ORDER BY %s", SongTable.getTableName(), SongTable.PARTY, SongTable.QUEUE_POS, SongTable.SHUFFLED_POS);
	
	private final static String GET_SONG_AT    = String.format("SELECT * FROM %s WHERE %s = ? AND %s = ?", SongTable.getTableName(), SongTable.PARTY, SongTable.QUEUE_POS);
	private final static String GET_SONG_BY_ID = String.format("SELECT * FROM %s WHERE %s = ?", SongTable.getTableName(), SongTable.ID);
	
	
	public static int getQueueLength(String partyID) throws SQLException {
		String query = GET_QUEUE_LEN_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);

		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		if (rs.next()) {
			return rs.getInt(SongTable.QUEUE_POS.getColumnName()) + 1;
		}

		return 0;
	}
	
	public static void addAlbumImage(String albumURI, SpotifyImage image) throws SQLException {
		String query = ADD_ALBUM_IMG_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		pStmt.setString(1, image.url);
		pStmt.setString(2, albumURI);
		pStmt.setInt(3, image.width);
		pStmt.setInt(4, image.height);
		pStmt.setString(5, image.url);
			
		// ServerConst.LOGGER.info("Executing update : " + pStmt);
		pStmt.executeUpdate();
		
	}
	
	public static SpotifyImage[] getAlbumImages(String albumURI) throws SQLException {
		String query = GET_ALBUM_IMGS_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, albumURI);
		
		
		// ServerConst.LOGGER.info("Executing query : " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		
		ArrayList<SpotifyImage> images = new ArrayList<SpotifyImage>();
		while (rs.next()) {
			images.add(new SpotifyImage(
				rs.getString(AlbumImgTable.URL.getColumnName()),
				rs.getInt(AlbumImgTable.WIDTH.getColumnName()),
				rs.getInt(AlbumImgTable.HEIGHT.getColumnName())					
			));
		}
		
		if (images.size() > 0) {
			return images.toArray(new SpotifyImage[images.size()-1]);
		} else {
			return new SpotifyImage[0];
		}
		
	}
	
	// add song to end of queue
	public static void addSong(String partyID, String userID, Track track) throws SQLException {	
		int queuePos = getQueueLength(partyID);
		
		String query = ADD_SONG_SQL;
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		pStmt.setString(2, userID);
		pStmt.setString(3, track.name);
		pStmt.setString(4, track.uri);
		pStmt.setString(5, track.albumName);
		pStmt.setString(6, track.albumURI);
		pStmt.setString(7, track.artistName);
		pStmt.setString(8, track.artistURI);
		pStmt.setInt(9, queuePos);
		
		// ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		pStmt.executeUpdate();
		
		for (SpotifyImage image : track.albumImages) { 
			addAlbumImage(track.albumURI, image);
		}

	}
	
	public static void offsetSongs(String partyID, int start, int end, int amount) throws SQLException {
		if (amount == 0)
			return;
		
		String query = OFFSET_SONGS_SQL;
		
		if (amount > 0) 
			query += " DESC";
		else 
			query += " ASC";
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		
		pStmt.setInt(1, amount);
		pStmt.setInt(2, start);
		pStmt.setInt(3, end);
		pStmt.setString(4, partyID);
	    
		ServerConst.LOGGER.log(Level.INFO, "Executing update: " + pStmt);
		pStmt.executeUpdate();
		
	}
	
	public static void insertSong(String partyID, String userID, Track track, int queuePos) throws SQLException {
		insertSong(partyID, userID, track, queuePos, true);
	}
	
	public static void insertSong(String partyID, String userID, Track track, int queuePos, boolean offset) throws SQLException {	
		if (0 > queuePos || getQueueLength(partyID) < queuePos) {
			throw new IllegalArgumentException("Queue position must be within range (" + queuePos + ")");
		}
		
		String insertSong = ADD_SONG_SQL;
	    
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(insertSong);
		pStmt.setString(1, partyID);
		pStmt.setString(2, userID);
		pStmt.setString(3, track.name);
		pStmt.setString(4, track.uri);
		pStmt.setString(5, track.albumName);
		pStmt.setString(6, track.albumURI);
		pStmt.setString(7, track.artistName);
		pStmt.setString(8, track.artistURI);
		pStmt.setInt(9, queuePos);
		
		if (offset)
			offsetSongs(partyID, queuePos, getQueueLength(partyID)-1, 1);
			    
		// Add song into the the queue at queuePos
		ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		pStmt.executeUpdate();
		
		for (SpotifyImage image : track.albumImages) { 
			addAlbumImage(track.albumURI, image);
		}
	}
	
	private static Track getSongAt(String partyID, int queuePos) throws SQLException {
		String query = GET_SONG_AT;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		pStmt.setInt(2, queuePos);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing query : " + pStmt.toString());
			
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
	
	
	private static Track getSongByID(int ID) throws SQLException {
		String query = GET_SONG_BY_ID;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setInt(1, ID);
		
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
	
	private static int getTrackPos(int ID) throws SQLException {
		String query = GET_TRACK_POS;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setInt(1, ID);
		
		ServerConst.LOGGER.info("Executing query: " + pStmt);
		ResultSet rs = pStmt.executeQuery();
		if (rs.next()) {
			return rs.getInt(SongTable.QUEUE_POS.getColumnName());
		}
		return -1;
	}
	
	public static void playNext(String partyID, String userID, Track track) throws SQLException {
		Integer currPos = Players.getCurrTrackNum(partyID);
		insertSong(partyID, userID, track, currPos + 1);
	}

	
	/*
	 * TODO: Make more efficient (currently making 4 calls to the database)
	 */
	public static void moveSong(String partyID, int ID, int newQueuePos) throws SQLException {
		Track track = getSongByID(ID);
		int oldQueuePos = getTrackPos(ID);
		
		if (oldQueuePos == newQueuePos) {
			return;
		}
		
		if (oldQueuePos == -1) {
			throw new SQLException("Could not find track with ID " + oldQueuePos);
		}
		
		int maxPos = Math.max(oldQueuePos, newQueuePos);
		int minPos = Math.min(oldQueuePos, newQueuePos);
		if (minPos < 0 || getQueueLength(partyID) < maxPos) {
			throw new IllegalArgumentException("Queue position must be in range (" + oldQueuePos + ")");
		}
		
		removeSong(partyID, oldQueuePos);
		
		// Offset tracks to make room for insert
		if (oldQueuePos < newQueuePos) {
			offsetSongs(partyID, minPos, maxPos, -1);
		} else if (oldQueuePos > newQueuePos) {
			offsetSongs(partyID, minPos, maxPos, 1);
		}
		
		
		insertSong(partyID, track.username, track, newQueuePos, false);
	}

	
	public static void removeSong(String partyID, int queuePos) throws SQLException {
		if (queuePos < 0 || getQueueLength(partyID) <= queuePos)
			return;
		
		String query = DEL_SONG_SQL;
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(query);
		pStmt.setString(1, partyID);
		pStmt.setInt(2, queuePos);
			    
		// Remove song at location queuePos
		ServerConst.LOGGER.log(Level.INFO, "Executing query: " + pStmt);
		pStmt.executeUpdate();

	}
	
	public static Track[] getQueue(String partyID, int start, int amount, boolean shuffled) throws SQLException {
		ArrayList<Track> tracks = new ArrayList<Track>();
		
		String getSongs;
		if (!shuffled) {
			getSongs = GET_QUEUE_SQL;
		} else {
			getSongs = GET_QUEUE_SHUFFLED_SQL;
		}
		
		if (start < 0) {
			start = 0;
		}
		
		// "SELECT * FROM %s WHERE %s = ? AND %s BETWEEN ? AND ? ORDER BY %s", SongTable.getTableName(), SongTable.PARTY, SongTable.QUEUE_POS, SongTable.QUEUE_POS
		
		PreparedStatement pStmt = SQLUtils.getConnection().prepareStatement(getSongs);
		pStmt.setString(1, partyID);
		pStmt.setInt(2,  start);
		pStmt.setInt(3, start + amount);
			
		// ServerConst.LOGGER.log(Level.INFO, "Executing query : " + pStmt.toString());
		
		ResultSet rs = pStmt.executeQuery();
		while (rs.next()) {
			Track track  = new Track(
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
			tracks.add(track);
		}
		
		if (tracks.size() > 0) {
			return tracks.toArray(new Track[tracks.size()-1]);
		} else {
			return new Track[0];
		}
	}
	
}
