package com.gibran.domains;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.gibran.exceptions.ExpiredTokenException;
import com.gibran.exceptions.InexistentTokenException;
import com.gibran.exceptions.NullTokenException;

/**
 * 
 * The logic system, to create and validate tokens
 * 
 * @author Gibran
 *
 */
public class LoginSystem {

	private final Map<String, Integer> sessions = new ConcurrentHashMap<>(15, 1.1f, 100);
	private final long tokenValidityInMinutes;
	private final Object cleanerLock = new Object();
	private final Object loginCreatorLock = new Object ();
	private final ExecutorService executorService;
	
	public LoginSystem (long tokenValidityInMinutes, ExecutorService executorService) {
		this.tokenValidityInMinutes = tokenValidityInMinutes;
		this.executorService = executorService;
		// Create a new thread to run each 1 minute to clean expired tokens
		Runnable cleaner = () -> {
			while (true) {
				this.cleanExpiredSessions(LocalDateTime.now());
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		executorService.submit(cleaner);
	}

	// Just used a regular LocalDateTime + playerId concatenation for the token, since it will most probably
	// be unique and also the application will have a copy of it stored to authenticate so less chance of 
	// frauds
	public String findOrCreateToken(Integer playerId, LocalDateTime currentTime)  {
		// Synchronized, in the case more than one incoming call at the same time, will create the login on one
		// and return on the other
		synchronized (loginCreatorLock) {
			if (sessions.containsValue(playerId)) {
				String existentToken = sessions.entrySet().parallelStream().filter(entry -> entry.getValue().equals(playerId))
						.findFirst()
						.get()
						.getKey();
				if (Duration.between(LocalDateTime.parse(parseDateFromToken(existentToken)), currentTime).toMinutes() < tokenValidityInMinutes) {
					return sessions.entrySet().parallelStream().filter(entry -> entry.getValue().equals(playerId))
																												.findFirst()
																												.get()
																												.getKey();
				}
			}
		}	
		StringBuilder token = new StringBuilder(); 
		token.append(LocalDateTime.now().toString());
		token.append(playerId.toString());
		
		// Always trying to use atomic methods
		sessions.putIfAbsent(token.toString(), playerId);
		return token.toString();
	}

	// injecting current time for testability
	private boolean validateTokenLogin(String token, LocalDateTime currentTime)
			throws InexistentTokenException, ExpiredTokenException, NullTokenException{
		if (null == token) {
			throw new NullTokenException();
		} else if (null == sessions.get(token) || token.length() < LocalDateTime.now().toString().length()) {
			throw new InexistentTokenException();
		} else if (Duration.between(LocalDateTime.parse(parseDateFromToken(token)), currentTime).toMinutes() > tokenValidityInMinutes) {
			sessions.remove(token, parsePlayerIdFromToken(token));
			throw new ExpiredTokenException();
		}
		return true;
	}

	// injecting current time for testability
	public Integer getTokenLogin(String token, LocalDateTime currentTime)
			throws InexistentTokenException, ExpiredTokenException, NullTokenException {
		validateTokenLogin(token, currentTime);
		return sessions.get(token);
	}
	
	private Integer parsePlayerIdFromToken (String token) throws InexistentTokenException  {
		try {
			return Integer.valueOf(token.substring(LocalDateTime.now().toString().length() ));
		} catch (NumberFormatException e) {
			throw new InexistentTokenException();
		}
	}
	
	private String parseDateFromToken (String token) {
		return token.substring(0, LocalDateTime.now().toString().length());
	}
	
	
	// TODO create tests for this
	private void cleanExpiredSessions (LocalDateTime currentTime) {
		synchronized (cleanerLock) {
			sessions.entrySet().removeIf(session -> Duration.between(LocalDateTime.parse(session.getKey()), currentTime).toMinutes() > tokenValidityInMinutes);
		}
	}
}
