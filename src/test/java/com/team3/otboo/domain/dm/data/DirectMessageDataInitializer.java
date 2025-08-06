package com.team3.otboo.domain.dm.data;

import com.team3.otboo.domain.dm.entity.DirectMessage;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Gender;
import com.team3.otboo.domain.user.enums.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Random;
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
public class DirectMessageDataInitializer {

	@PersistenceContext
	EntityManager entityManager;

	@Autowired
	TransactionTemplate transactionTemplate;

	CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

	static final int BULK_INSERT_SIZE = 200;
	static final int EXECUTE_COUNT = 2; // 데이터 400개 생성

	private UUID senderId;
	private UUID receiverId;
	private Random random = new Random();

	@BeforeEach
	void createTestUsers() {
		transactionTemplate.executeWithoutResult(status -> {
			// 첫 번째 유저 생성 (sender)
			User sender = User.builder()
				.username("senderUser")
				.email("sender@example.com")
				.password("password123")
				.role(Role.USER.USER)
				.build();

			Profile senderProfile = Profile.builder()
				.user(sender)
				.gender(Gender.MALE)
				.birthDate(LocalDate.of(1990, 5, 15))
				.location(null)
				.temperatureSensitivity(null)
				.binaryContent(null)
				.build();

			sender.setProfile(senderProfile);
			entityManager.persist(sender);
			entityManager.flush();
			senderId = sender.getId();

			// 두 번째 유저 생성 (receiver)
			User receiver = User.builder()
				.username("receiverUser")
				.email("receiver@example.com")
				.password("password123")
				.role(Role.USER)
				.build();

			Profile receiverProfile = Profile.builder()
				.user(receiver)
				.gender(Gender.FEMALE)
				.birthDate(LocalDate.of(1995, 3, 22))
				.location(null)
				.temperatureSensitivity(null)
				.binaryContent(null)
				.build();

			receiver.setProfile(receiverProfile);
			entityManager.persist(receiver);
			entityManager.flush();
			receiverId = receiver.getId();

			System.out.println(
				"Created users - Sender ID: " + senderId + ", Receiver ID: " + receiverId);
		});
	}

	@Test
	void initialize() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(4);

		for (int i = 0; i < EXECUTE_COUNT; i++) {
			executorService.submit(() -> {
				insertDirectMessages();
				latch.countDown();
			});
		}

		latch.await();
		executorService.shutdown();

	}

	void insertDirectMessages() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < BULK_INSERT_SIZE; i++) {
				// 랜덤하게 sender와 receiver를 바꿈
				UUID currentSender = random.nextBoolean() ? senderId : receiverId;
				UUID currentReceiver = currentSender.equals(senderId) ? receiverId : senderId;

				DirectMessage directMessage = DirectMessage.create(
					currentSender,
					currentReceiver,
					"랜덤 생성 댓글 " + i
				);

				entityManager.persist(directMessage);

				// 배치 사이즈마다 flush & clear로 메모리 관리
				if (i % 1000 == 0) {
					entityManager.flush();
					entityManager.clear();
				}
			}

			// 마지막 배치 처리
			entityManager.flush();
			entityManager.clear();
		});
	}
}
