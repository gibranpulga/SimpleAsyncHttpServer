package com.gibran.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * 
 * Creating a server with infinite threads
 * 
 * @author Gibran
 *
 */
public class ScoreSystemHttpServer {
	private HttpServer server;
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	private final HttpHandler controller = new ScoreSystemHandler(executorService);

	public static void main(String[] args) throws Exception {
		new ScoreSystemHttpServer().startServer();
	}
	
	@SuppressWarnings("restriction")
	public ScoreSystemHttpServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/", controller);
		server.setExecutor(this.executorService);
	}

	public void startServer() throws IOException {
		this.server.start();
	}

	public void stopServer() {
		server.stop(0);
	}

}
