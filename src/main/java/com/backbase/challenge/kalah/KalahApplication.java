package com.backbase.challenge.kalah;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KalahApplication {

	public static void main(String[] args) {
		SpringApplication.run(KalahApplication.class, args);
	}
}

@RestController
class KalahController {

	@Autowired
	GameRepository gameRepository;

	@GetMapping(consumes = { "application/json" }, path = { "games/{gameId}" })
	public HttpEntity<Object> readGame(HttpServletRequest request,
			@PathVariable("gameId") @NonNull long gameId) throws URISyntaxException {
		Optional<Game> theGame = gameRepository.findById(gameId);
		if (!theGame.isPresent()) {
			return httpResponse("Game id not found " + gameId, HttpStatus.NOT_FOUND);
		}
		
		Game game = theGame.get();

		Map<String, Object> result = prepareResult(request, game);
		result.put("status", status(game));
		result.put("to move", game.phase);
		return httpResponse(result, HttpStatus.OK);
	}

	@PostMapping(consumes = { "application/json" }, path = { "games" })
	public HttpEntity<Object> createGame(HttpServletRequest request) throws URISyntaxException {
		Game game = new Game();
		gameRepository.saveAndFlush(game);
		
		Map<String, Object> result = prepareResult(request, game);
		return httpResponse(result, HttpStatus.CREATED);
	}

	@PutMapping(consumes = { "application/json" }, path = { "games/{gameId}/pits/{pit}" })
	public HttpEntity<Object> move(HttpServletRequest request, 
			@PathVariable("gameId") @NonNull long gameId,
			@PathVariable("pit") @NonNull @Min(1) @Max(14) int pit) throws URISyntaxException {
		Optional<Game> theGame = gameRepository.findById(gameId);
		if (!theGame.isPresent()) {
			return httpResponse("Game id not found " + gameId, HttpStatus.NOT_FOUND);
		}
		
		Game game = theGame.get();
		moveAndPassTurn(game,pit);
		gameRepository.saveAndFlush(game);
		
		Map<String, Object> result = prepareResult(request, game);
		result.put("status", status(game));
		return httpResponse(result, HttpStatus.OK);
	}

	//returns true if next move will be done by the other player
	void moveAndPassTurn(Game game, int pit) {
		if(pit<1||pit>14)
			throw new RuntimeException("Incorrect pit is out of bounds");
		if(pit==7||pit==14)
			throw new RuntimeException("pit is a kalah/pits 7 or 14 are not allowed");
		if (game.phase==GamePhase.SOUTH_MOVES && !(1<=pit&&pit<=6))
			throw new RuntimeException("Incorrect pit given for first player moving");
		if (game.phase==GamePhase.NORTH_MOVES && !(8<=pit&&pit<=13))
			throw new RuntimeException("Incorrect pit given for second player moving");

		int pitStonesToDistribute=getPitCount(game, pit);
		if(!(pitStonesToDistribute>0))
			throw new RuntimeException("Invalid number or pieces "+pitStonesToDistribute+" in pit "+pit);
		setPitCount(game, pit, 0);
/*
		REQ 0) The player who begins picks up all the stones in any of their own pits, and sows the stones on to the right, one in
		each of the following pits, including his own Kalah. 
		
		REQ 1) No stones are put in the opponent's' Kalah. 
		REQ 2) If the players last
		stone lands in his own Kalah, he gets another turn. This can be repeated any number of times before it's the other
		player's turn.
		REQ 3) When the last stone lands in an own empty pit, the player captures this stone and all stones in the opposite pit (the
		other players' pit) and puts them in his own Kalah.
		REQ 4) The game is over as soon as one of the sides run out of stones. The player who still has stones in his/her pits keeps
		them and puts them in his/hers Kalah. The winner of the game is the player who has the most stones in his Kalah.
*/
		while (pitStonesToDistribute>0){ // REQ 0) 
			pit = ++pit % 14;
			
			if( isOpposingKalah(game,pit) )
				continue; //REQ 1) 
			
			int stonesInCurrentPit=getPitCount(game, pit);
			addToPit(game,pit,1);
			pitStonesToDistribute--;
			
			if(pitStonesToDistribute > 0 )
				continue;
			
			if(isOwnKalah(game,pit))//REQ 2)
				return; 
			
			if (stonesInCurrentPit==0 && isOwnPit(game, pit)){//REQ 3)
				int oppositePitCountCapture=getPitCount(game, oppositePit(pit));
				setPitCount(game, oppositePit(pit),0);
				int ownKalah=getOwnKalah(game);
				addToPit(game, ownKalah, oppositePitCountCapture);
			}

			if(game.phase==GamePhase.SOUTH_MOVES){
				game.phase=GamePhase.NORTH_MOVES;
			}else if(game.phase==GamePhase.NORTH_MOVES){
				game.phase=GamePhase.SOUTH_MOVES;
			}
		}
	}
	
	boolean isOwnPit(Game game, int pit) {
		return game.phase==GamePhase.SOUTH_MOVES && 1<=pit && pit<=6 
			|| game.phase==GamePhase.NORTH_MOVES && 8<=pit && pit<=13;
	}

	void addToPit(Game game, int pit, int c) {
		int stonesInPit=getPitCount( game,  pit);
		setPitCount( game,  pit, stonesInPit + c);
	}

	int getPitCount(Game game, int pit) {
		return game.status.get(pit-1);
	}

	void setPitCount(Game game, int pit,int c) {
		game.status.set(pit-1,c);
	}

	boolean isOpposingKalah(Game game, int pit) {
		return (game.phase==GamePhase.SOUTH_MOVES && pit==14)
				||(game.phase==GamePhase.NORTH_MOVES && pit==7);
	}
	
	int getOpposingKalah(Game game){
		if (game.phase==GamePhase.SOUTH_MOVES)
			return 14;
		if (game.phase==GamePhase.NORTH_MOVES)
			return 7;
		throw new RuntimeException("Invalid Kalah due to Game phase: "+game.phase);
	}

	int getOwnKalah(Game game){
		if (game.phase==GamePhase.SOUTH_MOVES)
			return 7;
		if (game.phase==GamePhase.NORTH_MOVES)
			return 14;
		throw new RuntimeException("Invalid Kalah due to Game phase: "+game.phase);
	}

	
	boolean isOwnKalah(Game game, int pit) {
		return (game.phase==GamePhase.SOUTH_MOVES && pit==7)
				||(game.phase==GamePhase.NORTH_MOVES && pit==14);
	}

	boolean allowDropStoneInPit(GamePhase phase , int pit){
		if(pit<1||pit>14)
			throw new RuntimeException("Incorrect pit is out of bounds");
		return (phase==GamePhase.SOUTH_MOVES && pit!=14)
			|| (phase==GamePhase.NORTH_MOVES && pit!=7);	
	}
	
	int oppositePit(int pit){
		if(pit==7||pit==14)
			throw new RuntimeException("pit is a kalah/pits 7 or 14 are not allowed");
		return 14-pit%14;
	}

	private HttpEntity<Object> httpResponse(Object result, HttpStatus httpStatus) {
		return new ResponseEntity<Object>(result, HttpStatus.OK);
	}

	private Map<String, Object> prepareResult(HttpServletRequest request, Game game) throws URISyntaxException {
		Map<String, Object> result = new HashMap<>();
		result.put("id", game.getId());
		result.put("url", link(request, game));
		return result;
	}

	private URI link(HttpServletRequest request, Game game) throws URISyntaxException {
		StringBuffer urlBuffer = new StringBuffer();
		String[] splitUri = request.getRequestURI().split("/");
		urlBuffer.append(request.getScheme()).append("://").append(request.getLocalName()).append(":")
				.append(request.getLocalPort()).append("/").append(splitUri[1]).append("/")
				.append(game.getId().toString());
		URI uri = new URI(urlBuffer.toString());
		return uri;
	}

	private Object status(Game game) {
		SortedMap<Integer, String> result = new TreeMap<>();
		for (int i = 0; i < game.status.size(); i++) {
			result.put(i + 1, game.status.get(i).toString());
		}
		return result;
	}

}

@Repository
interface GameRepository extends JpaRepository<Game, Long> {
}

enum GamePhase {
	SOUTH_MOVES("pits 1..6"),
	NORTH_MOVES("pits 8..13"),
	ENDED("all pits empty for one of players");
	
	private String state;

	private GamePhase(String state){
		this.state=state;
	}
}

@Entity
class Game {

	@Value("${start.stones}")
	private static int C = 6;

	public static final List<Integer> INITIAL_STATE = Arrays.asList(C, C, C, C, C, C, 0, C, C, C, C, C, C, 0);

	@Id
	@GeneratedValue
	private Long id;

	@Convert(converter = GameStatusConverter.class)
	ArrayList<Integer> status;
	
	GamePhase phase;

	public Game() {
		status = new ArrayList<>(INITIAL_STATE);
		phase=GamePhase.SOUTH_MOVES;
	}

	public Long getId() {
		return id;
	}

}

class GameStatusConverter implements AttributeConverter<List<Integer>, String> {
	@Override
	public String convertToDatabaseColumn(List<Integer> status) {
		StringBuffer result = new StringBuffer();
		status.stream().forEach(s -> result.append(s.toString()).append(","));
		return result.toString();
	}

	@Override
	public List<Integer> convertToEntityAttribute(String status) {
		List<Integer> result = new ArrayList<>();
		Arrays.asList(status.split(",")).stream().forEach(s -> result.add(Integer.valueOf(s)));
		;
		return result;
	}
}
