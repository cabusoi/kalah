package com.backbase.challenge.kalah;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@RunWith(SpringRunner.class)
//@SpringBootTest
//(
//			webEnvironment=SpringBootTest.WebEnvironment.MOCK,
//			classes = KalahApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableWebMvc
public class KalahApplicationTests {

	public GameStatusConverter converter(){
		return new GameStatusConverter();
	}
	
	@Autowired
	KalahController gameController;
	
	@Autowired
	private MockMvc mvc;
	
	@Before
	public void contextLoads() {
		assertNotNull(gameController);
	}
	
	@Test
	public void testOpposingKalah(){
		Game game =new Game();
		
		assertThat(game.phase, is(GamePhase.SOUTH_MOVES));
		assertThat(gameController.isOpposingKalah(game, 7), is(false));
		assertThat(gameController.isOpposingKalah(game, 8), is(false));
		assertThat(gameController.isOpposingKalah(game, 14), is(true));
		
		game.phase=GamePhase.NORTH_MOVES;
		assertThat(gameController.isOpposingKalah(game, 7), is(true));
		assertThat(gameController.isOpposingKalah(game, 8), is(false));
		assertThat(gameController.isOpposingKalah(game, 14), is(false));
	}
	
	@Test
	public void testOwnPit(){
		Game game =new Game();
		
		assertThat(game.phase, is(GamePhase.SOUTH_MOVES));
		assertThat(gameController.isOwnPit( game, 0), is(false));
		assertThat(gameController.isOwnPit( game, 1), is(true));
		assertThat(gameController.isOwnPit( game, 2), is(true));
		assertThat(gameController.isOwnPit( game, 3), is(true));
		assertThat(gameController.isOwnPit( game, 4), is(true));
		assertThat(gameController.isOwnPit( game, 5), is(true));
		assertThat(gameController.isOwnPit( game, 6), is(true));
		assertThat(gameController.isOwnPit( game, 7), is(false));
		assertThat(gameController.isOwnPit( game, 8), is(false));
		assertThat(gameController.isOwnPit( game, 9), is(false));
		assertThat(gameController.isOwnPit( game, 10), is(false));
		assertThat(gameController.isOwnPit( game, 11), is(false));
		assertThat(gameController.isOwnPit( game, 12), is(false));
		assertThat(gameController.isOwnPit( game, 13), is(false));
		assertThat(gameController.isOwnPit( game, 14), is(false));
		assertThat(gameController.isOwnPit( game, 15), is(false));

		game.phase=GamePhase.NORTH_MOVES;
		assertThat(gameController.isOwnPit( game, 0), is(false));
		assertThat(gameController.isOwnPit( game, 1), is(false));
		assertThat(gameController.isOwnPit( game, 2), is(false));
		assertThat(gameController.isOwnPit( game, 3), is(false));
		assertThat(gameController.isOwnPit( game, 4), is(false));
		assertThat(gameController.isOwnPit( game, 5), is(false));
		assertThat(gameController.isOwnPit( game, 6), is(false));
		assertThat(gameController.isOwnPit( game, 7), is(false));
		assertThat(gameController.isOwnPit( game, 8), is(true));
		assertThat(gameController.isOwnPit( game, 9), is(true));
		assertThat(gameController.isOwnPit( game, 10), is(true));
		assertThat(gameController.isOwnPit( game, 11), is(true));
		assertThat(gameController.isOwnPit( game, 12), is(true));
		assertThat(gameController.isOwnPit( game, 13), is(true));
		assertThat(gameController.isOwnPit( game, 14), is(false));
		assertThat(gameController.isOwnPit( game, 15), is(false));
	}
	
	@Test
	public void testConverter(){
		Game game =new Game();
		System.out.println(game.status);

		String convertToDatabaseColumn = converter().convertToDatabaseColumn(game.status);
		System.out.println(convertToDatabaseColumn);
		
		List<Integer> convertToEntityAttribute = converter().convertToEntityAttribute(convertToDatabaseColumn);
		System.out.println(convertToEntityAttribute);
		
		assertThat(game.status,equalTo(convertToEntityAttribute));
	}
	
	@Test
	public void testOposites(){
		assertThat(1, is(gameController.oppositePit(13)));
		assertThat(2, is(gameController.oppositePit(12)));
		assertThat(3, is(gameController.oppositePit(11)));
		assertThat(4, is(gameController.oppositePit(10)));
		assertThat(5, is(gameController.oppositePit(9)));
		assertThat(6, is(gameController.oppositePit(8)));
		try {
			assertThat(7, is(gameController.oppositePit(7)));
			fail();
		} catch (Exception e) {
		}
		assertThat(8, is(gameController.oppositePit(6)));
		assertThat(9, is(gameController.oppositePit(5)));
		assertThat(10, is(gameController.oppositePit(4)));
		assertThat(11, is(gameController.oppositePit(3)));
		assertThat(12, is(gameController.oppositePit(2)));
		assertThat(13, is(gameController.oppositePit(1)));
		try {
			assertThat(14, is(gameController.oppositePit(7)));
			fail();
		} catch (Exception e) {
		}
	}


	@Test
	public void testMoveAndPassControl() throws Exception {

		//test GET to inexistent game expect 404
		System.out.println("\n"+"get /games/-1");
		this.mvc.perform( get("/games/-1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
		
		//test PUT to inexistent game expect 404
		System.out.println("\n"+"put /games/-1/pits/-1");
		this.mvc.perform( put("/games/-1/pits/-1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8));
		
		//test POST to inexistent game expect 201
		System.out.println("\n"+"post /games");
		this.mvc.perform(post("/games").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().string("{\"id\":1,\"url\":\"http://localhost:80/games/1\"}"));
		
		//test PUT to have SOUTH_MOVES their pit number 1 expect 200
		System.out.println("\n"+"put /games/1/pits/1");
		MvcResult result = this.mvc.perform( put("/games/1/pits/1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().json("{'id':1,'url':'http://localhost:80/games/1','status':{'1':'0','2':'7','3':'7','4':'7','5':'7','6':'7','7':'1','8':'6','9':'6','10':'6','11':'6','12':'6','13':'6','14':'0'}}"))
	            .andReturn();
		
		//test GET game 1 expect 200 and expect SOUTH_MOVES as result of REQ 2) after the move above
		System.out.println("\n"+"get /games/1");
		this.mvc.perform( get("/games/1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().string( containsString("SOUTH_MOVES")));

		//test PUT to have SOUTH_MOVES again , this time their pit number 2 expect 200
		System.out.println("\n"+"put /games/1/pits/2");
		result = this.mvc.perform( put("/games/1/pits/2").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().json("{'id':1,'url':'http://localhost:80/games/1','status':{'1':'0','2':'0','3':'8','4':'8','5':'8','6':'8','7':'2','8':'7','9':'7','10':'6','11':'6','12':'6','13':'6','14':'0'}}"))
	            .andReturn();
				
		//test GET game 1 expect 200 and expect NORTH_MOVES 
		System.out.println("\n"+"get /games/1");
		this.mvc.perform( get("/games/1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().string( containsString("NORTH_MOVES")));

		//test PUT to have NORTH_MOVES, their pit number 8 expect 200
		System.out.println("\n"+"put /games/1/pits/8");
		result = this.mvc.perform( put("/games/1/pits/8").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().json("{'id':1,'url':'http://localhost:80/games/1','status':{'1':'1','2':'0','3':'8','4':'8','5':'8','6':'8','7':'2','9':'8','9':'8','10':'7','11':'7','12':'7','13':'7','14':'1'}}"))
	            .andReturn();
		
		//test GET game 1 expect 200 and expect SOUTH_MOVES 
		System.out.println("\n"+"get /games/1");
		this.mvc.perform( get("/games/1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().string( containsString("SOUTH_MOVES")));

		//test PUT to have SOUTH_MOVES, their pit number 6 and verify REQ 3) expect 200
		System.out.println("\n"+"put /games/1/pits/6");
		result = this.mvc.perform( put("/games/1/pits/6").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(content().json("{'id':1,'url':'http://localhost:80/games/1','status':{'1':'2','2':'0','3':'8','4':'8','5':'8','6':'0','7':'3','9':'9','9':'9','10':'8','11':'8','12':'8','13':'8','14':'1'}}"))
	            .andReturn();
		
		//do some random moves
		for (int i=0;i<500;i++){
			try {
				System.out.print("Extra move "+i+" ");
				 this.mvc.perform( put("/games/1/pits/"+i%15).accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		//test GET game 1 expect 200  
		System.out.println("\n"+"get /games/1");
		result = this.mvc.perform( get("/games/1").accept(MediaType.APPLICATION_JSON_UTF8).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		
		System.out.println(result);
	}
}
