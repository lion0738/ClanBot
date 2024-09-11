package kr.spender.bot.viewer;

import java.util.ArrayList;
import java.util.List;

import kr.spender.bot.utils.EmojiContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class Selector<T> extends BaseViewer {
	public static final String EMOJI_LEFT_KEY = "left";
	public static final String EMOJI_RIGHT_KEY = "right";

	private List<T> mList;

	private String mOwnerId;
	private int mMaxPage;
	private int mCurrentPage;

	public Selector(Callback callback, String id, List<T> list) {
		super(callback);

		mOwnerId = id;
		mList = list;
		mMaxPage = (mList.size() - 1) / 5;
	}

	@Override
	protected MessageEmbed getMessageEmbed() {
		EmbedBuilder eb = new EmbedBuilder();

		int size = Math.min(mList.size(), mCurrentPage * 5 + 5);

		eb.setTitle(getTitle());
		for (int i = mCurrentPage * 5; i < size; i++) {
			T object = mList.get(i);
			eb.appendDescription(EmojiContainer.getNumberEmoji(i % 5 + 1).getName() + " "
					+ getObjectString(object) + "\n");
		}

		if (mMaxPage > 0) {
			eb.setFooter("(" + (mCurrentPage + 1) + "/" + (mMaxPage + 1) + ")", null);
		}

		return eb.build();
	}

	@Override
	protected List<ActionRow> getActionRows() {
		List<ActionRow> actionRows = new ArrayList<>();

		List<Button> buttons = new ArrayList<>();
		for (int i = 0; i < Math.min(5, mList.size()); i++) {
			String buttonLabel = String.valueOf(i + 1);
			boolean disabled = mList.size() - mCurrentPage * 5 - (i + 1) < 0;
			buttons.add(Button.primary(buttonLabel, buttonLabel)
					.withDisabled(disabled));
		}
		actionRows.add(ActionRow.of(buttons));

		if (mList.size() > 5) {
			List<Button> arrows = new ArrayList<Button>();
			arrows.add(Button.secondary(EMOJI_LEFT_KEY,
					EmojiContainer.EMOJI_ARROW_LEFT));
			arrows.add(Button.secondary(EMOJI_RIGHT_KEY,
					EmojiContainer.EMOJI_ARROW_RIGHT));

			actionRows.add(ActionRow.of(arrows));
		}

		return actionRows;
	}

	protected abstract String getTitle();
	protected abstract String getObjectString(T object);
	protected abstract void onSelected(InteractionHook hook, T object);

	@Override
	public String getChannelId() {
		return null;
	}

	@Override
	public String getOwnerId() {
		return mOwnerId;
	}

	@Override
	public void onButtonClick(InteractionHook hook, String buttonId) {
		switch (buttonId) {
		case EMOJI_LEFT_KEY:
			mCurrentPage = --mCurrentPage < 0 ? mMaxPage : mCurrentPage;
			updateMessage(hook, false);
			break;
		case EMOJI_RIGHT_KEY:
			mCurrentPage = (mCurrentPage + 1) % (mMaxPage + 1);
			updateMessage(hook, false);
			break;
		default:
			int selectedItem = Integer.parseInt(buttonId) - 1;
			if (mCurrentPage * 5 + selectedItem < mList.size()) {
				onSelected(hook, mList.get(mCurrentPage * 5 + selectedItem));
			}
			break;
		}
	}
}
