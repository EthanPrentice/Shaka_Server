package tables;

public enum PlayerStatus {

	PLAYING  (1001),
	PAUSED   (1002),
	STOPPED  (1003),
	ERROR    (1004);
	
	private static final String tableName = "PlayerStatuses";
	private int val;
	
	private PlayerStatus (int val) {
		this.val = val;
	}
	
	public static String getTableName() {
		return tableName;
	}

	public int getValue() {
		return val;
	}

}
