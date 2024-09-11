package kr.spender.bot.object;

import com.google.gson.annotations.SerializedName;

public class Song {
	@SerializedName("id")
	private String mId;

	@SerializedName("hash")
	private String mHash;

	@SerializedName("name")
	private String mName;

	@SerializedName("author")
	private String mAuthor;

	@SerializedName("mapper")
	private String mMapper;

	@SerializedName("coverImage")
	private String mCoverImage;

	public String getId() {
		return mId;
	}

	public String getHash() {
		return mHash;
	}

	public String getName() {
		return mName;
	}

	public String getAuthor() {
		return mAuthor;
	}

	public String getMapper() {
		return mMapper;
	}

	public String getCoverImage() {
		return mCoverImage;
	}
}