package kr.spender.bot.command;

import java.util.List;

import kr.spender.bot.viewer.IViewer.Callback;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {
	public String getCommand();
	public SlashCommandData getCommandData();
	public boolean isEphemeral();
	public void onSlashCommand(InteractionHook hook,
			List<OptionMapping> options, Callback callback);
}
