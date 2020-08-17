package it.aqua.fantamercato.persistance.models;

import org.springframework.data.annotation.Id;

public class Player {
    @Id
    private String id;

    private String role;
    private String name;
    private String realTeam;
    private String team;
    private int value;
    
    
    
	public Player() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Player(String id, String role, String name, String realTeam, String team, int value) {
		super();
		this.id = id;
		this.role = role;
		this.name = name;
		this.realTeam = realTeam;
		this.team = team;
		this.value = value;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRealTeam() {
		return realTeam;
	}
	public void setRealTeam(String realTeam) {
		this.realTeam = realTeam;
	}
	public String getTeam() {
		return team;
	}
	public void setTeam(String team) {
		this.team = team;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
    
    

}
