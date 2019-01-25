package tables;

public enum UserType {

	PLAYER   (1001),
	USER     (1002),
	INACTIVE (1003);
	
	private static final String tableName = "UserStatuses";
	private int val;
	
	private UserType (int val) {
		this.val = val;
	}
	
	public static String getTableName() {
		return tableName;
	}

	public int getValue() {
		return val;
	}

}