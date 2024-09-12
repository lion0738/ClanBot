package kr.panda.bot.viewer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import kr.panda.bot.object.AssociatedScore;
import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.ClanMapLeaderboardData;
import kr.panda.bot.object.Difficulty;
import kr.panda.bot.object.ModifiersRating;
import kr.panda.bot.object.Song;
import kr.panda.bot.utils.BeatLeaderAPIHelper;
import kr.panda.bot.utils.EmojiContainer;
import kr.panda.bot.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ClanWarViewer extends BaseViewer {
	private static final String BEATSAVER_URL = "https://beatsaver.com/maps/{0}";
	private static final String BEATLEADER_URL = "https://beatleader.xyz/leaderboard/clans/{0}/1?clanTag={1}";

	public static final String EMOJI_LEFT_KEY = "left";
	public static final String EMOJI_RIGHT_KEY = "right";

	private String mChannelId;
	private String mOwnerId;
	private ClanMapData mMapData;
	private ClanMapLeaderboardData mMapLeaderboardData;

	private int mMaxPage;
	private int mCurrentPage;

	public ClanWarViewer(Callback callback, InteractionHook hook, ClanMapData mapData) {
		super(callback);

		mChannelId = hook.getInteraction().getChannelId();
		mOwnerId = hook.getInteraction().getUser().getId();
		mMapData = mapData;
		mMapLeaderboardData = BeatLeaderAPIHelper.getClanMapLeaderboard(
				mMapData.getClan().getId(), mMapData.getLeaderboardId());

		mMaxPage = (mMapLeaderboardData.getAssociatedScores().size() - 1) / 5;
		mCurrentPage = 0;
	}

	@Override
	public String getChannelId() {
		return mChannelId;
	}

	@Override
	public String getOwnerId() {
		return mOwnerId;
	}

	@Override
	public void onButtonClick(InteractionHook hook, String buttonId) {
		switch (buttonId) {
		case EMOJI_LEFT_KEY:
			mCurrentPage = --mCurrentPage < 0 ? mMaxPage : mCurrentPage;
			updateMessage(hook, false);
			break;
		case EMOJI_RIGHT_KEY:
			mCurrentPage = (mCurrentPage + 1) % (mMaxPage + 1);
			updateMessage(hook, false);
			break;
		}
	}

	private MessageEmbed getMapEmbed() {
		EmbedBuilder mapEmbedBuilder = new EmbedBuilder();
		Song song = mMapData.getLeaderboard().getSong();
		Difficulty difficulty = mMapData.getLeaderboard().getDifficulty();
		mapEmbedBuilder.setThumbnail(song.getCoverImage());
		mapEmbedBuilder.setTitle(MessageFormat.format("{0} - {1}", song.getName(), difficulty.getDifficultyName()));
		mapEmbedBuilder.setUrl(MessageFormat.format(BEATSAVER_URL, song.getId().substring(0, 5)));

		double requiredPp = mMapLeaderboardData.getPp() - mMapData.getPp();
		StringBuilder mapInfoBuilder = new StringBuilder();
		mapInfoBuilder.append(MessageFormat.format("Mappers: {0}\n", song.getMapper()));
		mapInfoBuilder.append(MessageFormat.format("Stars: {0,number,#.##}★\n", difficulty.getStars()));
		mapInfoBuilder.append(MessageFormat.format("Pass: {0,number,#.##}★ Acc: {1,number,#.##}★ Tech: {2,number,#.##}★\n",
				difficulty.getPassRating(), difficulty.getAccRating(), difficulty.getTechRating()));
		mapEmbedBuilder.addField("Map Info", mapInfoBuilder.toString(), false);

		StringBuilder clanInfoBuilder = new StringBuilder();
		clanInfoBuilder.append(MessageFormat.format("Clan PP: {0}\n", mMapLeaderboardData.getPp()));
		clanInfoBuilder.append(MessageFormat.format("Clan Rank: #{0}\n", mMapData.getRank()));
		clanInfoBuilder.append(MessageFormat.format("Average Rank: #{0}\n", mMapData.getAverageRank()));
		clanInfoBuilder.append(MessageFormat.format("Average Acc: {0, number, #.#}%\n", mMapData.getAverageAccuracy() * 100));
		clanInfoBuilder.append(MessageFormat.format("Total Score: {0}\n", mMapData.getTotalScore()));
		mapEmbedBuilder.addField("Clan Info", clanInfoBuilder.toString(), false);

		ModifiersRating modifiers = difficulty.getModifiersRating();
		double targetPp = Util.calculateRequiredPp(mMapLeaderboardData.getAssociatedScores(), null, requiredPp);
		double requiredAcc = Util.calculateAcc(targetPp, difficulty.getAccRating(), difficulty.getPassRating(), difficulty.getTechRating());
		double requiredFsAcc = Util.calculateAcc(targetPp, modifiers.getFsAccRating(), modifiers.getFsPassRating(), modifiers.getFsTechRating());
		double requiredSfAcc = Util.calculateAcc(targetPp, modifiers.getSfAccRating(), modifiers.getSfPassRating(), modifiers.getSfTechRating());
		StringBuilder conquerInfoBuilder = new StringBuilder();
		conquerInfoBuilder.append(MessageFormat.format("Diff from #1: {0,number,#.##pp}\n", mMapData.getPp()));
		conquerInfoBuilder.append(MessageFormat.format("New Play Required: "
				+ "{0,number,#.##pp} ({1,number,#.#}%, FS: {2,number,#.#}%, SF: {3,number,#.#}%)\n",
				targetPp, requiredAcc * 100, requiredFsAcc * 100, requiredSfAcc * 100));
		mapEmbedBuilder.addField("To Conquer", conquerInfoBuilder.toString(), false);

		return mapEmbedBuilder.build();
	}

	private MessageEmbed getClanLeaderboardEmbed() {
		EmbedBuilder clanLeaderboardEmbedBuilder = new EmbedBuilder();
		clanLeaderboardEmbedBuilder.setTitle("Map Leaderboard");
		List<AssociatedScore> scoreList = mMapLeaderboardData.getAssociatedScores();
		double requiredPp = mMapLeaderboardData.getPp() - mMapData.getPp();
		for (int i = mCurrentPage * 5; i < mCurrentPage * 5 + 5 && i < scoreList.size(); i++) {
			AssociatedScore score = scoreList.get(i);
			double targetPp = Util.calculateRequiredPp(scoreList, score, requiredPp);
			Difficulty difficulty = mMapData.getLeaderboard().getDifficulty();
			ModifiersRating modifiers = difficulty.getModifiersRating();
			double requiredAcc = Util.calculateAcc(targetPp, difficulty.getAccRating(), difficulty.getPassRating(), difficulty.getTechRating());
			double requiredFsAcc = Util.calculateAcc(targetPp, modifiers.getFsAccRating(), modifiers.getFsPassRating(), modifiers.getFsTechRating());
			double requiredSfAcc = Util.calculateAcc(targetPp, modifiers.getSfAccRating(), modifiers.getSfPassRating(), modifiers.getSfTechRating());

			StringBuilder playInfoBuilder = new StringBuilder();
			int misses = score.getMissedNotes() + score.getBombCuts() + score.getBadCuts();
			String missString = misses == 0 ? "FC" : misses + "X";
			playInfoBuilder.append(MessageFormat.format("Score: {0,number,#.##pp} ({1,number,#.#}%, {2}",
					score.getPp(), score.getAccuracy() * 100, missString));
			if (!score.getModifiers().equals("")) {
				playInfoBuilder.append(MessageFormat.format(", {0}", score.getModifiers()));
			}
			playInfoBuilder.append(")\n");
			playInfoBuilder.append(MessageFormat.format("Last Played: <t:{0,number,#}>\n", score.getTimeset()));
			playInfoBuilder.append(MessageFormat.format("To Conquer: {0,number,#.##}pp ({1,number,#.#}%, FS: {2,number,#.#}%, SF: {3,number,#.#}%)",
					targetPp, requiredAcc * 100, requiredFsAcc * 100, requiredSfAcc * 100));
			clanLeaderboardEmbedBuilder.addField(MessageFormat.format("#{0} {1}",
					score.getRank(), score.getPlayer().getName()), playInfoBuilder.toString(), false);
		}

		clanLeaderboardEmbedBuilder.setUrl(MessageFormat.format(BEATLEADER_URL,
				mMapData.getLeaderboardId(), mMapLeaderboardData.getClan().getTag()));
		return clanLeaderboardEmbedBuilder.build();
	}

	@Override
	protected List<MessageEmbed> getMessageEmbeds() {
		List<MessageEmbed> embedList = new ArrayList<>();
		embedList.add(getMapEmbed());
		embedList.add(getClanLeaderboardEmbed());
		return embedList;
	}

	@Override
	protected MessageEmbed getMessageEmbed() {
		return null;
	}

	@Override
	protected List<ActionRow> getActionRows() {
		List<ActionRow> actionRows = new ArrayList<>();

		if (mMapLeaderboardData.getAssociatedScores().size() > 5) {
			List<Button> arrows = new ArrayList<Button>();
			arrows.add(Button.secondary(EMOJI_LEFT_KEY,
					EmojiContainer.EMOJI_ARROW_LEFT));
			arrows.add(Button.secondary(EMOJI_RIGHT_KEY,
					EmojiContainer.EMOJI_ARROW_RIGHT));

			actionRows.add(ActionRow.of(arrows));
		}

		return actionRows;
	}

}
