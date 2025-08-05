package com.team3.otboo.domain.follow.data;

import com.team3.otboo.domain.follow.service.FollowService;
import com.team3.otboo.domain.follow.service.request.FollowCreateRequest;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Gender;
import com.team3.otboo.domain.user.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class FollowingDataInitializer {

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	TransactionTemplate transactionTemplate;

	@Autowired
	FollowService followService;

	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

	static final int BULK_INSERT_SIZE = 200;
	static final int EXECUTE_COUNT = 1;

	private UUID userId;

	@BeforeEach
	void createTestUser() {
		transactionTemplate.executeWithoutResult(status -> {
			// User 먼저 생성
			User user = User.builder()
				.username("testUser")
				.email("testuser@example.com")
				.password("password123")
				.role(Role.USER)
				.build();

			Profile profile = Profile.builder()
				.user(user)
				.gender(Gender.MALE)
				.birthDate(LocalDate.of(2025, 8, 4))
				.location(null)
				.temperatureSensitivity(null)
				.binaryContent(null)
				.build();

			user.setProfile(profile);

			entityManager.persist(user);
			entityManager.flush();
			userId = user.getId();
		});
	}

	@Test
	void initialize() throws InterruptedException {
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
	}

	void insert() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < BULK_INSERT_SIZE; i++) {
				User followee = User.builder()
					.username("follower_" + UUID.randomUUID())
					.email(UUID.randomUUID() + "@example.com")
					.password("pw")
					.role(Role.USER)
					.build();

				entityManager.persist(followee);

				FollowCreateRequest request = new FollowCreateRequest(
					followee.getId(),
					userId
				);

				followService.createBulk(request);

				if (i % 500 == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}
		});
	}
}
