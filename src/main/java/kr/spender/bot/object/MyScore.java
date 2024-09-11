package kr.spender.bot.object;

import com.google.gson.annotations.SerializedName;

public class MyScore {
	@SerializedName("accLeft")
	private double mAccLeft;

	@SerializedName("accRight")
	private double mAccRight;

	@SerializedName("baseScore")
	private int mBaseScore;

	public double getAccLeft() {
		return mAccLeft;
	}

	public double getAccRight() {
		return mAccRight;
	}

	public int getBaseScore() {
		return mBaseScore;
	}
}