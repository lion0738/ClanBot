package kr.spender.bot.object;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ClanConquerResponse {
	@SerializedName("metadata")
	private Metadata mMetadata;

	@SerializedName("data")
	private List<ClanMapData> mData;

	public Metadata getMetadata() {
		return mMetadata;
	}

	public List<ClanMapData> getData() {
		return mData;
	}
}