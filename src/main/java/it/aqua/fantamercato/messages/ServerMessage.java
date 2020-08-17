package it.aqua.fantamercato.messages;

import java.util.List;

import it.aqua.fantamercato.persistance.models.Player;
import it.aqua.fantamercato.persistance.models.Team;

public class ServerMessage {
	private String type;
	private List<Player> goalkeepers;
	private List<Player> defenders;
	private List<Player> midfielders;
	private List<Player> strikers;
	private List<Team> teams;
	private String nextBidder;
	private String content;


	public ServerMessage() {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Player> getGoalkeepers() {
		return goalkeepers;
	}

	public void setGoalkeepers(List<Player> goalkeepers) {
		this.goalkeepers = goalkeepers;
	}

	public List<Player> getDefenders() {
		return defenders;
	}

	public void setDefenders(List<Player> defenders) {
		this.defenders = defenders;
	}

	public List<Player> getMidfielders() {
		return midfielders;
	}

	public void setMidfielders(List<Player> midfielders) {
		this.midfielders = midfielders;
	}

	public List<Player> getStrikers() {
		return strikers;
	}

	public void setStrikers(List<Player> strikers) {
		this.strikers = strikers;
	}

	public List<Team> getTeams() {
		return teams;
	}

	public void setTeams(List<Team> teams) {
		this.teams = teams;
	}

	public String getNextBidder() {
		return nextBidder;
	}

	public void setNextBidder(String nextBidder) {
		this.nextBidder = nextBidder;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
	
	
}
