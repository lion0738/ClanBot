package kr.panda.bot.object;

import com.google.gson.annotations.SerializedName;

public class Clan {
	@SerializedName("id")
	private String mId;

	@SerializedName("name")
	private String mName;

	@SerializedName("color")
	private String mColor;

	@SerializedName("icon")
	private String mIcon;

	@SerializedName("tag")
	private String mTag;

	@SerializedName("leaderID")
	private String mLeaderID;

	@SerializedName("description")
	private String mDescription;

	@SerializedName("bio")
	private String mBio;

	@SerializedName("pp")
	private double mPp;

	@SerializedName("rank")
	private int mRank;

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public String getColor() {
		return mColor;
	}

	public String getIcon() {
		return mIcon;
	}

	public String getTag() {
		return mTag;
	}

	public String getLeaderID() {
		return mLeaderID;
	}

	public String getDescription() {
		return mDescription;
	}

	public String getBio() {
		return mBio;
	}

	public double getPp() {
		return mPp;
	}

	public int getRank() {
		return mRank;
	}
}