package tables;

public enum AlbumImgTable implements Table {
	
	URL     ("Url"),
	ALBUM   ("Album"),
	WIDTH 	("Width"),
	HEIGHT  ("Height");
	
	private static final String tableName = "AlbumImages";
	private String colName;
	
	private AlbumImgTable(String colName) {
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
