package kr.spender.bot;

import java.util.Timer;
import java.util.TimerTask;

import net.dv8tion.jda.api.JDA.Status;

public class Watchdog {
	private static final int TIME_LIMIT = 5;

	private DiscordBot mDiscordBot;

	private Timer mTimer;
	private TimerTask mTimerTask;

	private boolean mWasConnected = false;

	public Watchdog(DiscordBot discordBot) {
		mDiscordBot = discordBot;

		mTimer = new Timer();
		mTimerTask = new TimerTask() {
			@Override
			public void run() {
				checkStatus();
			}
		};
	}

	public void startWatch() {
		mTimer.scheduleAtFixedRate(mTimerTask, TIME_LIMIT * 60 * 1000, TIME_LIMIT * 60 * 1000);
	}

	private void checkStatus() {
		boolean isConnected = mDiscordBot.getStatus().equals(Status.CONNECTED);
		if (!mWasConnected && !isConnected) {
			System.out.println("5분 이상 미접속 확인. 프로세스 재시작");
			mDiscordBot.restart();
		}

		mWasConnected = isConnected;
	}
}
