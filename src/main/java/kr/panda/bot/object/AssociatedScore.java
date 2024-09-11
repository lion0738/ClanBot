package kr.panda.bot.object;

import com.google.gson.annotations.SerializedName;

public class AssociatedScore {
	@SerializedName("id")
	private int mId;

	@SerializedName("baseScore")
	private int mBaseScore;

	@SerializedName("modifiedScore")
	private int mModifiedScore;

	@SerializedName("accuracy")
	private double mAccuracy;

	@SerializedName("playerId")
	private String mPlayerId;

	@SerializedName("pp")
	private double mPp;

	@SerializedName("rank")
	private int mRank;

	@SerializedName("modifiers")
	private String mModifiers;

	@SerializedName("badCuts")
	private int mBadCuts;

	@SerializedName("missedNotes")
	private int mMissedNotes;

	@SerializedName("bombCuts")
	private int mBombCuts;

	@SerializedName("wallsHit")
	private int mWallsHit;

	@SerializedName("player")
	private Player mPlayer;

	public int getId() {
		return mId;
	}

	public int getBaseScore() {
		return mBaseScore;
	}

	public int getModifiedScore() {
		return mModifiedScore;
	}

	public double getAccuracy() {
		return mAccuracy;
	}

	public String getPlayerId() {
		return mPlayerId;
	}

	public double getPp() {
		return mPp;
	}

	public double getRank() {
		return mRank;
	}

	public String getModifiers() {
		return mModifiers;
	}

	public int getBadCuts() {
		return mBadCuts;
	}

	public int getMissedNotes() {
		return mMissedNotes;
	}

	public int getBombCuts() {
		return mBombCuts;
	}

	public int getWallsHit() {
		return mWallsHit;
	}

	public Player getPlayer() {
		return mPlayer;
	}
}
