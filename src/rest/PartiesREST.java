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
import adt.Party;
import adt.User;
import constants.ServerConst;
import sql.Parties;
import sql.Users;

@Path("/parties")
public class PartiesREST {
	
	
	@POST
	@Produces("application/json")
	@Path("/get")
	public static Response getParty(@HeaderParam("Authorization") String auth, String partyID) {
		try {
			AuthHeader authHeader = new AuthHeader(auth);
			
			if (Users.isUserOwner(partyID, authHeader.username)) {
				Party party = Parties.getParty(partyID);
				return Response.ok().entity(party).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are not in that party!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error accessing that party from the database.").build();
		}
	}
	
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/create")
	public static Response addParty(User user) {
		try {
			String partyID = Parties.generatePartyID();
			
			if (Users.usersParties(user.username).length < ServerConst.MAX_PARTIES) {
				Parties.addPartyID(user, partyID);
				return Response.ok().entity( Parties.getParty(partyID) ).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are already in the maximum (3) amount of parties!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error adding a party to the database.").build();
		}
		
	}
	
	@GET
	@Produces("application/json")
	@Path("/nearby")
	public static Response getNearby(@QueryParam("lat") double lat, @QueryParam("lng") double lng) {
		try {			
			Party[] parties = Parties.getNearbyParties(lat, lng);
			return Response.ok().entity(parties).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error adding a party to the database.").build();
		}
	}
	
	@GET
	@Path("/updateLocation")
	public static Response updateLocation(@HeaderParam("Authorization") String auth,
										  @QueryParam("lat") double lat, @QueryParam("lng") double lng) {
		
		AuthHeader authHeader = new AuthHeader(auth);
		try {
			if (Users.isValidUser(authHeader)) {
				Parties.updateLocation(authHeader.username, lat, lng);
				return Response.ok().build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You aren't a valid user.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error updating your location in the database.").build();
		}
		
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/join")
	public static Response joinParty(User user, @QueryParam("partyID") String partyID) {
		try {
			// Make sure user isn't already in party / hasn't reached maxParties
			String[] userParties = Users.usersParties(user.username);
			boolean userInParty = false;
			
			for (String party : userParties) {
				if (party.equals(partyID)) {
					userInParty = true;
				}
			}
			
			if (userInParty || userParties.length < ServerConst.MAX_PARTIES) {
				if (!userInParty) {
					Users.addUserToParty(partyID, user);
				}  else {
					Users.updateUser(partyID, user);
				}
				Party party = Parties.getParty(partyID);
				return Response.ok().entity(party).build();
			} else if (userParties.length >= ServerConst.MAX_PARTIES) {
				return Response.status(ServerConst.FORBIDDEN).entity("You are already in the maximum (3) amount of parties!").build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You are already in this party!").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was a server error joining the party.").build();
		}
	}
	
	@POST
	@Consumes("application/json")
	@Path("/delete")
	public static Response deleteParty(@HeaderParam("Authorization") String auth, String partyID) {
		AuthHeader authHeader = new AuthHeader(auth);
		try {
			if (Users.isUserInParty(partyID, authHeader.username)) {
				Parties.deleteParty(partyID);
				return Response.ok().entity(partyID).build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You can't delete that.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error adding a party to the database.").build();
		}
	}
	
	@POST
	@Consumes("application/json")
	@Path("/changeowner")
	public static Response changeOwner(@HeaderParam("Authorization") String auth, String partyID) {
		AuthHeader authHeader = new AuthHeader(auth);
		try {
			if (Users.isUserOwner(partyID, authHeader.username)) {
				Parties.changeOwner(partyID, authHeader.username);
				return Response.ok().build();
			} else {
				return Response.status(ServerConst.FORBIDDEN).entity("You don't have permission to do that.").build();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(ServerConst.SERVER_ERROR).entity("There was an error accessing the database.").build();
		}
	
	}

}
