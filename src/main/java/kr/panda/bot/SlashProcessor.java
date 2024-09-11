package kr.panda.bot;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import kr.panda.bot.command.ICommand;
import kr.panda.bot.moderator.IModerator;
import kr.panda.bot.utils.EmojiContainer;
import kr.panda.bot.viewer.IViewer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.Command.Option;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

public class SlashProcessor implements IModerator {
	private static final int TIMEOUT = 300;

	private ScheduledExecutorService mBackgroundWorker;

	private Map<String, ICommand> mCommands;
	private Map<String, ICommand> mSpecialCommands;
	private Map<String, Stack<IViewer>> mViewers;
	private Map<String, Future<?>> mTimers;

	public SlashProcessor(ScheduledExecutorService worker) {
		mBackgroundWorker = worker;
		mCommands = new HashMap<>();
		mSpecialCommands = new HashMap<>();
		mViewers = new ConcurrentHashMap<>();
		mTimers = new ConcurrentHashMap<>();
	}

	public void initialize() {
		List<ICommand> commands = getCommands();
		List<ICommand> specialCommands = getSpecialCommands();

		commands.forEach(command -> mCommands.put(command.getCommand(), command));
		specialCommands.forEach(command -> mSpecialCommands.put(command.getCommand(), command));

		List<SlashCommandData> commandDatas = mCommands.values().stream()
				.map(command -> command.getCommandData())
				.collect(Collectors.toList());

		JDA jda = DiscordBot.getJDA();
		jda.retrieveCommands().queue(oldCommands
				-> updateCommands(jda, oldCommands, commandDatas));
	}

	private List<ICommand> getCommands() {
		List<ICommand> commands = new ArrayList<>();
		Reflections reflections = new Reflections(ICommand.class.getPackage().getName());
		for (Class<?> clazz : reflections.getSubTypesOf(ICommand.class)) {
			Constructor<?>[] constructors = clazz.getConstructors();
			for (Constructor<?> constructor : constructors) {
				Class<?>[] paramTypes = constructor.getParameterTypes();
				Object[] initargs = new Object[paramTypes.length];

				try {
					ICommand command = (ICommand) constructor.newInstance(initargs);
					commands.add(command);
					break;
				} catch (IllegalArgumentException | InstantiationException
						| IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}

		return commands;
	}

	private List<ICommand> getSpecialCommands() {
		List<ICommand> commands = new ArrayList<>();
		//commands.add(new UninitCommand(mCallback));

		return commands;
	}

	private IViewer.Callback mViewerCallback = new IViewer.Callback() {
		@Override
		public void onViewerCreated(Message message, IViewer viewer) {
			addViewer(message, viewer);
		}

		@Override
		public void onViewerDeleted(Message message, IViewer viewer) {
			removeViewer(message, viewer);
		}
	};

	private void addViewer(Message message, IViewer viewer) {
		if (updateTimer(message)) {
			String messageId = message.getId();
			Stack<IViewer> stack = mViewers.get(messageId);
			if (stack == null) {
				stack = new Stack<>();
				mViewers.put(messageId, stack);
				if (!message.isEphemeral()) {
					message.addReaction(EmojiContainer.EMOJI_NOT_COMPLETED).queue(null, err -> {});
				}
			}

			stack.push(viewer);
			if (stack.size() > 1) {
				boolean alreadyAdded = message.getReactions().stream().filter(
						reaction -> EmojiContainer.EMOJI_ARROW_LEFT.getFormatted().equals(
								reaction.getEmoji().getFormatted()))
						.findAny().isPresent();
				if (!alreadyAdded) {
					message.addReaction(EmojiContainer.EMOJI_ARROW_LEFT).queue(null, err -> {});
				}
			}
		}
	}

	private void removeViewer(Message message, IViewer viewer) {
		String messageId = message.getId();
		Stack<IViewer> viewerStack = mViewers.get(messageId);
		if (viewerStack != null) {
			viewerStack.remove(viewer);

			if (viewerStack.size() == 1) {
				if (message.isFromGuild()) {
					message.removeReaction(EmojiContainer.EMOJI_ARROW_LEFT).queue(null, err -> {});
				}
			} else if (viewerStack.size() == 0) {
				mViewers.remove(messageId);
				mTimers.remove(messageId).cancel(false);
			}
		}
	}

	private void removeViewer(MessageReactionAddEvent event, Message message) {
		if (updateTimer(message)) {
			String messageId = message.getId();
			Stack<IViewer> viewerStack = mViewers.get(messageId);
			if (viewerStack != null && viewerStack.size() > 1) {
				viewerStack.pop();
				viewerStack.peek().updateMessage(event, false);

				if (event.isFromGuild()) {
					if (viewerStack.size() == 1) {
						message.removeReaction(EmojiContainer.EMOJI_ARROW_LEFT).queue(null, err -> {});
					}

					event.getReaction().removeReaction(event.getUser()).queue(null, err -> {});
				}
			}
		}
	}

	private boolean updateTimer(Message message) {
		String messageId = message.getId();

		boolean shouldUpdate = true;
		if (mTimers.containsKey(messageId)) {
			shouldUpdate = mTimers.get(messageId).cancel(false);
		}

		if (shouldUpdate) {
			mTimers.put(messageId, mBackgroundWorker.schedule(
					() -> onTimerExpired(message), TIMEOUT, TimeUnit.SECONDS));
		}

		return shouldUpdate;
	}

	private void onTimerExpired(Message message) {
		Stack<IViewer> viewers = mViewers.remove(message.getId());
		if (viewers.size() > 0) {
			message.clearReactions().queue(null, err -> {});
			viewers.peek().onTimerExpired(message);
		}
	}

	private void updateCommands(JDA jda, List<Command> oldCommands,
			List<SlashCommandData> newCommands) {
		boolean shouldUpdate = false;

		// 삭제된 명령어 찾기
		for (Command oldCommand : oldCommands) {
			Optional<SlashCommandData> newCommandOptional = newCommands.stream()
					.filter(newCommand -> newCommand.getName()
							.equals(oldCommand.getName())).findAny();

			if (!newCommandOptional.isPresent()) {
				shouldUpdate = true;
			}
		}

		// 수정되거나 추가된 명령어 찾기
		for (SlashCommandData newCommand : newCommands) {
			Optional<Command> oldCommandOptional = oldCommands.stream()
					.filter(old -> old.getName().equals(newCommand.getName())).findAny();
			if (oldCommandOptional.isPresent()) {
				Command oldCommand = oldCommandOptional.get();
				// 설명 비교
				if (!oldCommand.getDescription().equals(newCommand.getDescription())) {
					shouldUpdate = true;
					break;
				}

				List<Option> oldOptions = oldCommand.getOptions();
				List<OptionData> newOptions = newCommand.getOptions();

				// 옵션 비교
				if (oldOptions.size() == newOptions.size()) {
					for (int i = 0; i < oldOptions.size(); i++) {
						Option oldOption = oldOptions.get(i);
						OptionData newOption = newOptions.get(i);

						boolean isSameName = oldOption.getName()
								.equals(newOption.getName());
						boolean isSameDesc = oldOption.getDescription()
								.equals(newOption.getDescription());

						if (!isSameName || !isSameDesc) {
							shouldUpdate = true;
							break;
						}
					}
				} else {
					shouldUpdate = true;
				}
			} else {
				shouldUpdate = true;
			}

			if (shouldUpdate) {
				break;
			}
		}

		if (shouldUpdate) {
			CommandListUpdateAction globalCommands = jda.updateCommands();
			globalCommands.addCommands(newCommands).queue();
		}
	}

	public void onSlashCommandInteraction(SlashCommandInteractionEvent event, boolean prohibited) {
		boolean hasPermission = event.isFromGuild() ? event.getGuild()
				.getSelfMember().hasAccess(event.getGuildChannel()) : true;
		if (hasPermission) {
			ICommand command = prohibited ? mSpecialCommands.get(event.getName())
					: mCommands.get(event.getName());
			if (command != null) {
				boolean isEphemeral = command.isEphemeral();
				event.deferReply(isEphemeral).queue(
						hook ->	command.onSlashCommand(hook, event.getOptions(),
								mViewerCallback));
			} else {
				event.reply("이 채널에서 해당 봇 명령어를 사용할 수 없습니다.").setEphemeral(true).queue();
			}
		} else {
			event.reply("이 채널에서 봇 명령어를 사용할 수 없습니다.").setEphemeral(true).queue();
		}
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		Stack<IViewer> viewers = mViewers.get(event.getMessageId());
		if (viewers != null && !viewers.isEmpty()) {
			IViewer viewer = viewers.peek();
			if (viewer.getOwnerId().equals(event.getUser().getId())) {
				updateTimer(event.getMessage());
				viewer.onStringSelectInteraction(event);
			}
		}
	}

	public void onButtonInteraction(ButtonInteractionEvent event) {
		Stack<IViewer> viewers = mViewers.get(event.getMessageId());
		if (viewers != null && !viewers.isEmpty()) {
			IViewer viewer = viewers.peek();
			if (viewer.getOwnerId().equals(event.getUser().getId())) {
				if (updateTimer(event.getMessage())) {
					event.deferEdit().queue(hook -> viewer
							.onButtonClick(hook, event.getButton().getId()));
				}
			}
		}
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		String messageId = event.getMessageId();
		if (mViewers.containsKey(messageId)) {
			mViewers.remove(messageId);
			Future<?> timer = mTimers.remove(messageId);
			if (timer != null) { 
				timer.cancel(false);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		String messageId = event.getMessageId();
		Stack<IViewer> viewers = mViewers.get(messageId);
		if (viewers != null && !viewers.isEmpty()) {
			IViewer viewer = viewers.peek();
			if (viewer.getOwnerId().equals(event.getUser().getId())) {
				String reactionName = event.getReaction().getEmoji().getFormatted();
				switch (reactionName) {
				case EmojiContainer.STRING_ARROW_LEFT:
					event.retrieveMessage().queue(message -> removeViewer(event, message));
					break;
				case EmojiContainer.STRING_NOT_COMPLETED:
					mViewers.remove(messageId);
					if (mTimers.containsKey(messageId)) {
						mTimers.remove(messageId).cancel(false);
					}

					event.getChannel().deleteMessageById(messageId).queue();
					break;
				}
			}
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public void onModeratorRemoved() {
	}
}
