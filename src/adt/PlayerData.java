package adt;

import java.util.ArrayList;

import tables.PlayerStatus;

public class PlayerData {
	
	public String party;
	public PlayerStatus status;
	public int trackTime;
	public int currTrack;
	
    public ArrayList<Track> tracks;
	
	public PlayerData(String party, PlayerStatus status, int trackTime, int currTrack) {
		this.party = party;
		this.status = status;
		this.trackTime = trackTime;
		this.currTrack = currTrack;
	}
	
	public PlayerData() {
		
	}
	
}
