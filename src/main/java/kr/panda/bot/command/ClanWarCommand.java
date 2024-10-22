package kr.panda.bot.command;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import kr.panda.bot.object.Clan;
import kr.panda.bot.object.ClanMapData;
import kr.panda.bot.object.ServerResponse;
import kr.panda.bot.utils.BeatLeaderAPIHelper;
import kr.panda.bot.viewer.ClanWarSelector;
import kr.panda.bot.viewer.IViewer;
import kr.panda.bot.viewer.IViewer.Callback;
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
		return Commands.slash(COMMAND, "Displays information related to the Clan War.")
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
		}
		if (clanTag == null && hook.getInteraction().getGuild() != null) {
			clanTag = BeatLeaderAPIHelper.getDefaultClanName(hook.getInteraction().getGuild().getId());
		}

		if (clanTag != null) {
			ServerResponse<ClanMapData, Clan> clanData = BeatLeaderAPIHelper.getClanConquerMaps(clanTag);
			Clan clan = clanData.getContainer();
			List<ClanMapData> mapData = clanData.getData();
			if (clan != null && mapData != null && !mapData.isEmpty()) {
				String ownerId = hook.getInteraction().getUser().getId();
				IViewer viewer = new ClanWarSelector(callback, ownerId, clan, mapData);
				viewer.updateMessage(hook, true);
			} else {
				hook.editOriginal("The clan tag may be invalid or there could be a server issue. "
						+ "Please try again later or contact bot developer if the problem persists.").queue();
			}
		} else {
			hook.editOriginal("Clan Tag Required").queue(
					success -> success.delete().queueAfter(3, TimeUnit.SECONDS));
		}
	}
}
