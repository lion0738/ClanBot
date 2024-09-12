package kr.panda.bot.command;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kr.panda.bot.viewer.IViewer;
import kr.panda.bot.viewer.IViewer.Callback;
import kr.panda.bot.viewer.InfoViewer;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class InfoCommand implements ICommand {
	public static final String COMMAND = "info";
	private static final String GITHUB_API_URL =
			"https://api.github.com/repos/lion0738/ClanBot/commits";

	private static List<JsonObject> sCommits;

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(COMMAND, "Displays bot-related information.");
	}

	@Override
	public boolean isEphemeral() {
		return false;
	}

	@Override
	public void onSlashCommand(InteractionHook hook, List<OptionMapping> options, Callback callback) {		
		if (sCommits == null || sCommits.isEmpty()) {
			retrieveCommits();
		}

		String ownerId = hook.getInteraction().getUser().getId();
		IViewer viewer = new InfoViewer(callback, ownerId, sCommits);
		viewer.updateMessage(hook, true);
	}

	private void retrieveCommits() {
		sCommits = new ArrayList<>();
		try {
			JsonArray commits = retrieveCommitsFromAPI();
			if (commits != null) {
				for (JsonElement commit : commits) {
					sCommits.add(commit.getAsJsonObject());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JsonArray retrieveCommitsFromAPI() throws IOException {
		URL obj = new URL(GITHUB_API_URL);
		HttpURLConnection con = null;
		JsonArray result = null;
		try {
			con = (HttpURLConnection) obj.openConnection();
			con.setConnectTimeout(3000);
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			if (responseCode != 200) {
				System.out.println("\nSending 'GET' request to GitHub : "
						+ GITHUB_API_URL);
				System.out.println("Response Code : " + responseCode);
			}

			try (Reader reader = new InputStreamReader(con.getInputStream())) {
				result = JsonParser.parseReader(reader).getAsJsonArray();
			}
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		return result;
	}
}
