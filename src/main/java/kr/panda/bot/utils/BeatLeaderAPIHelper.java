package kr.panda.bot.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import com.google.gson.Gson;

import kr.panda.bot.object.ClanConquerResponse;
import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.ClanMapLeaderboardData;

public class BeatLeaderAPIHelper {
	private static final String BEATLEADER_API_URL = "https://api.beatleader.xyz";
	private static final String CLAN_COMQUER_URL = BEATLEADER_API_URL + "/clan/{0}/maps?page=1&count=50&sortBy=toconquer";
	private static final String CLAN_MAP_LEADERBOARD_URL = BEATLEADER_API_URL + "/leaderboard/clanRankings/{0}/clan/{1}?page=1&count=100";
	private static Gson sGson = new Gson();

	public static List<ClanMapData> getClanMaps(String clanId) {
		String url = MessageFormat.format(CLAN_COMQUER_URL, clanId);
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
