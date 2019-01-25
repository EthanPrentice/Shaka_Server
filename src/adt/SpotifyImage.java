package adt;

/**
 * Created by Ethan on 2018-07-07.
 */

public class SpotifyImage {
    public String url;
    public int width, height;
    
    public SpotifyImage() {}
    
    public SpotifyImage(String url, int width, int height) {
    	this.url = url;
    	this.width = width;
    	this.height = height;
    }
}

