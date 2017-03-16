package com.gibran.domains;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Players and scores for each, encapsulated to not have to create a map of maps.
 * For the moment I am just returning the list and doing all the operations on top of it 
 * from the caller class. 
 * 
 * @author Gibran
 *
 */
public class PlayersScores {

	/*
	 *   For these properties an average of concurrency should be analized to take a decision.
	 *	 They are initialCapacity, loadFactor and concurrencyLevel.
	 *	 Since the ConcurrentHashMap doesn't block, it doesn't mean that this 
	 *	 number will be 1 for each thread. For now using these values.
	 *
	 */
	private final Map <Integer, Integer> playersScores = new ConcurrentHashMap<>(50, 0.9f, 100);
	
	public Map<Integer, Integer> getPlayerScores () {
		return this.playersScores;
	}
}
