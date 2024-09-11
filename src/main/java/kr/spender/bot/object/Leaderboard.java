package kr.spender.bot.object;

import com.google.gson.annotations.SerializedName;

public class Leaderboard {
	@SerializedName("id")
	private String mId;

	@SerializedName("song")
	private Song mSong;

	@SerializedName("difficulty")
	private Difficulty mDifficulty;

	public String getId() {
		return mId;
	}

	public Song getSong() {
		return mSong;
	}

	public Difficulty getDifficulty() {
		return mDifficulty;
	}
}