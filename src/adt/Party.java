package adt;

public class Party {
	public String ID;
	public String status;
	public String name;
	public String owner;
	public double latitude;
	public double longitude;
	
	public Party(String partyID, String status,String name, String owner, double lat, double lng) {
		ID = partyID;
		this.status = status;
		this.name = name;
		this.owner = owner;
		latitude = lat;
		longitude = lng;
	}
	
	public Party() {}
}
