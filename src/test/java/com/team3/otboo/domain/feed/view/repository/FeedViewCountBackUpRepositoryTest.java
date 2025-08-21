package com.team3.otboo.domain.feed.view.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.team3.otboo.domain.feed.entity.FeedViewCount;
import com.team3.otboo.domain.feed.repository.FeedViewCountBackUpRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class FeedViewCountBackUpRepositoryTest {

	@Autowired
	FeedViewCountBackUpRepository feedViewCountBackUpRepository;

	@PersistenceContext
	EntityManager entityManager;

	@Test
	@Transactional
	void updateViewCountTest() {

		UUID feedId = UUID.randomUUID();

		feedViewCountBackUpRepository.save(
			FeedViewCount.init(feedId, 0L)
		);
		entityManager.flush();
		entityManager.clear();

		// when
		int result1 = feedViewCountBackUpRepository.update(feedId, 100L);
		int result2 = feedViewCountBackUpRepository.update(feedId, 300L);
		int result3 = feedViewCountBackUpRepository.update(feedId, 200L);

		// then
		assertThat(result1).isEqualTo(1); // 백업 성공
		assertThat(result2).isEqualTo(1); // 백업 성공
		assertThat(result3).isEqualTo(0);

		Long backUpCount = feedViewCountBackUpRepository.findById(feedId)
			.map(FeedViewCount::getViewCount)
			.orElse(0L);

		assertThat(backUpCount).isEqualTo(300L);
	}
}
