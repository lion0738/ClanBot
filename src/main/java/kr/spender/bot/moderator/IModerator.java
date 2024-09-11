package kr.spender.bot.moderator;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

public interface IModerator {
	public void initialize();
	public String getId();
	public void onMessageDelete(MessageDeleteEvent event);
	public void onMessageReactionAdd(MessageReactionAddEvent event);
	public void onButtonInteraction(ButtonInteractionEvent event);
	public void onStringSelectInteraction(StringSelectInteractionEvent event);
	public void onModalInteraction(ModalInteractionEvent event);
	public void onModeratorRemoved();
	public void shutdown();
}
