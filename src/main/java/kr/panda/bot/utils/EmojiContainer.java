package kr.panda.bot.utils;

import kr.panda.bot.DiscordBot;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;

/**
 * The EmojiContainer class contains constants and methods related to emojis.
 */
public class EmojiContainer {
	public static final String STRING_ARROW_UP = "⬆";
	public static final String STRING_ARROW_DOWN = "⬇";
	public static final String STRING_ARROW_LEFT = "⬅";
	public static final String STRING_ARROW_RIGHT = "➡";
	public static final String STRING_SQUARE = "⏹";
	public static final String STRING_BOOK = "📖";
	public static final String STRING_CHART = "📊";
	public static final String STRING_SEARCH = "🔍";

	public static final String STRING_COMPLETED = "✅";
	public static final String STRING_NOT_COMPLETED = "❎";

	public static final Emoji EMOJI_ARROW_UP = Emoji.fromUnicode(STRING_ARROW_UP);
	public static final Emoji EMOJI_ARROW_DOWN = Emoji.fromUnicode(STRING_ARROW_DOWN);
	public static final Emoji EMOJI_ARROW_LEFT = Emoji.fromUnicode(STRING_ARROW_LEFT);
	public static final Emoji EMOJI_ARROW_RIGHT = Emoji.fromUnicode(STRING_ARROW_RIGHT);
	public static final Emoji EMOJI_SQUARE = Emoji.fromUnicode(STRING_SQUARE);
	public static final Emoji EMOJI_BOOK = Emoji.fromUnicode(STRING_BOOK);
	public static final Emoji EMOJI_CHART = Emoji.fromUnicode(STRING_CHART);
	public static final Emoji EMOJI_SEARCH = Emoji.fromUnicode(STRING_SEARCH);

	public static final Emoji EMOJI_NUMBER_ZERO = Emoji.fromUnicode("0⃣");
	public static final Emoji EMOJI_NUMBER_ONE = Emoji.fromUnicode("1⃣");
	public static final Emoji EMOJI_NUMBER_TWO = Emoji.fromUnicode("2⃣");
	public static final Emoji EMOJI_NUMBER_THREE = Emoji.fromUnicode("3⃣");
	public static final Emoji EMOJI_NUMBER_FOUR = Emoji.fromUnicode("4⃣");
	public static final Emoji EMOJI_NUMBER_FIVE = Emoji.fromUnicode("5⃣");
	public static final Emoji EMOJI_NUMBER_SIX = Emoji.fromUnicode("6⃣");
	public static final Emoji EMOJI_NUMBER_SEVEN = Emoji.fromUnicode("7⃣");
	public static final Emoji EMOJI_NUMBER_EIGHT = Emoji.fromUnicode("8⃣");
	public static final Emoji EMOJI_NUMBER_NINE = Emoji.fromUnicode("9⃣");
	public static final Emoji EMOJI_NUMBER_TEN = Emoji.fromUnicode("🔟");

	public static final Emoji EMOJI_ALPHABET_A = Emoji.fromUnicode("🇦");
	public static final Emoji EMOJI_ALPHABET_B = Emoji.fromUnicode("🇧");
	public static final Emoji EMOJI_ALPHABET_C = Emoji.fromUnicode("🇨");
	public static final Emoji EMOJI_ALPHABET_D = Emoji.fromUnicode("🇩");
	public static final Emoji EMOJI_ALPHABET_E = Emoji.fromUnicode("🇪");
	public static final Emoji EMOJI_ALPHABET_F = Emoji.fromUnicode("🇫");
	public static final Emoji EMOJI_ALPHABET_G = Emoji.fromUnicode("🇬");
	public static final Emoji EMOJI_ALPHABET_H = Emoji.fromUnicode("🇭");
	public static final Emoji EMOJI_ALPHABET_I = Emoji.fromUnicode("🇮");
	public static final Emoji EMOJI_ALPHABET_J = Emoji.fromUnicode("🇯");
	public static final Emoji EMOJI_ALPHABET_K = Emoji.fromUnicode("🇰");
	public static final Emoji EMOJI_ALPHABET_L = Emoji.fromUnicode("🇱");
	public static final Emoji EMOJI_ALPHABET_N = Emoji.fromUnicode("🇳");
	public static final Emoji EMOJI_ALPHABET_M = Emoji.fromUnicode("🇲");
	public static final Emoji EMOJI_ALPHABET_R = Emoji.fromUnicode("🇷");

	public static final Emoji EMOJI_COMPLETED = Emoji.fromUnicode(STRING_COMPLETED);
	public static final Emoji EMOJI_NOT_COMPLETED = Emoji.fromUnicode(STRING_NOT_COMPLETED);

	/**
	 * Retrieves a custom emoji by its ID.
	 *
	 * @param id The ID of the emoji.
	 * @return The RichCustomEmoji object representing the emoji, or null if not found.
	 */
	public static RichCustomEmoji getEmoji(String id) {
		if (DiscordBot.getJDA() == null)
			return null;

		return DiscordBot.getJDA().getEmojiById(id);
	}

	/**
	 * Retrieves an emoji object representing a number.
	 *
	 * @param num The number for which to retrieve the emoji.
	 * @return The Emoji object representing the number, or null if not found.
	 */
	public static Emoji getNumberEmoji(int num) {
		Emoji result;
		switch (num) {
		case 0:
			result = EMOJI_NUMBER_ZERO;
			break;
		case 1:
			result = EMOJI_NUMBER_ONE;
			break;
		case 2:
			result = EMOJI_NUMBER_TWO;
			break;
		case 3:
			result = EMOJI_NUMBER_THREE;
			break;
		case 4:
			result = EMOJI_NUMBER_FOUR;
			break;
		case 5:
			result = EMOJI_NUMBER_FIVE;
			break;
		case 6:
			result = EMOJI_NUMBER_SIX;
			break;
		case 7:
			result = EMOJI_NUMBER_SEVEN;
			break;
		case 8:
			result = EMOJI_NUMBER_EIGHT;
			break;
		case 9:
			result = EMOJI_NUMBER_NINE;
			break;
		case 10:
			result = EMOJI_NUMBER_TEN;
			break;
		default:
			result = null;
			break;
		}

		return result;
	}
}
