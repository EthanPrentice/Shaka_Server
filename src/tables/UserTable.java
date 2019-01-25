package tables;

public enum UserTable implements Table {
	
	ID     		("ID"),
	PARTY 		("Party"),
	ACCESS_TOK  ("AccessToken"),
	REFRESH_TOK ("RefreshToken"),
	SCOPES    	("Scopes"),
	TOK_TYPE   	("TokenType"),
	EXPIRES 	("ExpiresAt"),
	D_NAME		("DisplayName"),
	STATUS  	("Status");
	
	private static final String tableName = "Users";
	private String colName;
	
	private UserTable(String colName) {
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
