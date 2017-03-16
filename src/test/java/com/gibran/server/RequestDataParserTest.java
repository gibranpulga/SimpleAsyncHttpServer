package com.gibran.server;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.mockito.Mockito;

import com.gibran.server.RequestDataParser;
import com.sun.net.httpserver.HttpExchange;

public class RequestDataParserTest {
	RequestDataParser requestDataParser = new RequestDataParser();
	
	@Test
	// http://localhost:8081/4711/login --> should return 4711
	public void shouldReturnUserIdForLogin () {
		String url = "/4711/login";
		String expectedUserId = "4711";
		assertEquals(expectedUserId, requestDataParser.getPlayerIdFromLoginGetRequest(url));
	}
	
	@Test
	// http://localhost:8081/2/score?sessionkey=UICSNDK -> should return 2
	public void shouldReturnLevelIdForPostUserScore () {
		String url = "/2/score";
		String expectedLevel = "2";
		assertEquals(expectedLevel, requestDataParser.getLevelFromUserScorePostRequest(url));		
	}
	
	@Test
	// http://localhost:8081/2/score?sessionkey=UICSNDK -> should return UICSNDK
	public void shouldreturnSessionKeyForPostUserScore () {
		String url = "sessionkey=UICSNDK";
		String expectedsessionKey = "UICSNDK";
		assertEquals(expectedsessionKey, requestDataParser.getSessionKeyFromUserScorePostRequest(url));	
	}
	
	@Test
	// http://localhost:8081/2/highscorelist -> should return 2
	public void shouldReturnLevelForHighScoreList() {
		String url = "/2/highscorelist";
		String expectedLevel = "2";
		assertEquals(expectedLevel, requestDataParser.getLevelFromHighscoreListGetRequest(url));
	}
	
	// mocking the request body
	@Test
	public void shouldReturnParsedCorrectScoreFromPostMethodBody () throws IOException {
		String bodyContent = "1500";
		HttpExchange httpExchange = Mockito.mock(HttpExchange.class);
		InputStream content = new ByteArrayInputStream(bodyContent.getBytes(StandardCharsets.UTF_8));
		Mockito.when(httpExchange.getRequestBody()).thenReturn(content);
		assertEquals(bodyContent, requestDataParser.getRequestBodyContent(httpExchange));
		content.close();
		
	}
	
	
	
}
