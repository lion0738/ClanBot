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

public class ClanWarSelector extends Selector<ClanMapData> {
	private static final String CLAN_URL = "https://beatleader.xyz/clan/{0}";
	private Clan mClan;
	private List<Player> mClanUsers;

	public ClanWarSelector(Callback callback, String ownerId, List<ClanMapData> mapData) {
		super(callback, ownerId, mapData);

		mClan = mapData.get(0).getClan();
		mClanUsers = BeatLeaderAPIHelper.getClanUsers(mClan.getTag());
	}

	@Override
	protected String getTitle() {
		return "Clan War - To Conquer";
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
		IViewer viewer = new ClanWarViewer(mCallback, hook, mapData, mClanUsers);
		viewer.updateMessage(hook, true);
	}
}
