package kr.panda.bot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import kr.panda.bot.utils.DatabaseHelper;
import kr.panda.bot.utils.Util;

public class ClanWarNotifier {
	private static HttpServer sServer;

	private static List<Map<String, String>> sDBList;
	private static Map<String, List<String>> sNotifyMap;

	public static void initialize(ScheduledExecutorService worker) {
		initializeWebServer(worker);
		loadChannels();
	}

	private static void initializeWebServer(ScheduledExecutorService worker) {
		int port = Integer.parseInt(System.getenv("PORT"));
		InetSocketAddress addr = new InetSocketAddress(port);
		try {
			sServer = HttpServer.create(addr, 0);
			sServer.createContext("/clanWebhook", (exchange) -> retrieveData(exchange));
			sServer.setExecutor(worker);
			sServer.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadChannels() {
		sNotifyMap = new HashMap<>();
		sDBList = DatabaseHelper.getRows("ClanWarNotifyChannel");
		for (Map<String, String> channelMap : sDBList) {
			String channelId = channelMap.get("ChannelId");
			String tagName = channelMap.get("ClanTag");

			List<String> targetList;
			if (sNotifyMap.containsKey(channelId)) {
				targetList = sNotifyMap.get(channelId);
			} else {
				targetList = new ArrayList<>();
				sNotifyMap.put(channelId, targetList);
			}

			targetList.add(tagName);
		}
	}

	public static boolean addServer(String channelId, String tagName) {
		if (sNotifyMap.containsKey(channelId)
				&& sNotifyMap.get(channelId).contains(tagName)) {
			return false;
		}

		Map<String, String> dbMap = new HashMap<>();
		dbMap.put("Id", Util.getRandomHash());
		dbMap.put("ChannelId", channelId);
		dbMap.put("ClanTag", tagName);
		sDBList.add(dbMap);
		DatabaseHelper.addRow("ClanWarNotifyChannel", dbMap);

		List<String> targetList;
		if (sNotifyMap.containsKey(channelId)) {
			targetList = sNotifyMap.get(channelId);
		} else {
			targetList = new ArrayList<>();
			sNotifyMap.put(channelId, targetList);
		}
		targetList.add(tagName);

		return true;
	}

	public static boolean removeServer(String channelId, String tagName) {
		if (sNotifyMap.containsKey(channelId)
				&& sNotifyMap.get(channelId).contains(tagName)) {
			Map<String, String> target = sDBList.stream()
					.filter(map -> channelId.equals(map.get("ChannelId")))
					.filter(map -> tagName.equals(map.get("ClanTag")))
					.findFirst().get();
			sNotifyMap.get(channelId).remove(tagName);
			sDBList.remove(target);
			DatabaseHelper.deleteRow("ClanWarNotifyChannel", "Id", target.get("Id"));
			return true;
		}

		return false;
	}

	private static void retrieveData(HttpExchange exchange) {
		// TODO Auto-generated method stub

		Object object = null;
		if (isDuplicateNotification(object)) {
			notifyChange(null);
		}
	}

	private static boolean isDuplicateNotification(Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	private static void notifyChange(Object object) {
		String capture = null;
		String lost = null;
		List<String> captureNotifyList = sNotifyMap.get(capture);
		List<String> lostNotifyList = sNotifyMap.get(lost);
		Set<String> notifyList = Stream.concat(captureNotifyList.stream(), lostNotifyList.stream())
				.collect(Collectors.toSet());

		// {0} - UserName
		// {1} - Capture Clan Name
		// {2} - Lost Clan Name
		// {3} - Song Name
		// {4} - Difficulty Name
		// {5} - pp
		// {6} - acc
		// {7} - Capture Percentage
		String message = MessageFormat.format(
				"{0} [{1}] captured {3} - {4} by getting {5,number,#.##}pp with {6,number,#.##}% acc.\n"
						+ "Taking over the map from [{2}] which brings [{1}] to {7,number,#.##}% of global dominance!",
						null);

		notifyList.stream().map(Integer::parseInt).map(id -> DiscordBot.getJDA().getTextChannelById(id))
		.forEach(channel -> channel.sendMessage(message).queue()); // .addFiles(null)
	}
}
