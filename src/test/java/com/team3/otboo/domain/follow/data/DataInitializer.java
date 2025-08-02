package com.team3.otboo.domain.follow.data;

import com.team3.otboo.domain.follow.service.FollowService;
import com.team3.otboo.domain.follow.service.request.FollowCreateRequest;
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
	FollowService followService;

	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

	static final int BULK_INSERT_SIZE = 2000;
	static final int EXECUTE_COUNT = 6000; // 1200만개

	// 한 명의 슈퍼 팔로워
	private static final UUID SUPER_FOLLOWER_ID = UUID.randomUUID();

	@Test
	void initialize() throws InterruptedException {
		System.out.println("=== 한 사람이 1200만명 팔로우하는 데이터 생성 시작 ===");
		System.out.println("Super Follower ID: " + SUPER_FOLLOWER_ID);

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

		System.out.println("=== 팔로우 데이터 생성 완료 ===");
	}

	void insert() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < BULK_INSERT_SIZE; i++) {
				// 매번 새로운 followee (팔로우 당하는 사람)
				UUID followeeId = UUID.randomUUID();

				FollowCreateRequest request = new FollowCreateRequest(
					followeeId,        // 팔로우 당하는 사람 (매번 다름)
					SUPER_FOLLOWER_ID  // 팔로우 하는 사람 (항상 같음)
				);

				followService.createBulk(request);

				// 메모리 정리
				if (i % 500 == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}
		});
	}
}
