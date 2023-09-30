package com.alexogden.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {
	public static long convertMinutesToTicks(int minutes) {
		long ticksPerSecond = 20L;
		long ticksPerMinute = 60L * ticksPerSecond;

		return minutes * ticksPerMinute;
	}

	public static long getMinutesSince(LocalDateTime startTime) {
		Duration timeSinceStart = Duration.between(startTime, LocalDateTime.now());

		return timeSinceStart.toMinutes();
	}

	public static long getSecondsSince(LocalDateTime startTime) {
		Duration timeSinceStart = Duration.between(startTime, LocalDateTime.now());

		return timeSinceStart.toSeconds();
	}
}
