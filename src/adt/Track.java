/**
 * @author Ethan Prentice
 * 
 */


package adt;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

//Ignores all undeclared properties returned by the API
@JsonIgnoreProperties(ignoreUnknown=true)
public class Track {
	
	public int ID;
	public String username;
	public SpotifyImage[] albumImages;

	public String artistName;
	public String artistURI;
	
	public String albumName;
	public String albumURI;
	
    public String name;
    public String uri;
	
	public Track() {}
	
	public Track(int ID, String username, String artistName, String artistURI,
			String albumName, String albumURI, String songName, String songURI, SpotifyImage[] images) {
		
		this.ID = ID;		
		this.username = username;
		this.artistName = artistName;
		this.artistURI = artistURI;
		this.albumName = albumName;
		this.albumURI = albumURI;
		this.name = songName;
		this.uri = songURI;
		albumImages = images;
	}

}

@JsonIgnoreProperties(ignoreUnknown=true)
class NameURI {
	public String name;
	public String uri;
}
