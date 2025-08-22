package com.team3.otboo.domain.feed.data;

import com.team3.otboo.domain.clothing.entity.Clothing;
import com.team3.otboo.domain.feed.entity.Comment;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.entity.Like;
import com.team3.otboo.domain.feed.entity.Ootd;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.weather.entity.Humidity;
import com.team3.otboo.domain.weather.entity.Precipitation;
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.entity.WeatherLocation;
import com.team3.otboo.domain.weather.entity.WindSpeed;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class DataInitializer {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private TransactionTemplate transactionTemplate;

	private final List<User> testUsers = new ArrayList<>();
	private final List<Clothing> testClothes = new ArrayList<>();
	private Weather testWeather;

	private static final int THREAD_COUNT = 10;
	private static final int FEEDS_PER_THREAD = 3;
	private static final int TOTAL_FEED_COUNT = THREAD_COUNT * FEEDS_PER_THREAD;

	@BeforeEach
	void setUp() {
		transactionTemplate.executeWithoutResult(status -> {
			for (int i = 0; i < 10; i++) {
				User user = User.builder()
					.username("test_user_" + i)
					.email("test" + i + "@example.com")
					.password("password123")
					.role(Role.USER)
					.build();
				entityManager.persist(user);
				testUsers.add(user);
			}

			Weather weather = Weather.of(
				LocalDateTime.now().minusHours(1),
				LocalDateTime.now(),
				SkyStatus.CLEAR,
				new WeatherLocation(37.5665, 126.9780, 60, 127, List.of("서울특별시", "종로구", "종로1가")),
				new Precipitation(PrecipitationType.NONE, 0.0, 0.0),
				new Humidity(60.0, 5.0),
				new Temperature(25.0, 2.0, 20.0, 30.0),
				new WindSpeed(2.5, WindSpeedLevel.WEAK)
			);
			entityManager.persist(weather);
			testWeather = weather;

			for (int i = 0; i < 5; i++) {
				User owner = getRandomUser();
				Clothing clothing = Clothing.of("test_clothing_" + i, owner);
				clothing.setType("OUTER");
				clothing.setImageUrl("http://example.com/image" + i + ".png");
				entityManager.persist(clothing);
				testClothes.add(clothing);
			}
		});
	}

	@Test
	@Commit
	void initialize() throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(TOTAL_FEED_COUNT);

		for (int i = 0; i < TOTAL_FEED_COUNT; i++) {
			executorService.submit(() -> {
				try {
					insertDataInTransaction();
				} finally {
					latch.countDown();
					System.out.println("남은 작업 수: " + latch.getCount());
				}
			});
		}

		latch.await();
		executorService.shutdown();
		System.out.println("Data initialization completed.");
	}

	private void insertDataInTransaction() {
		transactionTemplate.executeWithoutResult(status -> {
			User author = getRandomUser();
			Feed feed = Feed.create(
				author.getId(),
				testWeather.getId(),
				"테스트 피드 내용입니다. #" + System.currentTimeMillis()
			);
			entityManager.persist(feed);

			List<UUID> clothesIds = getRandomClothesIds(ThreadLocalRandom.current().nextInt(1, 4));
			for (UUID clothesId : clothesIds) {
				Ootd ootd = Ootd.create(feed.getId(), clothesId);
				entityManager.persist(ootd);
			}

			long commentCount = ThreadLocalRandom.current().nextLong(1, 11);
			for (int i = 0; i < commentCount; i++) {
				Comment comment = Comment.create(
					feed.getId(),
					getRandomUser().getId(),
					"테스트 댓글 " + i
				);
				entityManager.persist(comment);
			}

			long likeCount = ThreadLocalRandom.current().nextLong(1, testUsers.size() + 1);
			List<User> usersToLike = new ArrayList<>(testUsers);
			Collections.shuffle(usersToLike);
			for (int i = 0; i < likeCount; i++) {
				Like like = Like.create(feed.getId(), usersToLike.get(i).getId());
				entityManager.persist(like);
			}

			FeedCommentCount feedCommentCount = FeedCommentCount.init(feed.getId(), commentCount);
			FeedLikeCount feedLikeCount = FeedLikeCount.init(feed.getId(), likeCount);
			entityManager.persist(feedCommentCount);
			entityManager.persist(feedLikeCount);
		});
	}

	private User getRandomUser() {
		int randomIndex = ThreadLocalRandom.current().nextInt(testUsers.size());
		return testUsers.get(randomIndex);
	}

	private List<UUID> getRandomClothesIds(int count) {
		List<Clothing> shuffled = new ArrayList<>(testClothes);
		Collections.shuffle(shuffled);
		return shuffled.stream()
			.limit(count)
			.map(Clothing::getId)
			.toList();
	}
}
