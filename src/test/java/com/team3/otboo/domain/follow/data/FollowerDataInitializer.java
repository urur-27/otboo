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
class FollowerBulkInitializer {

	@PersistenceContext
	EntityManager em;

	@Autowired
	TransactionTemplate tx;

	@Autowired
	FollowService followService;

	private UUID targetUserId;

	static final int FOLLOWER_SIZE = 2_000_000; // 200만 팔로워
	static final int BATCH_SIZE = 500;
	static final int THREAD_COUNT = 1;
	static final int TASK_PER_THREAD = FOLLOWER_SIZE / THREAD_COUNT;

	// 팔로워 받을 user 객체 1개 생성 .
	@BeforeEach
	void createTargetUser() {
		tx.executeWithoutResult(status -> {
			User target = User.builder()
				.username("호날두")
				.email("ronaldo@example.com")
				.password("password")
				.role(Role.USER)
				.build();

			target.setProfile(Profile.builder()
				.user(target)
				.gender(Gender.MALE)
				.birthDate(LocalDate.of(1999, 1, 1))
				.build());

			em.persist(target);
			em.flush();

			targetUserId = target.getId();       // ★ 이 아이디가 followeeId
		});
	}

	/* -------------------------------------------------------
	   2) 20,000명의 팔로워 계정 + follow 레코드 생성
	------------------------------------------------------- */
	@Test
	void initialize() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

		for (int t = 0; t < THREAD_COUNT; t++) {
			pool.submit(() -> {
				insert(TASK_PER_THREAD);
				latch.countDown();
			});
		}

		latch.await();
		pool.shutdown();
	}

	private void insert(int count) {
		tx.executeWithoutResult(status -> {
			for (int i = 0; i < count; i++) {
				User follower = User.builder()
					.username("follower_" + UUID.randomUUID())
					.email(UUID.randomUUID() + "@example.com")
					.password("pw")
					.role(Role.USER)
					.build();
				em.persist(follower);

				followService.createBulk(new FollowCreateRequest(
					targetUserId,
					follower.getId()
				));

				if (i % BATCH_SIZE == 0) {
					em.flush();
					em.clear();
				}
			}
		});
	}
}
