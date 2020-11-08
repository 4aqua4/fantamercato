package it.aqua.fantamercato;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.aqua.fantamercato.messages.ClientMessage;
import it.aqua.fantamercato.messages.OfferMessage;
import it.aqua.fantamercato.messages.PlayerMessage;
import it.aqua.fantamercato.messages.ServerMessage;
import it.aqua.fantamercato.persistance.models.Player;
import it.aqua.fantamercato.persistance.models.Team;
import it.aqua.fantamercato.persistance.repositories.PlayerRepository;
import it.aqua.fantamercato.persistance.repositories.TeamRepository;

@Controller
public class FantamercatoController {
    private static final Logger logger = LoggerFactory.getLogger(FantamercatoController.class.getName());
    
    private static final String BASE_XLS_FILE_PATH = "D:/FANTAMERCATO_DB/importExcel/";
    //private static final String BASE_XLS_FILE_PATH = "/importFiles/";
    
    private HashMap<String, String> sessionUsers = new HashMap<String, String>();//id_sessione - id_utente
    
    private List<String> connectedClientId = new ArrayList<String>();
    
    //gestione tipo mercato I=iniziale M=campionato in corso
	private String marketType;

    
    public String getMarketType() {
		return marketType;
	}

	public void setMarketType(String marketType) {
		this.marketType = marketType;
	}


	//gestione turno
    private ArrayList<String> orderedTeamList; //lista team ordinati
    private int currentIdx = 0; //utente corrente nel turno
    private String lastBidder;	//utlimo offerente
    private String lastOffer;	//ultima offerta
    private ArrayList<String> quitTeamList; //elenco team ritirati dal turno

    private int lastIdxTurn = -1; //ultimo utente che ha iniziato un turno

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    
    @Autowired
    private SimpMessagingTemplate brokerMessagingTemplate;
    
/*    
    @MessageMapping("/fantamercato/broad")
    @SendTo("/topic/players")
    public Greeting greeting(HelloMessage message) throws Exception {
    	logger.debug("prima");
    	for(Player player : playerRepository.findAll()) {
        	logger.debug("in...");
    		logger.debug(player.toString());
    	}
    	
    	logger.debug("dopo");
    	
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }
*/
    
    @MessageMapping("/fantamercato/single")
    @SendToUser("/queue/single")
    public ServerMessage messageToUser(ClientMessage message) throws Exception {
logger.debug("messageType: "+message.getType());
    	
        Thread.sleep(1000); // simulated delay
        if(message.getType().equalsIgnoreCase("playersList")) {
        	//elenco player disponibili per ruolo
            ServerMessage msg = new ServerMessage();
            msg.setType("playersList");        
            msg.setGoalkeepers(playerRepository.findByRole("P"));
            msg.setDefenders(playerRepository.findByRole("D"));
            msg.setMidfielders(playerRepository.findByRole("C"));
            msg.setStrikers(playerRepository.findByRole("A"));

        	msg.setMarketType(getMarketType());

            return msg;
        }else if(message.getType().equalsIgnoreCase("teamsList")) {
        	//elenco team
            ServerMessage msg = new ServerMessage();
            msg.setType("teamsList");     
            List<Team>allTeams = teamRepository.findAll();
//logger.debug("****************************************************");
            for (Iterator iterator = allTeams.iterator(); iterator.hasNext();) {
				Team team = (Team) iterator.next();
//logger.debug("...id: "+team.getUsername());
				if(connectedClientId.contains(team.getUsername())) {
//logger.debug("è connesso");	
					team.setStatus(1);
				}	
			}
//logger.debug("****************************************************");
            msg.setTeams(allTeams);

            return msg;
        }else if(message.getType().equalsIgnoreCase("currentOffer")) {
        	//elenco team
            ServerMessage msg = new ServerMessage();
            msg.setType("currentOffer");        
            msg.setNextBidder(currentBidder());
            msg.setContent(lastOffer);

            return msg;
            /*
        }else if(message.getType().equalsIgnoreCase("importPlayers")) {
        	//ADMIN
        	int svd = mapPlayersExcelDatatoDB(message.getContent());
        	ServerMessage msg = new ServerMessage();
            msg.setType("saveResult");        
        	msg.setContent("saved "+svd+" teams");
            return msg;
        }else if(message.getType().equalsIgnoreCase("importTeams")) {
        	//ADMIN
        	int svd = mapTeamsExcelDatatoDB();
        	ServerMessage msg = new ServerMessage();
            msg.setType("saveResult");        
        	msg.setContent("saved "+svd+" teams");
            return msg;
            */
        }else {
        	return null;
        }

    }
    
    //BROAD da
    @MessageMapping("/fantamercato/broad")
    @SendTo("/topic/all")
    public ServerMessage messageToBroad(ClientMessage message) throws Exception {
//logger.debug("messageType: "+message.getType());
		if(message.getType().equalsIgnoreCase("bidderTurn")) {
			//settaggi nuovo turno
			setupStartTurn();
			//set currentIdx per nuovo turno
			currentIdx = nextTurnBeginnerIdx();
			//messaggio primo utente del nuovo turno
			String currentUserInTurn = orderedTeamList.get(currentIdx);
		    ServerMessage msg = new ServerMessage();
		    msg.setType("bidderTurn");
		    msg.setNextBidder(currentUserInTurn);
		    return msg;
			
		}        
		else if(message.getType().equalsIgnoreCase("offer")) {
        	lastBidder = message.getUsername();
			lastOffer = message.getContent();
            ServerMessage msg = new ServerMessage();
            String nextBidder = nextBidder();
            if(nextBidder.equals(lastBidder)){
            	//termina turno (qui non dovrebbe mai entrare)
                msg.setType("endTurn");
                msg.setContent(lastOffer);
            }else {
            	//continua il turno
            	//setta messaggio offerta
                msg.setType("offer");
                msg.setNextBidder(nextBidder);
                msg.setContent(lastOffer);
            }
            return msg;
        }else if(message.getType().equalsIgnoreCase("quitOffer")) {
        	//aggiunge al lista team che si sono ritirati dalle offerte del turno
        	quitTeamList.add(orderedTeamList.get(currentIdx));
            ServerMessage msg = new ServerMessage();
            
            String nextBidder = nextBidder();
            //se prossimo offerente è l'ultimo che ha fatto offerta --> è rimasto l'unico --> fine turno
            if(nextBidder.equals(lastBidder)){
            	//termina turno
            	//aggiorna DB (aggiunge player al team)
				assignPlayerToTeam();
				//setta messaggio fine turno
                msg.setType("endTurn");
                msg.setContent(lastOffer);
                
                //gestione tipo mercato
                msg.setMarketType(this.getMarketType());

            }else {
            	//continua il turno
            	//setta messaggio offerente ritirato
                msg.setType("quitOffer");
            	msg.setNextBidder(nextBidder);
            }
            return msg;
        }
//da
		//gestione tipo mercato
        else if(message.getType().equalsIgnoreCase("changeMarketType")) {
//        	logger.debug("-message.getContent()-----"+message.getContent());
        	setMarketType(message.getContent());
            ServerMessage msg = new ServerMessage();
            msg.setType("marketTypeChanged");
        	msg.setMarketType(getMarketType());
            return msg;
        }
		//leavePlayer
        else if(message.getType().equalsIgnoreCase("leavePlayer")) {
        	Player leftPlayer = removePlayerFromTeam(message.getUsername(), message.getContent());
            ServerMessage msg = new ServerMessage();
            String msgContent = "{\"name\":\""+leftPlayer.getName()+"("+leftPlayer.getRealTeam()+")\"}";
            msg.setType("leftPlayer");
            msg.setContent(msgContent);
            return msg;
        }
//a
        else {
        	return null;
        }
    }

    //A

    
    @EventListener
    public void connectionEstablished(SessionConnectedEvent sce)
    {
		MessageHeaders msgHeaders = sce.getMessage().getHeaders();
//		StompHeaderAccessor sha = StompHeaderAccessor.wrap(sce.getMessage());
		/*
logger.debug("------------------------------------");
logger.debug(""+msgHeaders.toString());
logger.debug("------------------------------------");
*/
		GenericMessage genericMsg = (GenericMessage) msgHeaders.get("simpConnectMessage");
		MessageHeaders genericMsgHeaders = genericMsg.getHeaders();
		String sessionId = (String)genericMsgHeaders.get("simpSessionId");
logger.debug("Connessione websocket stabilita. sessionId "+sessionId);
		
		Map nativeHeaders = (Map)genericMsgHeaders.get("nativeHeaders"); 
		LinkedList<String> userIdList = (LinkedList)nativeHeaders.get("userId");
		String userId = userIdList.get(0);
logger.debug("userId: "+userId);
		connectedClientId.add(userId);
		
		sessionUsers.put(sessionId, userId);
		
		if( logger.isDebugEnabled() )
		{
		    logger.debug("Connessione websocket stabilita. ID Utente "+userId);
		}

		//CREO LISTA UTENTI
		if(orderedTeamList == null) {
			orderedTeamList = getOrCreateOrderedTeamList();
		}
		
		if(quitTeamList == null) {
			quitTeamList = new ArrayList<String>();
		}
		
		//invio broadcast stato connessione
	    ServerMessage msg = new ServerMessage();
	    msg.setType("userStatusChanged");
	    msg.setContent("{\"userId\":\""+userId+"\",\"status\":\"1\"}");
		this.brokerMessagingTemplate.convertAndSend("/topic/all", msg);

    }

    @EventListener
    public void webSockectDisconnect(SessionDisconnectEvent sde)
    {
	    logger.debug("****DISCONNESSIONE***** ");
	    MessageHeaders msgHeaders = sde.getMessage().getHeaders();
	    /*
logger.debug("------------------------------------");
logger.debug(""+msgHeaders.toString());
logger.debug("------------------------------------");
*/
		String sessionId = (String)msgHeaders.get("simpSessionId");
		logger.debug("disconnessione. sessionId "+sessionId);

	    
		String userId = sessionUsers.get(sessionId);
		logger.debug("disconnessione. userId "+userId);
		connectedClientId.remove(userId);
		sessionUsers.remove(sessionId);
		
/*	    
        Principal princ = (Principal) msgHeaders.get("simpUser");
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(sde.getMessage());
        List<String> nativeHeaders = sha.getNativeHeader("userId");
        String userId = null;
        if( nativeHeaders != null )
        {
            userId = nativeHeaders.get(0);
            connectedClientId.remove(userId);
            if( logger.isDebugEnabled() )
            {
                logger.debug("Disconnesso. ID Utente "+userId);
            }
        }
        else
        {
            userId = princ.getName();
            connectedClientId.remove(userId);
            if( logger.isDebugEnabled() )
            {
                logger.debug("Disconnesso. ID Utente "+userId);
            }
        }
*/        
		//invio broadcast stato connessione
//	    logger.debug("****userId***** "+userId);
	    ServerMessage msg = new ServerMessage();
	    msg.setType("userStatusChanged");
	    msg.setContent("{\"userId\":\""+userId+"\",\"status\":\"0\"}");
		this.brokerMessagingTemplate.convertAndSend("/topic/all", msg);

    }
/*
    public List<String> getConnectedClientId()
    {
        return connectedClientId;
    }
    public void setConnectedClientId(List<String> connectedClientId)
    {
        this.connectedClientId = connectedClientId;
    }    
    */
    private ArrayList<String> getOrCreateOrderedTeamList(){
    	if(orderedTeamList == null) {
    		orderedTeamList = new ArrayList<String>();
    		List<Team> teams = teamRepository.findAll();
    		for(int i=0; i<teams.size(); i++) {
    			orderedTeamList.add(teams.get(i).getUsername());
    		}
    	}
    	return orderedTeamList;
    }
    
    //inizio turno
    private void setupStartTurn() {
    	//svuota lista team ritirati
    	quitTeamList = new ArrayList<String>();
    	//svuota lastBidder
    	lastBidder = null;
    }
    
    public int nextTurnBeginnerIdx() {
    	lastIdxTurn++;
    	if(lastIdxTurn == orderedTeamList.size()) {
    		lastIdxTurn = 0;
    	}
    	//TODO:fare controlli
    	return lastIdxTurn;
    }

    //prossimo team che deve fare offerta nel turno
    public String nextBidder() {
    	currentIdx++;
    	if(currentIdx == orderedTeamList.size()) {
    		currentIdx = 0;
    	}
    	//TODO:fare controlli
    	//se ritirato salta al prossimo
    	if(quitTeamList.contains(orderedTeamList.get(currentIdx))) {
    		return nextBidder();
    	}
    	String currentUserInTurn = orderedTeamList.get(currentIdx);
    	return currentUserInTurn;
    }

    public String currentBidder() {
    	String currentUserInTurn = orderedTeamList.get(currentIdx);
    	return currentUserInTurn;
    }

    
    private Player findPlayerInList(List<Player> playerList, String playerId) {
    	Player ret = null;
    	for (Player player : playerList) {
//    	    logger.debug("---player.getId()---> "+player.getId());
			if(playerId.equalsIgnoreCase(player.getId())) {
				ret = player;
			}
		}
    	return ret;
    }
    
    //aggiorna team su DB
    private Player removePlayerFromTeam(String teamUsername, String msgContent) {
    	ObjectMapper mapper = new ObjectMapper();
    	Player playerToRemove = null;
    	try {
    		
    		PlayerMessage obj = mapper.readValue(msgContent, PlayerMessage.class);
	    	String playerId = obj.getPlayerId();
	    	String playerRole = obj.getRole();
	    		    	
	    	Team team = teamRepository.findByUsername(teamUsername);
    		//rimuove player da team (dalla giusta lista)
//    	    logger.debug("---playerRole---> "+playerRole);
    		switch (playerRole) {
			case "P":
				playerToRemove = findPlayerInList(team.getGoalkeepers(), playerId);
				team.getGoalkeepers().remove(playerToRemove);
				break;
			case "D":
				playerToRemove = findPlayerInList(team.getDefenders(), playerId);
				team.getDefenders().remove(playerToRemove);
				break;
			case "C":
				playerToRemove = findPlayerInList(team.getMidfielders(), playerId);
				team.getMidfielders().remove(playerToRemove);
				break;
			case "A":
				playerToRemove = findPlayerInList(team.getForwards(), playerId);
				team.getForwards().remove(playerToRemove);
				break;

			default:
				break;
			}
	    	
/*    		
    	    logger.debug("****team.getBudget()***** "+team.getBudget());
    	    logger.debug("****playerToRemove***** "+playerToRemove);
    	    logger.debug("****playerToRemove.getValue()***** "+playerToRemove.getValue());
*/
    		//aggiorna budget del team
    		int newBuget = team.getBudget() + playerToRemove.getValue();
    		team.setBudget(newBuget);
    		//salva modifica del team
    		teamRepository.save(team);
	    	
	    	//rimette nella lista giocatori svincolati
    		playerRepository.insert(playerToRemove);
    		
    		//playerRepository.save(player);//TODO:togliere lasciato solo per non dover ricaricare elenco giocatori su DB
	    	
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return playerToRemove;
		
    }
    
    private void assignPlayerToTeam() {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			OfferMessage obj = mapper.readValue(lastOffer, OfferMessage.class);
	    	String teamUsername = obj.getOfferBy();
	    	String playerId = obj.getPlayerId();
	    	int price = obj.getPrice();
	    	String playerRole = obj.getPlayerRole();
	    		    	
	    	Team team = teamRepository.findByUsername(obj.getOfferBy());
	    	Optional<Player> oPlayer = playerRepository.findById(playerId);
	    	if(oPlayer.isPresent()) {
	    		//recupera Player
	    		Player player = oPlayer.get();
	    		//setta nuovi valori (team e valore)
	    		player.setValue(price);
	    		player.setTeam(team.getUsername());
	    		//aggiunge player a team (nella giusta lista)
	    		switch (playerRole) {
				case "P":
					team.getGoalkeepers().add(player);
					break;
				case "D":
					team.getDefenders().add(player);
					break;
				case "C":
					team.getMidfielders().add(player);
					break;
				case "A":
					team.getForwards().add(player);
					break;

				default:
					break;
				}
	    		//aggiorna budget del team
	    		int newBuget = team.getBudget()-price;
	    		team.setBudget(newBuget);
	    		//salva modifica del team
	    		teamRepository.save(team);
	    		//toglie dall'elenco Player liberi
	    		playerRepository.deleteById(playerId);
	    		//playerRepository.save(player);//TODO:togliere lasciato solo per non dover ricaricare elenco giocatori su DB
	    	}
	    	
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    //EXCEL
    /*
    public int mapPlayersExcelDatatoDB(String role) throws IOException {
    	if(role == null) {
    		return mapPlayersExcelDatatoDB();
    	}else {
        	List<Player> playersList = new ArrayList<Player>();
        	String FILE_NAME = BASE_XLS_FILE_PATH+role+".xlsx";
        	FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
        	
        	XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
        	XSSFSheet worksheet = workbook.getSheetAt(0);
            for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
            	XSSFRow row = worksheet.getRow(i);
            	Player tmpPlayer = new Player();
            	tmpPlayer.setRole(row.getCell(1).getStringCellValue());
            	tmpPlayer.setName(row.getCell(2).getStringCellValue());
            	tmpPlayer.setRealTeam(row.getCell(3).getStringCellValue());
            	playersList.add(tmpPlayer);
            }
        	
            playerRepository.deleteByRole(role);
            List<Player> savedPlayersList = playerRepository.saveAll(playersList);
            return savedPlayersList.size();
    	}
    }
    
    public int mapPlayersExcelDatatoDB() throws IOException {
    	List<Player> playersList = new ArrayList<Player>();
    	String FILE_NAME = BASE_XLS_FILE_PATH+"ALL.xlsx";
    	FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
    	XSSFSheet worksheet = workbook.getSheetAt(0);
        for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
        	XSSFRow row = worksheet.getRow(i);
        	//logger.debug("ID:"+row.getCell(0)+" Ruolo:"+row.getCell(1)+" Nme:"+row.getCell(2)+" Squadra:"+row.getCell(3));
        	Player tmpPlayer = new Player();
        	tmpPlayer.setRole(row.getCell(1).getStringCellValue());
        	tmpPlayer.setName(row.getCell(2).getStringCellValue());
        	tmpPlayer.setRealTeam(row.getCell(3).getStringCellValue());
        	playersList.add(tmpPlayer);
        }

        playerRepository.deleteAll();
        List<Player> savedPlayersList = playerRepository.saveAll(playersList);
        return savedPlayersList.size();
    }
    
    public int mapTeamsExcelDatatoDB() throws IOException {
    	List<Team> teamsList = new ArrayList<Team>();
    	String FILE_NAME = BASE_XLS_FILE_PATH+"teams.xlsx";
    	FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
    	XSSFSheet worksheet = workbook.getSheetAt(0);
        for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
        	XSSFRow row = worksheet.getRow(i);
        	//logger.debug("ID:"+row.getCell(0)+" Ruolo:"+row.getCell(1)+" Nme:"+row.getCell(2)+" Squadra:"+row.getCell(3));
        	Team tmpTeam = new Team();
        	tmpTeam.setUsername(row.getCell(1).getStringCellValue());
        	tmpTeam.setTeamname(row.getCell(2).getStringCellValue());
        	tmpTeam.setPresident(row.getCell(3).getStringCellValue());
        	Double bdgt = row.getCell(4).getNumericCellValue();
        	tmpTeam.setBudget(bdgt.intValue());
        	tmpTeam.setRole(row.getCell(5).getStringCellValue());
        	Double foglio = row.getCell(6).getNumericCellValue();
        	mapTeamPlayersExcelDatatoDB(tmpTeam, workbook, foglio.intValue());
        	teamsList.add(tmpTeam);
        }

        teamRepository.deleteAll();
        List<Team> savedTeamsList = teamRepository.saveAll(teamsList);
        return savedTeamsList.size();
    }


    private void mapTeamPlayersExcelDatatoDB(Team currentTeam, XSSFWorkbook workbook, int sh) throws IOException {
    	List<Player> gkList = new ArrayList<Player>();
    	List<Player> dfList = new ArrayList<Player>();
    	List<Player> mfList = new ArrayList<Player>();
    	List<Player> fwList = new ArrayList<Player>();
    	XSSFSheet worksheet = workbook.getSheetAt(sh);
        for(int i=1;i<worksheet.getPhysicalNumberOfRows() ;i++) {
        	XSSFRow row = worksheet.getRow(i);
        	Player tmpPlayer = new Player();
        	String playerRole = row.getCell(1).getStringCellValue();
        	tmpPlayer.setRole(playerRole);
        	tmpPlayer.setName(row.getCell(2).getStringCellValue());
        	tmpPlayer.setRealTeam(row.getCell(3).getStringCellValue());
        	tmpPlayer.setTeam(currentTeam.getTeamname());
        	Double price = row.getCell(4).getNumericCellValue();
        	tmpPlayer.setValue(price.intValue());
    		switch (playerRole) {
			case "P":
				gkList.add(tmpPlayer);
				break;
			case "D":
				dfList.add(tmpPlayer);
				break;
			case "C":
				mfList.add(tmpPlayer);
				break;
			case "A":
				fwList.add(tmpPlayer);
				break;

			default:
				break;
			}
        }
        currentTeam.setGoalkeepers(gkList);
        currentTeam.setDefenders(dfList);
        currentTeam.setMidfielders(mfList);
        currentTeam.setForwards(fwList);

    }
*/
    
}
