package kr.spender.bot.command;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import kr.spender.bot.object.ClanMapData;
import kr.spender.bot.utils.BeatLeaderAPIHelper;
import kr.spender.bot.viewer.ClanWarSelector;
import kr.spender.bot.viewer.IViewer;
import kr.spender.bot.viewer.IViewer.Callback;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClanWarCommand implements ICommand {
	public static final String COMMAND = "clanwar";
	public static final String TAG_OPTION = "clantag";

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(COMMAND, "Clan war info")
				.addOption(OptionType.STRING, TAG_OPTION, "Tag of the clan (ex. TECH)");
	}

	@Override
	public boolean isEphemeral() {
		return false;
	}

	@Override
	public void onSlashCommand(InteractionHook hook, List<OptionMapping> options, Callback callback) {
		String clanTag = null;
		Optional<OptionMapping> tagOption = options.stream()
				.filter(option -> option.getName().equals(TAG_OPTION)).findFirst();
		if (tagOption.isPresent()) {
			clanTag = tagOption.get().getAsString();
		} else if (hook.getInteraction().getGuild() != null &&
				hook.getInteraction().getGuild().getId().equals("1274090597837180969")) {
			// TODO - DB integration (maybe one day?)
			clanTag = "TECH";
		}

		if (clanTag != null) {
			List<ClanMapData> mapData = BeatLeaderAPIHelper.getClanMaps(clanTag);
			if (mapData != null && !mapData.isEmpty()) {
				String ownerId = hook.getInteraction().getUser().getId();
				IViewer viewer = new ClanWarSelector(callback, ownerId, mapData);
				viewer.updateMessage(hook, true);
			}
		} else {
			hook.editOriginal("Clan Tag Required").queue(
					success -> success.delete().queueAfter(3, TimeUnit.SECONDS));
		}
	}
}
