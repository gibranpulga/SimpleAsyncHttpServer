package com.gibran.domains;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import com.gibran.domains.HighScoreList;

public class HighScoreListSynchronousTest {
	HighScoreList highScoreList;
	String token;
	final static Integer LEVEL_1 = 1;
	
	@Before
	public void setUp() {
		highScoreList = new HighScoreList(Executors.newCachedThreadPool());	
		token = "";
	}

	@Test
	public void shouldReturnEmptyStringForEmptyHighScoreList() {
		String expectedResult = "";
		assertEquals(expectedResult, highScoreList.getHighScoreListForLevelInCsv(1));		
	}
	
	@Test
	public void shouldReturnCorrectHighscoreForPostedHighscore () throws InterruptedException {
		highScoreList.addPlayerScoreForLevelSynchronously(1, 1000, 1);
		String expectedResult = "1=1000";
		assertEquals(expectedResult, highScoreList.getHighScoreListForLevelInCsv(1));
	}
	
	@Test
	public void shouldSubstituteOldPlayerHighscoreForNewIfHigher () throws InterruptedException {
		highScoreList.addPlayerScoreForLevelSynchronously(1, 1000, 1);
		highScoreList.addPlayerScoreForLevelSynchronously(1, 1500, 1);
		String expectedResult = "1=1500";
		assertEquals(expectedResult, highScoreList.getHighScoreListForLevelInCsv(1));
	}
	
	@Test
	public void shouldNotSubstituteOldPlayerHighscoreForNewIfLower () throws InterruptedException {
		highScoreList.addPlayerScoreForLevelSynchronously(1, 1500, 1);
		highScoreList.addPlayerScoreForLevelSynchronously(1, 1000, 1);
		String expectedResult = "1=1500";
		assertEquals(expectedResult, highScoreList.getHighScoreListForLevelInCsv(1));
		
	}
	
	@Test
	public void shouldKeepOnlyHigher15Highscores () throws InterruptedException {
		for (int i = 0; i < 30; i++) {
			highScoreList.addPlayerScoreForLevelSynchronously(i, 100 * i, 1);	
		}
		String expectedResult = "29=2900,28=2800,27=2700,26=2600,25=2500,24=2400,23=2300,22=2200,21=2100,20=2000,19=1900,18=1800,17=1700,16=1600,15=1500";
		assertEquals(expectedResult, highScoreList.getHighScoreListForLevelInCsv(1));
	}
	
	/*
	 *  Logic behind this stress test is to try to simulate concurrent connections to check if there is no race
	 *  condition. I create a CountDownLatch so the main thread will not advance while the other threads don't
	 *  finish their jobs and also a CyclicBarrier to try to start all the jobs at the same time, once they are
	 *  ready, this way I can try to emulate some concurrency.
	 */
	@Test
	public void multiThreadedConcurrencyStressTest () throws InterruptedException, BrokenBarrierException {
		final CyclicBarrier barrier = new CyclicBarrier(101);
		final CountDownLatch latch = new CountDownLatch(1);
		for (int i = 0; i < 100; i++) {
			new Thread(new HighScoreRunnable(highScoreList, i, latch, barrier, false)).start();
		}
		latch.countDown();
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		assertEquals("99=9900,98=9800,97=9700,96=9600,95=9500,94=9400,93=9300,92=9200,91=9100,90=9000,89=8900,88=8800,87=8700,86=8600,85=8500", highScoreList.getHighScoreListForLevelInCsv(LEVEL_1));
	}
		
}

