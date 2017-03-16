package com.gibran.domains;

/**
 * 
 * A class to keep the values of each task sent to the ConcurrentLinkedQueue, to facilitate access to data
 * 
 * @author Gibran
 *
 */
public class PlayerScoreAdditionAsyncTask {
	private final Integer level;
	private final Integer playerId;
	private final Integer score;
	
	public PlayerScoreAdditionAsyncTask(Integer level, Integer playerId, Integer score) {
		this.level = level;
		this.playerId = playerId;
		this.score = score;
	}

	public Integer getLevel() {
		return level;
	}

	public Integer getPlayerId() {
		return playerId;
	}

	public Integer getScore() {
		return score;
	}
	
	
}
