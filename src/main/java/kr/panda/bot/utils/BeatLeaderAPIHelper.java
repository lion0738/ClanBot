package kr.panda.bot.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import kr.panda.bot.object.ClanConquerResponse;
import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.ClanMapLeaderboardData;

public class BeatLeaderAPIHelper {
	private static final String BEATLEADER_API_URL = "https://api.beatleader.xyz";
	private static final String CLAN_CONQUER_URL = BEATLEADER_API_URL + "/clan/{0}/maps?page=1&count=100&sortBy=toconquer";
	private static final String CLAN_MAP_LEADERBOARD_URL = BEATLEADER_API_URL + "/leaderboard/clanRankings/{0}/clan/{1}?page=1&count=100";
	private static Gson sGson = new Gson();

	private static Map<String, String> sGuildClanMap;

	public static void initialize() {
		loadDefaultClans();
	}

	private static void loadDefaultClans() {
		sGuildClanMap = new HashMap<>();

		List<Map<String, String>> list = DatabaseHelper.getRows("ClanList");
		for (Map<String, String> channelMap : list) {
			String guildId = channelMap.get("GuildId");
			String tagName = channelMap.get("ClanTag");

			sGuildClanMap.put(guildId, tagName);
		}
	}

	public static boolean setDefaultClanName(String guildId, String clanTag) {
		if (clanTag != null) {
			Map<String, String> target = new HashMap<>();
			target.put("GuildId", guildId);
			target.put("ClanTag", clanTag);
			DatabaseHelper.addRow("ClanList", target);
			sGuildClanMap.put(guildId, clanTag);
			return true;
		} else if (sGuildClanMap.containsKey(guildId)) {
			DatabaseHelper.deleteRow("ClanList", "GuildId", guildId);
			sGuildClanMap.remove(guildId);
			return true;
		}

		return false;
	}

	public static String getDefaultClanName(String guildId) {
		return sGuildClanMap.get(guildId);
	}

	public static List<ClanMapData> getClanMaps(String clanId) {
		String url = MessageFormat.format(CLAN_CONQUER_URL, clanId);
		ClanConquerResponse response = getObjectFromAPI(url, ClanConquerResponse.class);
		return response.getData();
	}

	public static ClanMapLeaderboardData getClanMapLeaderboard(String clanId, String leaderboardId) {
		String url = MessageFormat.format(CLAN_MAP_LEADERBOARD_URL, leaderboardId, clanId);
		ClanMapLeaderboardData response = getObjectFromAPI(url, ClanMapLeaderboardData.class);
		return response;
	}

	private static <T> T getObjectFromAPI(String url, Class<T> clazz) {
		String response = getStringFromApi(url);
		return (T) sGson.fromJson(response, clazz);
	}

	private static String getStringFromApi(String url) {
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			StringBuilder jsonResponse = new StringBuilder();
			try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), "UTF-8")) {
				int data = reader.read();
				while (data != -1) {
					jsonResponse.append((char) data);
					data = reader.read();
				}
			}

			return jsonResponse.toString();
		} catch (IOException e) {
			return null;
		}
	}
}
