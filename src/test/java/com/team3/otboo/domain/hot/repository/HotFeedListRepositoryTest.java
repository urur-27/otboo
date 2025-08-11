package com.team3.otboo.domain.hot.repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class HotFeedListRepositoryTest {

	@Autowired
	HotFeedListRepository hotFeedListRepository;

	@Test
	void addTest() throws InterruptedException {
		LocalDateTime time = LocalDateTime.of(2025, 8, 7, 0, 0);

		long limit = 5;

		UUID random1 = UUID.randomUUID();
		UUID random2 = UUID.randomUUID();
		UUID random3 = UUID.randomUUID();
		UUID random4 = UUID.randomUUID();
		UUID random5 = UUID.randomUUID();
		UUID random6 = UUID.randomUUID();
		UUID random7 = UUID.randomUUID();
		UUID random8 = UUID.randomUUID();
		UUID random9 = UUID.randomUUID();
		UUID random10 = UUID.randomUUID();

		// 1 10 2 9 3 만 남아야함 .
		// when
		hotFeedListRepository.add(random1, time, 10L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random2, time, 8L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random3, time, 6L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random4, time, 4L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random5, time, 2L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random6, time, 3L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random7, time, 5L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random8, time, 1L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random9, time, 7L, limit, Duration.ofSeconds(3));
		hotFeedListRepository.add(random10, time, 9L, limit, Duration.ofSeconds(3));

		// then
		List<String> feedIds = hotFeedListRepository.readAll("20250807");

		Assertions.assertThat(feedIds).hasSize(Long.valueOf(limit).intValue());
		Assertions.assertThat(feedIds.get(0)).isEqualTo(random1.toString());
		Assertions.assertThat(feedIds.get(1)).isEqualTo(random10.toString());
		Assertions.assertThat(feedIds.get(2)).isEqualTo(random2.toString());
		Assertions.assertThat(feedIds.get(3)).isEqualTo(random9.toString());
		Assertions.assertThat(feedIds.get(4)).isEqualTo(random3.toString());

		TimeUnit.SECONDS.sleep(5);

		Assertions.assertThat(hotFeedListRepository.readAll("20250807")).isEmpty();
	}
}
