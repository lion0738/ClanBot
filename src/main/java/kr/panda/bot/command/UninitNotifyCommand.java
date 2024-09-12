package kr.panda.bot.command;

import java.util.List;
import java.util.Optional;

import kr.panda.bot.ClanWarNotifier;
import kr.panda.bot.viewer.IViewer.Callback;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class UninitNotifyCommand implements ICommand {
	public static final String COMMAND = "initclanwarnotify";
	public static final String TAG_OPTION = "clantag";

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(COMMAND, "Disable clan war notifications in this channel.")
				.addOption(OptionType.STRING, TAG_OPTION, "Tag of the clan (ex. TECH), Enter 'ALL' to disable all notifications.", true);
	}

	@Override
	public boolean isEphemeral() {
		return true;
	}

	@Override
	public void onSlashCommand(InteractionHook hook, List<OptionMapping> options, Callback callback) {
		if (hook.getInteraction().getChannelType() != ChannelType.TEXT) {
			hook.editOriginal("Can only be used in the server's text channels.").queue();
			return;
		}

		Member member = hook.getInteraction().getMember();
		if (!member.isOwner() || !member.hasPermission(Permission.ADMINISTRATOR)) {
			hook.editOriginal("Can only be used by the server administrator.").queue();
			return;
		}

		String clanTag = null;
		Optional<OptionMapping> tagOption = options.stream()
				.filter(option -> option.getName().equals(TAG_OPTION)).findFirst();
		if (tagOption.isPresent()) {
			clanTag = tagOption.get().getAsString();
		}

		if (clanTag != null) {
			if (ClanWarNotifier.removeServer(hook.getInteraction().getChannelId(), clanTag)) {
				hook.editOriginal("Notifications have been successfully disabled.").queue();
			} else {
				hook.editOriginal("There are no notifications set for the specified clan tag.").queue();
			}
		} else {
			hook.editOriginal("Clan Tag Required").queue();
		}
	}
}
