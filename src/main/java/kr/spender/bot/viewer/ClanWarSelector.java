package kr.spender.bot.viewer;

import java.text.MessageFormat;
import java.util.List;

import kr.spender.bot.object.ClanMapData;
import kr.spender.bot.object.Difficulty;
import kr.spender.bot.object.Song;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class ClanWarSelector extends Selector<ClanMapData> {
	public ClanWarSelector(Callback callback, String ownerId, List<ClanMapData> mapData) {
		super(callback, ownerId, mapData);
	}

	@Override
	protected String getTitle() {
		return "Clan War - To Conquer";
	}

	@Override
	protected String getObjectString(ClanMapData mapData) {
		Song song = mapData.getLeaderboard().getSong();
		String songName = song.getName();
		Difficulty difficulty = mapData.getLeaderboard().getDifficulty();
		String difficultyName = difficulty.getDifficultyName();
		double star = difficulty.getStars();
		double remainPp = mapData.getPp();

		return MessageFormat.format("{0,number,#.#pp} / {1} - {2} ({3,number,#.#★})",
				remainPp, songName, difficultyName, star);
	}

	@Override
	protected void onSelected(InteractionHook hook, ClanMapData mapData) {
		IViewer viewer = new ClanWarViewer(mCallback, hook, mapData);
		viewer.updateMessage(hook, true);
	}
}
