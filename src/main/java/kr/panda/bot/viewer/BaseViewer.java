package kr.panda.bot.viewer;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageEditAction;

public abstract class BaseViewer implements IViewer {
	protected Callback mCallback;

	public BaseViewer(Callback callback) {
		mCallback = callback;
	}

	@Override
	public void updateMessage(GenericMessageEvent event, boolean addViewer) {
		List<MessageEmbed> messageEmbeds = getMessageEmbeds();
		List<ActionRow> actionRows = getActionRows();

		RestAction<Message> action = event.getChannel()
				.retrieveMessageById(event.getMessageId())
				.map(message -> message.editMessageEmbeds(messageEmbeds))
				.flatMap(message -> message.setComponents(actionRows));

		Consumer<Message> consumer = null;
		Consumer<Throwable> error = null;
		if (addViewer) {
			consumer = message -> mCallback.onViewerCreated(message, this);
			error = throwable -> event.getChannel()
					.retrieveMessageById(event.getMessageId())
					.map(message -> message.editMessageEmbeds(messageEmbeds))
					.flatMap(messageAction -> messageAction.setComponents(actionRows))
					.queue(message -> mCallback.onViewerCreated(message, this));
		}

		action.queue(consumer, error);
	}

	@Override
	public void updateMessage(InteractionHook hook, boolean addViewer) {
		List<MessageEmbed> messageEmbeds = getMessageEmbeds();
		List<ActionRow> actionRows = getActionRows();

		WebhookMessageEditAction<Message> action =
				hook.editOriginalEmbeds(messageEmbeds).setComponents(actionRows);

		Consumer<Message> success = null;
		if (addViewer) {
			success = message -> mCallback.onViewerCreated(message, this);
		}

		action.queue(success, err -> {});
	}

	protected List<MessageEmbed> getMessageEmbeds() {
		return Collections.singletonList(getMessageEmbed());
	};

	protected abstract MessageEmbed getMessageEmbed();
	protected abstract List<ActionRow> getActionRows();

	@Override
	public void onTimerExpired(Message message) {
		message.editMessageEmbeds(getMessageEmbeds()).setComponents(Collections.EMPTY_LIST).queue(null, err -> {});
	}

	public void onStringSelectInteraction(StringSelectInteractionEvent event) {}
}
