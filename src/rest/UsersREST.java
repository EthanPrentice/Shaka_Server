package rest;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

import adt.AuthHeader;
import adt.User;
import constants.ServerConst;
import sql.Users;

@Path("/users")
public class UsersREST {

	
	@GET
	@Produces("application/json")
	@Path("/getUsers")
	public static Response getUsers(@HeaderParam("Authorization") String auth, 
									@QueryParam("partyID") String partyID) {
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				User[] users = Users.getUsersInParty(partyID);
				return Response.ok().entity(new ObjectMapper().writeValueAsString(users)).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error getting the users from the datavbase.").build();
		} catch (IOException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("Invalid data.").build();
		}
		
	}
	
	@GET
	@Produces("application/json")
	@Path("/isOwner")
	public static Response isOwner(@HeaderParam("Authorization") String auth,
								   @QueryParam("partyID") String partyID) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				return Response.ok().entity(Users.isUserOwner(partyID, authHeader.username)).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error getting the users from the datavbase.").build();
		}
	}
	
	@GET
	@Path("/delUser")
	public static Response delUserFromParty() {
		
		return Response.ok().build();
	}
	
}
