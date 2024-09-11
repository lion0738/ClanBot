package kr.spender.bot.object;

import com.google.gson.annotations.SerializedName;

public class Metadata {
	@SerializedName("itemsPerPage")
	private int mItemsPerPage;

	@SerializedName("page")
	private int mPage;

	@SerializedName("total")
	private int mTotal;

	public int getItemsPerPage() {
		return mItemsPerPage;
	}

	public int getPage() {
		return mPage;
	}

	public int getTotal() {
		return mTotal;
	}
}