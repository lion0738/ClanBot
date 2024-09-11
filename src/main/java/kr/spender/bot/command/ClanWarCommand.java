package kr.spender.bot.command;

import java.util.List;

import kr.spender.bot.object.ClanMapData;
import kr.spender.bot.utils.BeatLeaderAPIHelper;
import kr.spender.bot.viewer.ClanWarSelector;
import kr.spender.bot.viewer.IViewer;
import kr.spender.bot.viewer.IViewer.Callback;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClanWarCommand implements ICommand {
	public static final String COMMAND = "clanwar";

	@Override
	public String getCommand() {
		return COMMAND;
	}

	@Override
	public SlashCommandData getCommandData() {
		return Commands.slash(COMMAND, "Clan war info");
	}

	@Override
	public boolean isEphemeral() {
		return false;
	}

	@Override
	public void onSlashCommand(InteractionHook hook, List<OptionMapping> options, Callback callback) {
		List<ClanMapData> mapData = BeatLeaderAPIHelper.getClanMaps("55998");
		if (mapData != null && !mapData.isEmpty()) {
			String ownerId = hook.getInteraction().getUser().getId();
			IViewer viewer = new ClanWarSelector(callback, ownerId, mapData);
			viewer.updateMessage(hook, true);
		}
	}
}
