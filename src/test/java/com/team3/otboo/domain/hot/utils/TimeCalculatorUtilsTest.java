package com.team3.otboo.domain.hot.utils;

import java.time.Duration;
import org.junit.jupiter.api.Test;

public class TimeCalculatorUtilsTest {

	@Test
	void test() {
		// 현재 시간으로 부터 자정까지 남은 시간 계산 (이 계산 값을 ttl 로 사용해서 오늘 데이터만 남김.)
		Duration duration = TimeCalculatorUtils.calculateDurationToMidnight();
		System.out.println("duration.getSecond() / 60 / 60= " + duration.getSeconds() / 60 / 60);
	}
}
