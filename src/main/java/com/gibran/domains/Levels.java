package com.gibran.domains;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Simple class to hold the levels, encapsulated to not have to create a map of maps.
 * For the moment I am just returning the list and doing all the operations on top of it 
 * from the caller class. 
 * 
 * @author Gibran
 *
 */
public class Levels {

	private final ConcurrentHashMap<Integer, PlayersScores> levelPlayersScores = new ConcurrentHashMap<>(50, 0.9f, 100);
	
	public ConcurrentHashMap<Integer, PlayersScores> getAllLevelsPlayerScores() {
		return this.levelPlayersScores;
	}
	
}
