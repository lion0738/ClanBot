package kr.panda.bot.object;

import com.google.gson.annotations.SerializedName;

public class ClanMapData {
	@SerializedName("id")
	private long mId;

	@SerializedName("clan")
	private Clan mClan;

	@SerializedName("lastUpdateTime")
	private long mLastUpdateTime;

	@SerializedName("averageRank")
	private double mAverageRank;

	@SerializedName("rank")
	private int mRank;

	@SerializedName("pp")
	private double mPp;

	@SerializedName("averageAccuracy")
	private double mAverageAccuracy;

	@SerializedName("totalScore")
	private long mTotalScore;

	@SerializedName("leaderboardId")
	private String mLeaderboardId;

	@SerializedName("leaderboard")
	private Leaderboard mLeaderboard;

	@SerializedName("myScore")
	private MyScore mMyScore;

	public long getId() {
		return mId;
	}

	public Clan getClan() {
		return mClan;
	}

	public long getLastUpdateTime() {
		return mLastUpdateTime;
	}

	public double getAverageRank() {
		return mAverageRank;
	}

	public int getRank() {
		return mRank;
	}

	public double getPp() {
		return mPp;
	}

	public double getAverageAccuracy() {
		return mAverageAccuracy;
	}

	public long getTotalScore() {
		return mTotalScore;
	}

	public String getLeaderboardId() {
		return mLeaderboardId;
	}

	public Leaderboard getLeaderboard() {
		return mLeaderboard;
	}

	public MyScore getMyScore() {
		return mMyScore;
	}
}