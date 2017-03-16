package com.gibran.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * A simple parser for the request data: the path, the parameters and the body
 * 
 * @author Gibran
 *
 */
public class RequestDataParser {
	public String getRequestBodyContent(HttpExchange httpExchange) throws UnsupportedEncodingException {
		return new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.UTF_8)).lines()
				.collect(Collectors.joining("\n"));
	}

	public String getLevelFromHighscoreListGetRequest(String path) {
		return path.replaceAll("\\/", "").substring(0, path.replaceAll("\\/", "").indexOf("highscorelist"));
	}

	public String getLevelFromUserScorePostRequest(String path) {
		return path.replaceAll("\\/", "").substring(0, path.replaceAll("\\/", "").indexOf("score"));
	}

	public String getPlayerIdFromLoginGetRequest(String path) {
		return path.replaceAll("\\/", "").substring(0, path.replaceAll("\\/", "").indexOf("login"));
	}

	public String getSessionKeyFromUserScorePostRequest(String parameters) {
		return parameters.substring("sessionkey=".length());
	}
}
