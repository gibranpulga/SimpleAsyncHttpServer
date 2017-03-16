package com.gibran.domains;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

import com.gibran.domains.HighScoreList;

public class HighScoreRunnable implements Runnable  {
	private final HighScoreList highScoreList;
	private final CountDownLatch latch;
	private final int index;
	private final CyclicBarrier barrier;
	private final boolean asynchronous;
	
	public HighScoreRunnable(HighScoreList highScoreList, int index, CountDownLatch latch, CyclicBarrier barrier, boolean asynchronous) {
		this.highScoreList = highScoreList;
		this.index = index;
		this.latch = latch;
		this.barrier = barrier;
		this.asynchronous = asynchronous;
	}
	@Override
	public void run() {
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
		if (asynchronous) {
			highScoreList.addPlayerScoreForLevelAsynchronously(index, 100 * index, HighScoreListSynchronousTest.LEVEL_1);
		} else {
			highScoreList.addPlayerScoreForLevelSynchronously(index, 100 * index, HighScoreListSynchronousTest.LEVEL_1);
		}
		// TODO change this barrier to a latch so the threads won't have to be hanged here, they just need to signal the main that they have completed
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
}
