package kr.panda.bot.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.collections4.map.MultiKeyMap;

import kr.panda.bot.DiscordBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Util {
	private static MultiKeyMap<String, String> sNameMap = new MultiKeyMap<>();

	/**
	 * Generates a random hash using SHA-256 algorithm.
	 *
	 * @return The generated random hash.
	 */
	public static String getRandomHash() {
		String result = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(UUID.randomUUID().toString().getBytes());

			byte[] bytes = md.digest();
			StringBuilder builder = new StringBuilder();
			for (byte b : bytes) {
				builder.append(String.format("%02x", b));
			}

			result = builder.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * Gets the username of a user based on the channel ID and user ID.
	 *
	 * @param channelId The ID of the channel.
	 * @param id        The ID of the user.
	 * @return The username of the user.
	 */
	public static String getUserName(String channelId, String id) {
		TextChannel channel = DiscordBot.getJDA().getTextChannelById(channelId);
		if (channel != null) {
			return getUserName(channel, id);
		} else {
			return "Unknown User";
		}
	}

	/**
	 * Gets the username of a user based on the channel and user ID.
	 *
	 * @param channel The text channel.
	 * @param id      The ID of the user.
	 * @return The username of the user.
	 */
	public static String getUserName(TextChannel channel, String id) {
		String memberName = sNameMap.get(channel.getGuild().getId(), id);
		if (memberName == null) {
			Member member = null;
			try {
				member = channel.getGuild().retrieveMemberById(id).complete();
			} catch (ErrorResponseException e) {
			}

			if (member != null) {
				memberName = member.getNickname();
				if (memberName == null) {
					memberName = member.getUser().getName();
				}
			}

			if (memberName != null) {
				sNameMap.put(channel.getGuild().getId(), id, memberName);
			} else {
				memberName = "Unknown User";
			}
		}

		return memberName;
	}
}
