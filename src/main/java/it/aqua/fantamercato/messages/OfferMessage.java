package it.aqua.fantamercato.messages;

public class OfferMessage {
	private String offerBy;
	private String playerId;
	private String playerName;
	private String playerRole;
	private Integer price;
	
	
	public String getOfferBy() {
		return offerBy;
	}
	public void setOfferBy(String offerBy) {
		this.offerBy = offerBy;
	}
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public String getPlayerRole() {
		return playerRole;
	}
	public void setPlayerRole(String playerRole) {
		this.playerRole = playerRole;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	
	

}
