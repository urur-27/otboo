package com.team3.otboo.domain.hot.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class TimeCalculatorUtils {

	public static Duration calculateDurationToMidnight() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT); // 내일의 자정
		return Duration.between(now, midnight);
	}
}
