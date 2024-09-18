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
import com.google.gson.reflect.TypeToken;

import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.ClanMapLeaderboardData;
import kr.panda.bot.object.Player;
import kr.panda.bot.object.ServerResponse;

public class BeatLeaderAPIHelper {
	private static final String BEATLEADER_API_URL = "https://api.beatleader.xyz";
	private static final String CLAN_CONQUER_URL = BEATLEADER_API_URL + "/clan/{0}/maps?page=1&count=100&sortBy=toconquer";
	private static final String CLAN_HOLD_URL = BEATLEADER_API_URL + "/clan/{0}/maps?page=1&count=100&sortBy=tohold";
	private static final String CLAN_USER_URL = BEATLEADER_API_URL + "/clan/{0}?page=1&count=100&primary=true";
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

	/**
	 * Sets the default clan name for a guild.
	 * 
	 * @param guildId  the ID of the guild
	 * @param clanTag  the clan tag to set as the default
	 * @return true if the default clan name was set successfully, false otherwise
	 */
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

	/**
	 * Gets the default clan name for a guild.
	 * 
	 * @param guildId  the ID of the guild
	 * @return the default clan name, or null if not set
	 */
	public static String getDefaultClanName(String guildId) {
		return sGuildClanMap.get(guildId);
	}

	/**
	 * Gets the list of clan users for a given clan ID.
	 * 
	 * @param clanId  the ID of the clan
	 * @return the list of clan users
	 */
	public static List<Player> getClanUsers(String clanId) {
		String url = MessageFormat.format(CLAN_USER_URL, clanId);
		TypeToken<ServerResponse<Player>> returnType = new TypeToken<ServerResponse<Player>>() {};
		ServerResponse<Player> response = getObjectFromAPI(url, returnType);
		List<Player> result = response == null ? null : response.getData();
		return result;
	}

	/**
	 * Gets the list of maps to conquer for a given clan ID.
	 * 
	 * @param clanId  the ID of the clan
	 * @return the list of clan maps
	 */
	public static List<ClanMapData> getClanConquerMaps(String clanId) {
		String url = MessageFormat.format(CLAN_CONQUER_URL, clanId);
		TypeToken<ServerResponse<ClanMapData>> returnType = new TypeToken<ServerResponse<ClanMapData>>() {};
		ServerResponse<ClanMapData> response = getObjectFromAPI(url, returnType);
		List<ClanMapData> result = response == null ? null : response.getData();
		return result;
	}

	/**
	 * Gets the list of maps to hold for a given clan ID.
	 * 
	 * @param clanId  the ID of the clan
	 * @return the list of clan maps
	 */
	public static List<ClanMapData> getClanHoldMaps(String clanId) {
		String url = MessageFormat.format(CLAN_HOLD_URL, clanId);
		TypeToken<ServerResponse<ClanMapData>> returnType = new TypeToken<ServerResponse<ClanMapData>>() {};
		ServerResponse<ClanMapData> response = getObjectFromAPI(url, returnType);
		List<ClanMapData> result = response == null ? null : response.getData();
		return result;
	}

	/**
	 * Gets the leaderboard data for a specific clan and leaderboard ID.
	 * 
	 * @param clanId        the ID of the clan
	 * @param leaderboardId the ID of the leaderboard
	 * @return the leaderboard data
	 */
	public static ClanMapLeaderboardData getClanMapLeaderboard(String clanId, String leaderboardId) {
		String url = MessageFormat.format(CLAN_MAP_LEADERBOARD_URL, leaderboardId, clanId);
		ClanMapLeaderboardData response = getObjectFromAPI(url, ClanMapLeaderboardData.class);
		return response;
	}

	private static <T> T getObjectFromAPI(String url, TypeToken<T> typeToken) {
		String response = getStringFromApi(url);
		return sGson.fromJson(response, typeToken.getType());
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
