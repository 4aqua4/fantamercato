package it.aqua.fantamercato.persistance.models;

import java.util.List;

import org.springframework.data.annotation.Id;

public class Team {
    @Id
    private String id;

    private String username;
    private String role;
    private String teamname;
    private String teamcolor;
	private String president;
    private int status;
    private List<Player> goalkeepers;
    private List<Player> defenders;
    private List<Player> midfielders;
    private List<Player> forwards;
    private int budget;
    
    
	public Team() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Team(String id, String username, String role, String teamname, String teamcolor, String president, int status, 
			List<Player> goalkeepers, List<Player> defenders, 
			List<Player> midfielders, List<Player> forwards, int budget) {
		super();
		this.id = id;
		this.username = username;
		this.role = role;
		this.teamname = teamname;
		this.teamcolor = teamcolor;
		this.president = president;
		this.status = status;
		this.goalkeepers = goalkeepers;
		this.defenders = defenders;
		this.midfielders = midfielders;
		this.forwards = forwards;
		this.budget = budget;
	}

	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
    public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getTeamname() {
		return teamname;
	}
	public void setTeamname(String teamname) {
		this.teamname = teamname;
	}
	public String getTeamcolor() {
		return teamcolor;
	}
	public void setTeamcolor(String teamcolor) {
		this.teamcolor = teamcolor;
	}
	public String getPresident() {
		return president;
	}
	public void setPresident(String president) {
		this.president = president;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
	public List<Player> getForwards() {
		return forwards;
	}
	public void setForwards(List<Player> forwards) {
		this.forwards = forwards;
	}
	public int getBudget() {
		return budget;
	}
	public void setBudget(int budget) {
		this.budget = budget;
	}
    
    
    
}
