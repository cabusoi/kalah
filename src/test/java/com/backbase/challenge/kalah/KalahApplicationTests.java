package com.backbase.challenge.kalah;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
//@SpringBootTest
//(
//			webEnvironment=SpringBootTest.WebEnvironment.MOCK,
//			classes = KalahApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
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
	public void testAllowDropStoneInPit(){
		Game game =new Game();
		assertThat(game.phase,is(GamePhase.SOUTH_MOVES));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 6));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 7));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 8));
		assertFalse(gameController.allowDropStoneInPit(game.phase, 14));

		game.phase=GamePhase.NORTH_MOVES;
		assertThat(game.phase,is(GamePhase.NORTH_MOVES));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 6));
		assertFalse(gameController.allowDropStoneInPit(game.phase, 7));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 8));
		assertTrue(gameController.allowDropStoneInPit(game.phase, 14));
		
		try{
			gameController.allowDropStoneInPit(game.phase, 16);
			fail("incorrect pit");
		}catch(RuntimeException ex){
			
		}
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

		//test POST to inexistent game expect 2
		this.mvc.perform(post("/games")).andExpect(status().is(200))
				.andExpect(content().string("{\"id\":1,\"url\":\"http://localhost:8081/games/1\"}"));
		
		//test PUT to inexistent game expect 404
		this.mvc.perform(put("/games")).andExpect(status().is(404));
		
		
	}
}
