package kr.panda.bot.viewer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface IViewer {
	public interface Callback {
		public void onViewerCreated(Message message, IViewer viewer);
		public void onViewerDeleted(Message message, IViewer viewer);
	}

	public String getChannelId();
	public String getOwnerId();
	public void updateMessage(InteractionHook hook, boolean addViewer);
	public void updateMessage(GenericMessageEvent event, boolean addViewer);
	public void onButtonClick(InteractionHook hook, String buttonId);
	public void onStringSelectInteraction(StringSelectInteractionEvent event);
	public void onTimerExpired(Message message);
}
