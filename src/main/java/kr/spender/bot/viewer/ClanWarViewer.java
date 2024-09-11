package kr.spender.bot.viewer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import kr.spender.bot.object.AssociatedScore;
import kr.spender.bot.object.ClanMapData;
import kr.spender.bot.object.ClanMapLeaderboardData;
import kr.spender.bot.object.Difficulty;
import kr.spender.bot.object.ModifiersRating;
import kr.spender.bot.object.Song;
import kr.spender.bot.utils.BeatLeaderAPIHelper;
import kr.spender.bot.utils.EmojiContainer;
import kr.spender.bot.utils.Util;
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
		mapEmbedBuilder.setDescription(MessageFormat.format("Mappers: {0}\n", song.getMapper()));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Stars: {0,number,#.##}★\n", difficulty.getStars()));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Pass: {0,number,#.##}★ Acc: {1,number,#.##}★ Tech: {2,number,#.##}★\n",
				difficulty.getPassRating(), difficulty.getAccRating(), difficulty.getTechRating()));
		mapEmbedBuilder.appendDescription("\n");

		double requiredPp = mMapLeaderboardData.getPp() - mMapData.getPp();
		ModifiersRating modifiers = difficulty.getModifiersRating();
		double targetPp = Util.calculateRequiredPp(mMapLeaderboardData.getAssociatedScores(), null, requiredPp);
		double requiredAcc = Util.calculateAcc(targetPp, difficulty.getAccRating(), difficulty.getPassRating(), difficulty.getTechRating());
		double requiredFsAcc = Util.calculateAcc(targetPp, modifiers.getFsAccRating(), modifiers.getFsPassRating(), modifiers.getFsTechRating());
		double requiredSfAcc = Util.calculateAcc(targetPp, modifiers.getSfAccRating(), modifiers.getSfPassRating(), modifiers.getSfTechRating());

		mapEmbedBuilder.appendDescription(MessageFormat.format("Current PP: {0}\n", mMapLeaderboardData.getPp()));
		mapEmbedBuilder.appendDescription(MessageFormat.format("To Conquer: {0} "
				+ "({1,number,#.##pp} - {2,number,#.#}%, FS: {3,number,#.#}%, SF: {4,number,#.#}%)\n",
				mMapData.getPp(), targetPp, requiredAcc * 100, requiredFsAcc * 100, requiredSfAcc * 100));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Clan Rank: #{0}\n", mMapData.getRank()));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Average Rank: #{0}\n", mMapData.getAverageRank()));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Average Acc: {0, number, #.#}%\n", mMapData.getAverageAccuracy() * 100));
		mapEmbedBuilder.appendDescription(MessageFormat.format("Total Score: {0}\n", mMapData.getTotalScore()));
		mapEmbedBuilder.setUrl(MessageFormat.format(BEATSAVER_URL, song.getId().substring(0, 5)));

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

			int misses = score.getMissedNotes() + score.getBombCuts() + score.getBadCuts();
			String missString = misses == 0 ? "FC" : misses + "X";
			clanLeaderboardEmbedBuilder.addField(MessageFormat.format("#{0} {1}",
					score.getRank(), score.getPlayer().getName()),
					MessageFormat.format("{0,number,#.#}%, {1}, {2,number,#.##pp} {3}\n"
							+ "To Conquer: {4,number,#.##}pp ({5,number,#.#}%, FS: {6,number,#.#}%, SF: {7,number,#.#}%)",
							score.getAccuracy() * 100, missString, score.getPp(),
							score.getModifiers(), targetPp, requiredAcc * 100,
							requiredFsAcc * 100, requiredSfAcc * 100), false);
			
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
