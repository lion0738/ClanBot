package kr.panda.bot.object;

import com.google.gson.annotations.SerializedName;

public class Player {
	@SerializedName("id")
	private String mId;

	@SerializedName("name")
	private String mName;

	@SerializedName("platform")
	private String mPlatform;

	@SerializedName("avatar")
	private String mAvatar;

	@SerializedName("country")
	private String mCountry;

	@SerializedName("alias")
	private String mAlias;

	@SerializedName("bot")
	private boolean mBot;

	@SerializedName("pp")
	private double mPp;

	@SerializedName("rank")
	private int mRank;

	@SerializedName("countryRank")
	private int mCountryRank;

	@SerializedName("role")
	private String mRole;

	public String getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public String getPlatform() {
		return mPlatform;
	}

	public String getAvatar() {
		return mAvatar;
	}

	public String getCountry() {
		return mCountry;
	}

	public String getAlias() {
		return mAlias;
	}

	public boolean ismBot() {
		return mBot;
	}

	public double getPp() {
		return mPp;
	}

	public int getRank() {
		return mRank;
	}

	public int getCountryRank() {
		return mCountryRank;
	}

	public String getRole() {
		return mRole;
	}
}
