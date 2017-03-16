package com.gibran.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * Small client I created to try to simulate some concurrent connections and check how much
 * can the server hold up to. Implementation could be improved, but I got up to almost 5000
 * concurrent connections before getting "Too many files" error, which might be caused
 * by my SO limitation, however I got a lot of java.lang.Thread.State: TIMED_WAITING (parking)
 * in the server output. The scores are posted correctly though.
 * 
 * @author Gibran
 *
 */
public class TestClient {

	ExecutorService executorService = Executors.newCachedThreadPool();

	public static void main(String[] args) throws IOException {
		new TestClient().simulateConcurrency();
	}

	public void simulateConcurrency() throws IOException {
		URL conn = new URL("http://localhost:8000/4711/login");
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.openStream()));

		String content = new String();
		StringBuilder token = new StringBuilder();
		while (null != (content = in.readLine())) {
			System.out.println(content);
			token.append(content);
		}

		final CyclicBarrier barrier = new CyclicBarrier(4001);
		for (int i = 0; i < 4000; i++) {
			executorService.submit(new ServerConnectorRunnable(barrier, token.toString(), i));
		}

		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}

		executorService.shutdown();

	}
}

class ServerConnectorRunnable implements Runnable {
	private final CyclicBarrier barrier;
	private final String token;
	int index;

	public ServerConnectorRunnable(CyclicBarrier barrier, String token, int index) {
		this.barrier = barrier;
		this.token = token;
		this.index = index;
	}

	@Override
	public void run() {
		try {
			System.out.println("Starting thread  " + Thread.currentThread().getName());

			
			HttpURLConnection conn = (HttpURLConnection) new URL("http://localhost:8000/1/score?sessionkey=" + token)
					.openConnection();
			// add score
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				e.printStackTrace();
			}
			
			conn.getOutputStream().write(String.valueOf((index + 10)).getBytes());
			conn.getOutputStream().flush();
			conn.getOutputStream().close();
			conn.getResponseCode();
			
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
