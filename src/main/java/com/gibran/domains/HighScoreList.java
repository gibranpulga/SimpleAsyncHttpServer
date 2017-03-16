package com.gibran.domains;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

/**
 * 
 * The HighScoreList class, used to add and retrieve the highscores
 * 
 * @author Gibran
 *
 */
public class HighScoreList {
	private final Queue<PlayerScoreAdditionAsyncTask> asyncQueue = new ConcurrentLinkedQueue<>();
	private final Levels levelsHighScores = new Levels();
	private final Object lock = new Object();
	private final ExecutorService executorService;
	
	public HighScoreList (ExecutorService executorService) {
		this.executorService = executorService;
	}
	
	public String getHighScoreListForLevelInCsv (Integer level) {
		if (null == levelsHighScores.getAllLevelsPlayerScores().get(level) 
				|| null == levelsHighScores.getAllLevelsPlayerScores().get(level).getPlayerScores()
				|| levelsHighScores.getAllLevelsPlayerScores().get(level).getPlayerScores().size() == 0) {
			return "";
		}

		StringBuilder highScoreListInCsv = new StringBuilder();
		
		levelsHighScores.getAllLevelsPlayerScores().get(level).getPlayerScores().entrySet().stream()
        																					.sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed()) 
        																					.forEach(entry -> {
        																						highScoreListInCsv.append(entry.getKey() + "=" + entry.getValue() + ",");																					
        																					});
		highScoreListInCsv.deleteCharAt(highScoreListInCsv.length() - 1);
		
		return highScoreListInCsv.toString();
	}
	
	public void addPlayerScoreForLevelSynchronously (Integer playerId, Integer score, Integer level) {
		addPlayerScoreForLevel(playerId, score, level);
	}
	
	/*
	 * Doing the insertion asynchronously, sending the score to a ConcurrentLinkedQueue and having
	 * another thread processing the queue in parallel. This way this method would respond faster
	 *  since it doesn't have to process anything and (doesn't need to return anything)
	 */
	public void addPlayerScoreForLevelAsynchronously (Integer playerId, Integer score, Integer level) {

		asyncQueue.offer(new PlayerScoreAdditionAsyncTask(level, playerId, score));
		processQueue();

	}
	
	private void processQueue () {
		Runnable taskRunnable = () -> {
			PlayerScoreAdditionAsyncTask asyncTask;
			 while ((asyncTask = asyncQueue.poll()) != null) {

				Integer level = asyncTask.getLevel();
				Integer playerId = asyncTask.getPlayerId();
				Integer score = asyncTask.getScore();
				
				addPlayerScoreForLevel(playerId, score, level);
			}
		};
		// TODO create a ExecutorService for these threads (or use the same as the handler)
		this.executorService.submit(taskRunnable);
		//new Thread(taskRunnable).start();
	}
	
	private void addPlayerScoreForLevel (Integer playerId, Integer score, Integer level) {
		// The methods putIfAbsent(), remove() and computeIfPresent() methods from ConcurrentHashMap are atomic
		levelsHighScores.getAllLevelsPlayerScores().putIfAbsent(level, new PlayersScores());
		
		if (null != levelsHighScores.getAllLevelsPlayerScores().get(level)
																		.getPlayerScores()
																		.putIfAbsent(playerId, score) ) {
			levelsHighScores.getAllLevelsPlayerScores().get(level)
			.getPlayerScores()
			.computeIfPresent(playerId, (key, value) -> value = score > value ? score : value);
		}
		synchronized (lock) {
			
			// This needs to be synchronized because size() is not atomic.. if is not synchronized it will return false values
			// and the logic will not work for concurrent insertions
			if (levelsHighScores.getAllLevelsPlayerScores().get(level).getPlayerScores().size() > 15) {
				
				Map.Entry<Integer, Integer> minValue = levelsHighScores.getAllLevelsPlayerScores().get(level)
						.getPlayerScores()
						.entrySet()
						.parallelStream()
						.min(Map.Entry.comparingByValue(Integer::compareTo))
						.get();

				levelsHighScores.getAllLevelsPlayerScores().get(level).getPlayerScores().remove(minValue.getKey(), minValue.getValue());
			}
		}
	}
}
