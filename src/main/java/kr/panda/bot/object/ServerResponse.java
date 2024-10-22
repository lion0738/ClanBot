package kr.panda.bot.object;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ServerResponse<T, U> {
	@SerializedName("metadata")
	private Metadata mMetadata;

	@SerializedName("data")
	private List<T> mData;

	@SerializedName("container")
	private U mContainer;

	public Metadata getMetadata() {
		return mMetadata;
	}

	public List<T> getData() {
		return mData;
	}

	public U getContainer() {
		return mContainer;
	}
}