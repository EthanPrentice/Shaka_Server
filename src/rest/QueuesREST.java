/**
 * @author Ethan Prentice
 * 
 */


package rest;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import adt.AuthHeader;
import adt.Track;
import constants.ServerConst;
import sql.Songs;
import sql.Users;

@Path("/queue")
public class QueuesREST {	
	
	@POST
	@Consumes("application/json")
	@Path("/add")
	public Response addToQueue(Track track,
							   @HeaderParam("Authorization") String auth,
							   @QueryParam("partyID") String partyID) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Songs.addSong(partyID, authHeader.username, track);
				return Response.status(ServerConst.OK).entity("Successfully added song to end of queue.").build();
				
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to add songs to this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error adding the song to the queue.").build();
		}
		
	}
	
	@POST
	@Consumes("application/json")
	@Path("/playNext")
	public Response playNext(Track track,
			@HeaderParam("Authorization") String auth,
		    @QueryParam("partyID") String partyID) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Songs.playNext(partyID, authHeader.username, track);
				return Response.status(ServerConst.OK).entity("Successfully inserted song into the queue.").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to add songs to this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error inserting the song into the queue").build();
		}
		
	}

	
	@POST
	@Consumes("application/json")
	@Path("/insert")
	public Response insertIntoQueue(Track track,
									@HeaderParam("Authorization") String auth,
								    @QueryParam("partyID") String partyID,
								    @DefaultValue("0") @QueryParam("queuePos") int queuePos) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Songs.insertSong(partyID, authHeader.username, track, queuePos);
				return Response.status(ServerConst.OK).entity("Successfully inserted song into the queue.").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to add songs to this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error inserting the song into the queue").build();
		}
	}
	
	@GET
	@Consumes("application/json")
	@Path("/move")
	public Response changeQueuePos(@HeaderParam("Authorization") String auth,
									@QueryParam("partyID") String partyID, 
		    						@QueryParam("ID") int ID,
		    						@QueryParam("newPos") int newPos) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Songs.moveSong(partyID, ID, newPos);
				return Response.status(ServerConst.OK).entity("Successfully moved song " + ID + " to " + newPos).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to add songs to this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error removing the song from the queue.").build();
		}
	
	}

	@GET
	@Consumes("application/json")
	@Path("/remove")
	public Response removeFromQueue(@HeaderParam("Authorization") String auth,
									@QueryParam("partyID") String partyID, 
		    						@QueryParam("queuePos") int queuePos) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Songs.removeSong(partyID, queuePos);
				return Response.status(ServerConst.OK).entity("Successfully removed song from the queue.").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to add songs to this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error removing the song from the queue.").build();
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/get")
	public Response getSongs(@HeaderParam("Authorization") String auth,
							 @QueryParam("partyID") String partyID,
							 @QueryParam("start") int start,
							 @QueryParam("amount") int amount) {
		
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			if (Users.isUserInParty(partyID, authHeader.username)) {
				
				Track[] tracks;
				if (amount == 0) {
					tracks = Songs.getQueue(partyID, start, Songs.getQueueLength(partyID)-(start + 1), false);
				} else {
					tracks = Songs.getQueue(partyID, start, amount, false);
				}
				return Response.status(ServerConst.OK).entity(tracks).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not allowed to get songs from this queue.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error getting the songs from the queue.").build();
		}
	}
}
