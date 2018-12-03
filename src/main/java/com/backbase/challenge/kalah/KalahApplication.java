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
import javax.transaction.Transactional;
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
		move(game,pit);
		gameRepository.saveAndFlush(game);
		
		Map<String, Object> result = prepareResult(request, game);
		result.put("status", status(game));
		return httpResponse(result, HttpStatus.OK);
	}

	void move(Game game, int pit) {
		if (game.phase==GamePhase.SOUTH_MOVES && !(1<=pit&&pit<=6)){
			throw new RuntimeException("Incorrect pit given for first player moving");
		}
		if (game.phase==GamePhase.NORTH_MOVES && !(8<=pit&&pit<=13)){
			throw new RuntimeException("Incorrect pit given for second player moving");
		}
		if(pit==7||pit==14){
			throw new RuntimeException("Stones in a kalah/pits 7 or 14 are not allowed to move");
		}
		
	}
	
	boolean allowDropStoneInPit(GamePhase phase , int pit){
		if(pit<1||pit>14){
			throw new RuntimeException("Incorrect pit is out of bounds");
		}
		return (phase==GamePhase.SOUTH_MOVES && pit!=14)
			|| (phase==GamePhase.NORTH_MOVES && pit!=7);	
	}
	
	int oppositePit(int pit){
		if (pit%7==0){
			throw new RuntimeException("Trying to determine opposite pit for a kalah");
		}
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
@Transactional
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
	List<Integer> status;
	
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
