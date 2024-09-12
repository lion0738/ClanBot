package kr.panda.bot;

import java.lang.Thread.UncaughtExceptionHandler;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import kr.panda.bot.moderator.IModerator;
import kr.panda.bot.utils.BeatLeaderAPIHelper;
import kr.panda.bot.utils.DatabaseHelper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.SessionRecreateEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

// 봇 초대 - https://discordapp.com/oauth2/authorize?client_id=1283367517456306230&scope=applications.commands%20bot&permissions=73728
public class DiscordBot extends ListenerAdapter {
	private static JDA sJDA;

	private ExecutorService mForegroundWorker;
	private ScheduledExecutorService mBackgroundWorker;
	private SlashProcessor mSlash;

	private Map<String, IModerator> mModerators;

	public static void main(String[] args) {
		UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				e.printStackTrace();
				System.out.println("심각한 에러 발생");
				System.exit(0);
			}
		};
		Thread.setDefaultUncaughtExceptionHandler(handler);

		DiscordBot bot = new DiscordBot();

		Watchdog watchdog = new Watchdog(bot);
		watchdog.startWatch();
	}

	public DiscordBot() {
		initialize();
	}

	public void initialize() {
		mForegroundWorker = Executors.newFixedThreadPool(2);
		mBackgroundWorker = Executors.newScheduledThreadPool(2);

		if (mModerators == null) {
			mModerators = new ConcurrentHashMap<String, IModerator>();
		}

		if (mSlash == null) {
			mSlash = new SlashProcessor(mBackgroundWorker);
		}

		DatabaseHelper.initialize();
		BeatLeaderAPIHelper.initialize();
		ClanWarNotifier.initialize(mBackgroundWorker);

		List<GatewayIntent> intents = new ArrayList<GatewayIntent>();
		intents.add(GatewayIntent.DIRECT_MESSAGES);
		intents.add(GatewayIntent.DIRECT_MESSAGE_REACTIONS);
		intents.add(GatewayIntent.GUILD_MESSAGES);
		intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS);

		List<CacheFlag> disabledCaches = new ArrayList<CacheFlag>();
		disabledCaches.add(CacheFlag.ACTIVITY);
		disabledCaches.add(CacheFlag.CLIENT_STATUS);
		disabledCaches.add(CacheFlag.ONLINE_STATUS);
		disabledCaches.add(CacheFlag.VOICE_STATE);
		disabledCaches.add(CacheFlag.EMOJI);
		disabledCaches.add(CacheFlag.STICKER);
		disabledCaches.add(CacheFlag.SCHEDULED_EVENTS);

		String token = System.getenv("BOT_TOKEN");
		JDABuilder builder = JDABuilder.create(token, intents);
		builder.setChunkingFilter(ChunkingFilter.NONE);
		builder.setMemberCachePolicy(MemberCachePolicy.NONE);
		builder.disableCache(disabledCaches);

		EventListener listener = (event) -> mForegroundWorker.execute(() -> onEvent(event));
		builder.addEventListeners(listener);

		try {
			sJDA = builder.build();
		} catch (InvalidTokenException | IllegalArgumentException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void restart() {
		sJDA.shutdownNow();
		mForegroundWorker.shutdownNow();
		try {
			mBackgroundWorker.awaitTermination(15, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		for (IModerator moderator : mModerators.values()) {
			moderator.shutdown();
		}
		mModerators.clear();
		mSlash.shutdown();
		mSlash = null;

		initialize();
	}

	public static JDA getJDA() {
		return sJDA;
	}

	public Status getStatus() {
		return sJDA.getStatus();
	}

	@Override
	public void onReady(ReadyEvent event) {
		System.out.println("OnReady Called");
		List<Guild> guilds = sJDA.getGuilds();
		System.out.println("접속된 서버 수: " + guilds.size());

		mSlash.initialize();
	}

	@Override
	public void onSessionRecreate(SessionRecreateEvent event) {
		System.out.println("onSessionRecreate Called");
		for (IModerator moderator : mModerators.values()) {
			moderator.shutdown();
		}
		mModerators.clear();
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		if (event.getUser() == null || event.getUser().isBot()) return;

		IModerator moderator = mModerators.getOrDefault(event.getChannel().getId(), mSlash);
		try {
			moderator.onMessageReactionAdd(event);
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getChannel(),
					event.getUser(), e.getPermission());
		} catch (IllegalStateException | IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageDelete(MessageDeleteEvent event) {
		IModerator moderator = mModerators.getOrDefault(event.getChannel().getId(), mSlash);
		try {
			moderator.onMessageDelete(event);
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getChannel(),
					null, e.getPermission());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		try {
			if (event.getChannel() != null) {
				logSlashCommand(event);
				IModerator moderator = mModerators.get(event.getChannel().getId());
				boolean prohibited = moderator != null;
				mSlash.onSlashCommandInteraction(event, prohibited);
			}
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getChannel(),
					event.getUser(), e.getPermission());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	private void logSlashCommand(SlashCommandInteractionEvent event) {
		StringBuilder options = new StringBuilder();
		event.getOptions().forEach(option -> options.append(" " + option));
		if (event.isFromGuild()) {
			System.out.println(String.format("%s/%s/%s - /%s%s",
					event.getGuild().getName(), event.getChannel().getName(),
					event.getUser().getName(), event.getFullCommandName(), options.toString()));
		} else {
			System.out.println(String.format("개인 메시지/%s - /%s%s",
					event.getUser().getName(), event.getFullCommandName(), options.toString()));
		}
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		IModerator moderator = mModerators.getOrDefault(event.getChannel().getId(), mSlash);
		try {
			moderator.onButtonInteraction(event);
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getChannel(),
					event.getUser(), e.getPermission());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		IModerator moderator = mModerators.getOrDefault(event.getChannel().getId(), mSlash);
		try {
			moderator.onStringSelectInteraction(event);
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getChannel(),
					event.getUser(), e.getPermission());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onModalInteraction(ModalInteractionEvent event) {
		IModerator moderator = mModerators.getOrDefault(event.getChannel().getId(), mSlash);
		try {
			moderator.onModalInteraction(event);
		} catch (InsufficientPermissionException e) {
			onInsufficientPermissionException(event.getMessageChannel(),
					event.getUser(), e.getPermission());
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	private void onInsufficientPermissionException(MessageChannel channel, User user, Permission e) {
		String errMsg = MessageFormat.format("봇에 \"{0}\" 권한이 없어 요청하신 작업을 수행할 수 없습니다.", e.getName());
		System.out.println(MessageFormat.format("{0}-{1}", channel.getId(), e.getName()));
		if (!e.equals(Permission.MESSAGE_SEND)) {
			channel.sendMessage(errMsg).queue(null,
					failure -> {
						if (user != null) {
							user.openPrivateChannel().map(privChannel -> privChannel.sendMessage(errMsg)).queue();
						}
					});
		} else if (user != null) {
			user.openPrivateChannel().map(privChannel -> privChannel.sendMessage(errMsg)).queue();
		}
	}
}