package tables;

public enum SongTable implements Table {
	
	ID     		 ("ID"),
	PARTY 		 ("Party"),
	USER  		 ("User"),
	SONG_NAME    ("SongName"),
	SONG_URI     ("SongURI"),
	ALBUM_NAME   ("AlbumName"),
	ALBUM_URI 	 ("AlbumURI"),
	ARTIST_NAME  ("ArtistName"),
	ARTIST_URI 	 ("ArtistURI"),
	QUEUE_POS 	 ("QueuePos"),
	SHUFFLED_POS ("ShuffledPos"); // Not implemented yet
	
	private static final String tableName = "Songs";
	private String colName;
	
	private SongTable(String colName) {
		this.colName = colName;
	}

	public static String getTableName() {
		return tableName;
	}

	@Override
	public String getColumnName() {
		return colName;
	}
	
	@Override
	public String toString() {
		return getColumnName();
	}
	
}
