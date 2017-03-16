package com.gibran.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

import com.gibran.domains.HighScoreList;
import com.gibran.domains.LoginSystem;
import com.gibran.exceptions.ExpiredTokenException;
import com.gibran.exceptions.InexistentTokenException;
import com.gibran.exceptions.NullTokenException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * 
 * The handler, routes the action according to the url path and request method
 * 
 * @author Gibran
 *
 */
@SuppressWarnings("restriction")
public class ScoreSystemHandler implements HttpHandler {
	private final HighScoreList highScoreList;
	private final RequestDataParser requestDataParser = new RequestDataParser();
	private final ExecutorService executorService;
	
	private final Integer TOKENS_VALIDITY_TIME_IN_MINUTES = 10; 
	private final LoginSystem loginSystem;
	
	public ScoreSystemHandler (ExecutorService executorService) {
		this.executorService = executorService;
		this.highScoreList = new HighScoreList(executorService);
		loginSystem = new LoginSystem(TOKENS_VALIDITY_TIME_IN_MINUTES, executorService);
	}
	
	public void handle(HttpExchange httpExchange) throws IOException {
		ResponseData responseData;
		
		if (httpExchange.getRequestMethod().equals("GET")) {
			responseData = this.processGet(httpExchange);
		} else if (httpExchange.getRequestMethod().equals("POST")) {
			responseData = this.processPost(httpExchange);
		} else {
			responseData = new ResponseData(400, "Unsupported method");
		}
		
		// TODO is this being closed right?
		OutputStream os = httpExchange.getResponseBody();
		try {
			httpExchange.sendResponseHeaders(responseData.getStatusCode(), responseData.getContent().length());
			os.write(responseData.getContent().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			os.flush();
			os.close();
		}
	}
	
	private ResponseData processGet(HttpExchange httpExchange) {
		URI requestURI = httpExchange.getRequestURI();
		
		// TODO check correct path with regular expressions
		// http://localhost:8000/4714/login
		if (requestURI.getPath().contains("login")) {
			return this.getLogin(httpExchange);
		// http://localhost:8000/1/highscorelist
		} else if (requestURI.getPath().contains("highscorelist")) {
			return this.getHighscoreList(httpExchange);
		}
		
		return new ResponseData(400, "Unsupported path");

	}
	
	private ResponseData getLogin (HttpExchange httpExchange) {
		URI requestURI = httpExchange.getRequestURI();
		try {
			return new ResponseData (200, 
					this.loginSystem.findOrCreateToken(Integer.valueOf(requestDataParser.getPlayerIdFromLoginGetRequest(requestURI.getPath())), LocalDateTime.now()));
		} catch (NumberFormatException e) {
			return new ResponseData(400, "Invalid player Id (must be a number).");
		}
	}
	
	private ResponseData getHighscoreList (HttpExchange httpExchange) {
		URI requestURI = httpExchange.getRequestURI();
		try {
			return new ResponseData(200, 
				this.highScoreList.getHighScoreListForLevelInCsv(Integer.valueOf(requestDataParser.getLevelFromHighscoreListGetRequest(requestURI.getPath()))));
		} catch (NumberFormatException e) {
			return new ResponseData(400, "Invalid level (must be a number).");
		}
	}

	private ResponseData processPost(HttpExchange httpExchange) {
		// TODO check correct path with regular expressions
		// http://localhost:8081/2/score?sessionkey=UICSNDK
		if (httpExchange.getRequestURI().getPath().contains("score")) {
			return this.postUserScore(httpExchange);
		}
		
		return new ResponseData(400, "Unsupported path");
	}

	private ResponseData postUserScore (HttpExchange httpExchange) {
		String token = requestDataParser.getSessionKeyFromUserScorePostRequest(httpExchange.getRequestURI().getQuery());
		// As recommended by Uncle Bob in Clean Code, catching exceptions instead of error codes
		Integer postedPlayer;
		Integer postedLevel;
		Integer postedScore;
		try {
			postedPlayer = this.loginSystem.getTokenLogin(token, LocalDateTime.now());
			postedLevel = Integer.parseInt(requestDataParser.getLevelFromUserScorePostRequest(httpExchange.getRequestURI().getPath()));
			postedScore = Integer.valueOf(requestDataParser.getRequestBodyContent(httpExchange));

			this.highScoreList.addPlayerScoreForLevelAsynchronously(postedPlayer, postedScore, postedLevel);
			return new ResponseData(200, "");
		} catch (NumberFormatException e) {
			return new ResponseData(400, "Invalid player Id, level or score (must be numbers).");
		} catch (UnsupportedEncodingException e) {
			return new ResponseData(400, "Internal error.");
		} catch (InexistentTokenException e) {
			return new ResponseData(400, "Inexistent token.");
		} catch (NullTokenException e) {
			return new ResponseData(400, "Token cannot be empty.");
		} catch (ExpiredTokenException e) {
			return new ResponseData(400, "Expired token.");
		} 
	}
}
