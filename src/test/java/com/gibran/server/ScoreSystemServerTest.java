package com.gibran.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gibran.server.ScoreSystemHttpServer;

/**
 * 
 * Some integration tests to check that the server is working accordingly. Unfinished for lack of time.
 * 
 * @author Gibran
 *
 */
// TODO mock the server or starting it and test? If starting, test will fail in environments where
// there is another server running in the same address and port, and also has the penalty of the 
// starting and stopping time
public class ScoreSystemServerTest {
	ScoreSystemHttpServer server;

	@Before
	public void setUp() throws IOException {
		server = new ScoreSystemHttpServer();
		server.startServer();
	}
	
	public void shouldStartServer() {
		
	}
	
	public void shouldReturnAValidTokenForUpTo10MinByGetRequest() throws UnknownHostException, IOException {

		URL conn = new URL("http://localhost:8000/4711/login");
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.openStream()));
		String content;
		while (null != (content = in.readLine())) {
			System.out.println(content);
		}
	}
	
	@After
	public void destruct() {
		server.stopServer();
	}

}
