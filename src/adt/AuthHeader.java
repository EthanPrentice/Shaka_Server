package adt;

public class AuthHeader {
	
	public String token;
	public String username;
	
	public AuthHeader(String s) {
		username = s.split(" : ")[0].trim();
		token = s.split(" : " )[1].trim();
	}

}
