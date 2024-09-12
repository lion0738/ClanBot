package kr.panda.bot.utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ScheduledExecutorService;

import com.sun.net.httpserver.HttpServer;

public class WebServer {
	private static HttpServer mServer;

	public static void initialize(ScheduledExecutorService worker) {
		int port = Integer.parseInt(System.getenv("PORT"));
		InetSocketAddress addr = new InetSocketAddress(port);
		try {
			mServer = HttpServer.create(addr, 0);
			//mServer.createContext("/register", (exchange) -> BungieAPIHelper.register(exchange));
			//mServer.createContext("/verifyUser", (exchange) -> DiscordAPIHelper.verifyUser(exchange));
			mServer.setExecutor(worker);
			mServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
