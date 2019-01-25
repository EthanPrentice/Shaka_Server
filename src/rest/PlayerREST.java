package rest;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import adt.AuthHeader;
import adt.PlayerData;
import constants.ServerConst;
import sql.Players;
import sql.Users;
import tables.PlayerStatus;

@Path("/player")
public class PlayerREST {
	
	@GET
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/get")
	public static Response getPlayer(@HeaderParam("Authorization") String auth, @QueryParam("partyID") String partyID) {
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			
			if (Users.isUserInParty(partyID, authHeader.username)) {
				PlayerData player = Players.getPlayer(partyID);
				return Response.ok().entity(player).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error accessing that player from the database.").build();
		}
	}
	
	@POST
	@Consumes("application/json")
	@Path("/updateStatus")
	public static Response updateStatus(@HeaderParam("Authorization") String auth, @QueryParam("partyID") String partyID,
			PlayerStatus status) {
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Players.updateStatus(partyID, status);
				return Response.ok().entity("Updated status").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error accessing that player from the database.").build();
		}
		
	}
	
	@POST
	@Consumes("application/json")
	@Path("/changeTrack")
	public static Response setCurrTrack(@HeaderParam("Authorization") String auth, @QueryParam("partyID") String partyID, int trackNum) {
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Players.setCurrTrack(partyID, trackNum);
				return Response.ok().entity("Updated track number").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error accessing that player from the database.").build();
		}
		
	}

}
