package kr.panda.bot.object;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ServerResponse<T> {
	@SerializedName("metadata")
	private Metadata mMetadata;

	@SerializedName("data")
	private List<T> mData;

	public Metadata getMetadata() {
		return mMetadata;
	}

	public List<T> getData() {
		return mData;
	}
}