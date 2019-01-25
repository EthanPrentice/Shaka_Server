package tables;

public enum UserImgTable implements Table {
	
	URL     ("Url"),
	USER    ("USER"),
	WIDTH 	("Width"),
	HEIGHT  ("Height");
	
	private static final String tableName = "UserImages";
	private String colName;
	
	private UserImgTable(String colName) {
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
