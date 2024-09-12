package kr.panda.bot.viewer;

import java.text.MessageFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import kr.panda.bot.DiscordBot;
import kr.panda.bot.utils.EmojiContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class InfoViewer extends Selector<JsonObject> {
	public static final String INVITE_URL =
			"https://discordapp.com/oauth2/authorize?client_id=1283367517456306230&scope=applications.commands%20bot&permissions=73728";
	public static final String SOURCE_URL = "https://github.com/lion0738/ClanBot";

	private MessageEmbed mInfoMessage;

	public InfoViewer(Callback callback, String id, List<JsonObject> list) {
		super(callback, id, list);

		mInfoMessage = getInfoMessage();
	}

	private MessageEmbed getInfoMessage() {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("Bot Info");
		eb.addField("Current server count", String.valueOf(DiscordBot.getJDA().getGuilds().size()), false);
		eb.addField("Report errors / Suggest features", "lion0738@naver.com", false);
		return eb.build();
	}

	@Override
	protected String getTitle() {
		return "Update log";
	}

	@Override
	protected List<ActionRow> getActionRows() {
		List<ActionRow> actionRows = new ArrayList<>();

		List<Button> arrows = new ArrayList<>();
		arrows.add(Button.secondary(EMOJI_LEFT_KEY,
				EmojiContainer.EMOJI_ARROW_LEFT));
		arrows.add(Button.secondary(EMOJI_RIGHT_KEY,
				EmojiContainer.EMOJI_ARROW_RIGHT));

		actionRows.add(ActionRow.of(arrows));

		actionRows.add(ActionRow.of(Button.link(INVITE_URL, "Invite Bot")));
		actionRows.add(ActionRow.of(Button.link(SOURCE_URL, "Source Code")));

		return actionRows;
	}

	@Override
	protected String getObjectString(JsonObject object) {
		String commitTime = object.get("commit").getAsJsonObject()
				.get("author").getAsJsonObject().get("date").getAsString();
		ZonedDateTime utcTime = ZonedDateTime.parse(commitTime);

		String message = object.get("commit").getAsJsonObject()
				.get("message").getAsString();

		return MessageFormat.format("{0} - <t:{1,number,#}>", message, utcTime.toEpochSecond());
	}

	@Override
	protected void onSelected(InteractionHook hook, JsonObject object) {
	}

	@Override
	protected List<MessageEmbed> getMessageEmbeds() {
		List<MessageEmbed> list = new ArrayList<>();
		list.add(mInfoMessage);
		list.add(getMessageEmbed());

		return list;
	}
}
