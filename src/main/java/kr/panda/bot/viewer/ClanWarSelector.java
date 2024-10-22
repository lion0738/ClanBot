package kr.panda.bot.viewer;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import kr.panda.bot.object.Clan;
import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.Difficulty;
import kr.panda.bot.object.Player;
import kr.panda.bot.object.Song;
import kr.panda.bot.utils.BeatLeaderAPIHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ClanWarSelector extends Selector<ClanMapData> {
	private static final String CLAN_URL = "https://beatleader.xyz/clan/{0}";

	private static final int MODE_CONQUER = 0;
	private static final int MODE_HOLD = 1;

	private static final String BUTTON_ID_CONQUER = "conquer";
	private static final String BUTTON_ID_HOLD = "hold";

	private Clan mClan;
	private List<Player> mClanUsers;

	private int mMode;

	private List<ClanMapData> mConquerMapList;
	private List<ClanMapData> mHoldMapList;

	public ClanWarSelector(Callback callback, String ownerId, Clan clan,
			List<ClanMapData> mapData) {
		super(callback, ownerId, mapData);

		mMode = MODE_CONQUER;
		mConquerMapList = mapData;
		mClan = clan;
		mClanUsers = BeatLeaderAPIHelper.getClanUsers(mClan.getTag()).getData();

		setColor(Color.RED);
	}

	@Override
	protected String getTitle() {
		String title = null;
		switch (mMode) {
		case MODE_CONQUER:
			title = "Clan War - To Conquer";
			break;
		case MODE_HOLD:
			title = "Clan War - To Hold";
			break;
		}

		return title;
	}

	private MessageEmbed getClanInfoEmbed() {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle(mClan.getName(), MessageFormat.format(CLAN_URL, mClan.getTag()));
		builder.setThumbnail(mClan.getIcon());
		builder.setColor(Color.decode(mClan.getColor()));
		builder.setDescription(mClan.getDescription());
		builder.addField("Tag", String.valueOf(mClan.getTag()), true);
		builder.addField("Rank", String.valueOf(mClan.getRank()), true);
		builder.addField("Total PP", MessageFormat.format("{0,number,#.##}pp", mClan.getPp()), true);

		return builder.build();
	}

	@Override
	protected List<MessageEmbed> getMessageEmbeds() {
		List<MessageEmbed> list = new ArrayList<>();
		MessageEmbed clanInfoEmbed = getClanInfoEmbed();
		MessageEmbed songListEmbed = getMessageEmbed();

		list.add(clanInfoEmbed);
		list.add(songListEmbed);

		return list;
	}

	@Override
	protected String getObjectString(ClanMapData mapData) {
		Song song = mapData.getLeaderboard().getSong();
		String songName = song.getName();
		Difficulty difficulty = mapData.getLeaderboard().getDifficulty();
		String modeName = difficulty.getModeName();
		String difficultyName = difficulty.getDifficultyName();
		double star = difficulty.getStars();
		double remainPp = mapData.getPp();

		return MessageFormat.format("{0,number,#.#pp} / {1} - {2} {3} ({4,number,#.#★})",
				remainPp, songName, modeName, difficultyName, star);
	}

	@Override
	protected void onSelected(InteractionHook hook, ClanMapData mapData) {
		IViewer viewer = new ClanWarViewer(mCallback, hook, mapData, mClan, mClanUsers);
		viewer.updateMessage(hook, true);
	}

	@Override
	protected List<ActionRow> getActionRows() {
		List<ActionRow> actionList = super.getActionRows();
		Button conquerButton = Button.danger(BUTTON_ID_CONQUER, "To Conquer")
				.withDisabled(mMode == MODE_CONQUER);
		Button holdButton = Button.success(BUTTON_ID_HOLD, "To Hold")
				.withDisabled(mMode == MODE_HOLD);
		actionList.add(ActionRow.of(conquerButton, holdButton));

		return actionList;
	}

	@Override
	public void onButtonClick(InteractionHook hook, String buttonId) {
		switch (buttonId) {
		case BUTTON_ID_CONQUER:
			mMode = MODE_CONQUER;
			setList(mConquerMapList);
			setColor(Color.RED);
			updateMessage(hook, false);
			break;
		case BUTTON_ID_HOLD:
			mMode = MODE_HOLD;
			if (mHoldMapList == null) {
				mHoldMapList = BeatLeaderAPIHelper.getClanHoldMaps(mClan.getTag()).getData();
			}
			setList(mHoldMapList);
			setColor(Color.GREEN);
			updateMessage(hook, false);
			break;
		default:
			super.onButtonClick(hook, buttonId);
			break;
		}
	}
}
