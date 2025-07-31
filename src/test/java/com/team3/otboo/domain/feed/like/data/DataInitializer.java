package com.team3.otboo.domain.feed.like.data;

import com.team3.otboo.domain.feed.entity.Like;
import com.team3.otboo.domain.feed.service.LikeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class DataInitializer {

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	LikeService likeService;

	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

	static final int BULK_INSERT_SIZE = 2000;
	static final int EXECUTE_COUNT = 6000;

	private static final UUID FEED_ID = UUID.randomUUID();

	@Test
	void initialize() throws InterruptedException {
		System.out.println("=== 하나의 피드에 1200만개의 좋아요 생성 시작 ===");
		System.out.println("Feed ID: " + FEED_ID);

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		for (int i = 0; i < EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				insert();
				latch.countDown();
				System.out.println("latch.getCount() = " + latch.getCount());
			});
		}
		latch.await();
		executorService.shutdown();

		System.out.println("=== 좋아요 데이터 생성 완료 ===");
	}

	void insert() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < BULK_INSERT_SIZE; i++) {
				Like like = Like.create(
					FEED_ID,
					UUID.randomUUID()  // 매번 새로운 userId
				);
				entityManager.persist(like);

				// 메모리 정리
				if (i % 500 == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}
		});
	}
}
