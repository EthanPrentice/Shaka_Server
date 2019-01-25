package tables;

public enum PartyTable implements Table {
	
	ID      ("ID"),
	STATUS  ("Status"),
	OWNER   ("Owner"),
	NAME    ("Name"),
	LAT     ("Latitude"),
	LONG    ("Longitude");
	
	private static final String tableName = "Parties";
	private String colName;
	
	private PartyTable(String colName) {
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
