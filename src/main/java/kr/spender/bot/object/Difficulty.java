package kr.spender.bot.object;

import com.google.gson.annotations.SerializedName;

public class Difficulty {
	@SerializedName("id")
	private long mId;

	@SerializedName("value")
	private int mValue;

	@SerializedName("mode")
	private int mMode;

	@SerializedName("difficultyName")
	private String mDifficultyName;

	@SerializedName("modifiersRating")
	private ModifiersRating mModifiersRating;

	@SerializedName("stars")
	private double mStars;

	@SerializedName("predictedAcc")
	private double mPredictedAcc;

	@SerializedName("passRating")
	private double mPassRating;

	@SerializedName("accRating")
	private double mAccRating;

	@SerializedName("techRating")
	private double mTechRating;

	public long getId() {
		return mId;
	}

	public int getValue() {
		return mValue;
	}

	public int getMode() {
		return mMode;
	}

	public String getDifficultyName() {
		return mDifficultyName;
	}

	public ModifiersRating getModifiersRating() {
		return mModifiersRating;
	}

	public double getStars() {
		return mStars;
	}

	public double getPredictedAcc() {
		return mPredictedAcc;
	}

	public double getPassRating() {
		return mPassRating;
	}

	public double getAccRating() {
		return mAccRating;
	}

	public double getTechRating() {
		return mTechRating;
	}
}