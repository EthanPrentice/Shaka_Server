package tables;

public enum PlayerTable implements Table {
	
	PARTY      ("Party"),
	STATUS     ("Status"),
	TRACK_MS   ("TrackTime"),
	CURR_TRACK ("CurrentTrack");
	
	private static final String tableName = "Players";
	private String colName;
	
	private PlayerTable(String colName) {
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
