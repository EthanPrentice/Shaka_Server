/**
 * @author Ethan Prentice
 * 
 */


package adt;

import java.io.IOException;
import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import tables.UserType;

public class User {

	@JsonProperty("access_token")   public String 		token;
	@JsonProperty("token_type")     public String 		tokenType;
	@JsonProperty("expires_at")     public Timestamp 	expiresAt;
	@JsonProperty("refresh_token")  public String 		refreshToken;
	@JsonProperty("display_name")   public String   	displayName;
									public String 		scope;
									public String 		username;
									public UserType 	type;
								    public UserImage[] 	images;
								    public boolean		owner=false;
	
	@Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			e.printStackTrace();
			return super.toString();
		}
	}

}
