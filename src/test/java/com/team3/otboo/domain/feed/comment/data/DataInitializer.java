package com.team3.otboo.domain.feed.comment.data;

import com.team3.otboo.domain.feed.entity.Comment;
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
public class DataInitializer {

	@PersistenceContext
	EntityManager entityManager;
	@Autowired
	TransactionTemplate transactionTemplate;
	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

	static final int BULK_INSERT_SIZE = 20000;
	static final int EXECUTE_COUNT = 1;

	private static final UUID FEED_ID = UUID.randomUUID();
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
				.profileImageUrl(null)
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
				System.out.println("Batch completed. Remaining: " + latch.getCount());
			});
		}

		latch.await();
		executorService.shutdown();
	}

	void insert() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < BULK_INSERT_SIZE; i++) {
				Comment comment = Comment.create(
					FEED_ID,
					userId,
					"comment content" + i
				);
				entityManager.persist(comment);
			}
			entityManager.flush();
			entityManager.clear();
		});
	}
}
