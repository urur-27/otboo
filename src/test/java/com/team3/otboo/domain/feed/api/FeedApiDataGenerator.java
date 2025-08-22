package com.team3.otboo.domain.feed.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

public class FeedApiDataGenerator {

	private static final String SERVER_URL = "http://localhost:8080";
	private static final int USER_COUNT = 10;
	private static final int CLOTHING_COUNT = 5;
	private static final int FEED_COUNT = 100;
	private static final int THREAD_COUNT = 10;

	private static final RestClient restClient = RestClient.create(SERVER_URL);
	private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(
		new JavaTimeModule());

	private record CsrfToken(String token, String headerName) {

	}

	private record UserCreateRequest(String name, String email, String password) {

	}

	private record UserResponse(UUID id, String name, String email) {

	}

	private record SignInRequest(String email, String password) {

	}

	private record ClothesAttributeDto(UUID definitionId, String value) {

	}

	private record ClothesCreateRequest(UUID ownerId, String name, String type,
										List<ClothesAttributeDto> attributes) {

	}

	private record ClothingResponse(UUID id) {

	}

	private record FullWeatherDto(UUID id) {

	}

	private record FeedCreateRequest(UUID authorId, UUID weatherId, List<UUID> clothesIds,
									 String content) {

	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println("🚀 API를 통한 데이터 자동 생성을 시작합니다. 서버: " + SERVER_URL);

		try {
			CsrfResponse csrf = getCsrfToken();
			System.out.println("✅ CSRF 토큰 획득 완료.");

			List<UserData> users = createUsers(csrf, USER_COUNT);
			System.out.printf("✅ 사용자 %d명 생성 완료.%n", users.size());

			UserData primaryUser = users.getFirst();
			String accessToken = authenticateUser(csrf, primaryUser.email(),
				primaryUser.password());
			System.out.printf("✅ 기본 사용자(%s) 로그인 성공.%n", primaryUser.name());

			UUID weatherId = getWeatherId(accessToken, csrf);
			System.out.println("✅ 날씨 데이터 ID 획득 완료: " + weatherId);

			List<UUID> clothingIds = createClothingItems(accessToken, primaryUser.id(),
				CLOTHING_COUNT, csrf);
			System.out.printf("✅ 옷 %d벌 생성 완료.%n", clothingIds.size());

			Map<UUID, String> userTokens = authenticateAllUsers(csrf, users);
			System.out.println("✅ 모든 사용자 인증 토큰 준비 완료.");

			// 병렬 처리 (병렬 처리하면 동시성 문제 생김 .)
//			createFeedsConcurrently(userTokens, weatherId, clothingIds, FEED_COUNT, csrf);/

			// 하나의 스레드로 처리 .
			createFeedsSequentially(userTokens, weatherId, clothingIds, FEED_COUNT, csrf);

			System.out.println("\n🎉 모든 데이터 생성 작업이 성공적으로 완료되었습니다!");

		} catch (Exception e) {
			System.err.println("\n❌ 데이터 생성 중 심각한 오류 발생: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static UUID getWeatherId(String accessToken, CsrfResponse csrf) {
		double latitude = 37.5665;
		double longitude = 126.9780;

		List<FullWeatherDto> weatherList = restClient.get()
			.uri("/api/weathers?latitude={lat}&longitude={lon}", latitude, longitude)
			.header("Authorization", "Bearer " + accessToken)
			.header(csrf.headerName(), csrf.token())
			.header("Cookie", csrf.cookie())
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		if (weatherList == null || weatherList.isEmpty()) {
			throw new RuntimeException("날씨 데이터를 가져올 수 없습니다.");
		}
		return weatherList.getFirst().id();
	}

	private static List<UserData> createUsers(CsrfResponse csrf, int count) {
		System.out.println("--- 사용자 생성을 시작합니다 ---");
		List<UserData> createdUsers = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			long timestamp = System.currentTimeMillis();
			String name = "user_" + timestamp + "_" + i;
			String email = String.format("user_%d_%d@otboo.test", timestamp, i);
			String password = "password123!";
			UserCreateRequest request = new UserCreateRequest(name, email, password);
			UserResponse response = restClient.post()
				.uri("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.header(csrf.headerName(), csrf.token())
				.header("Cookie", csrf.cookie())
				.body(request)
				.retrieve()
				.body(UserResponse.class);
			createdUsers.add(new UserData(response.id(), name, email, password));
			System.out.printf("  - 생성된 사용자: %s (ID: %s)%n", name, response.id());
		}
		return createdUsers;
	}

	private static String authenticateUser(CsrfResponse csrf, String email, String password)
		throws JsonProcessingException {
		SignInRequest request = new SignInRequest(email, password);
		ResponseEntity<String> response = restClient.post()
			.uri("/api/auth/sign-in")
			.contentType(MediaType.APPLICATION_JSON)
			.header(csrf.headerName(), csrf.token())
			.header("Cookie", csrf.cookie())
			.body(request)
			.retrieve()
			.toEntity(String.class);
		return objectMapper.readValue(response.getBody(), String.class);
	}

	private static Map<UUID, String> authenticateAllUsers(CsrfResponse csrf, List<UserData> users) {
		System.out.println("--- 모든 사용자 인증을 시작합니다 ---");
		Map<UUID, String> userTokens = new ConcurrentHashMap<>();
		users.parallelStream().forEach(user -> {
			try {
				String token = authenticateUser(csrf, user.email(), user.password());
				userTokens.put(user.id(), token);
			} catch (JsonProcessingException e) {
				System.err.println("사용자 인증 실패: " + user.name());
			}
		});
		return userTokens;
	}

	private static List<UUID> createClothingItems(String accessToken, UUID ownerId, int count,
		CsrfResponse csrf) {
		System.out.println("--- 옷 데이터 생성을 시작합니다 ---");
		List<UUID> createdClothingIds = new ArrayList<>();
		String[] types = {"TOP", "BOTTOM", "OUTER", "SHOES", "ACCESSORY"};

		for (int i = 0; i < count; i++) {
			try {
				ClothesCreateRequest request = new ClothesCreateRequest(
					ownerId,
					"API Clothing " + i,
					types[i % types.length],
					Collections.emptyList()
				);

				Map<String, ClothesCreateRequest> requestMap = Collections.singletonMap("request",
					request);

				ClothingResponse response = restClient.post()
					.uri("/api/clothes")
					.header("Authorization", "Bearer " + accessToken)
					.header(csrf.headerName(), csrf.token())
					.header("Cookie", csrf.cookie())
					.contentType(MediaType.APPLICATION_JSON)
					.body(requestMap)
					.retrieve()
					.body(ClothingResponse.class);

				if (response != null && response.id() != null) {
					createdClothingIds.add(response.id());
					System.out.printf("  - 생성된 옷: API Clothing %d (ID: %s)%n", i, response.id());
				}
			} catch (Exception e) {
				System.err.printf("❌ 옷 생성 실패 (항목 %d): %s%n", i, e.getMessage());
			}
		}
		return createdClothingIds;
	}

	private static void createFeedsSequentially(Map<UUID, String> userTokens, UUID weatherId,
		List<UUID> clothingIds, int count, CsrfResponse csrf) {
		// ExecutorService와 CountDownLatch를 모두 제거합니다.
		List<UUID> userIds = new ArrayList<>(userTokens.keySet());
		System.out.printf("--- 피드 %d개 생성을 순차적으로 시작합니다 ---\n", count);

		// 일반적인 for문으로 변경하여 작업을 하나씩 순서대로 실행합니다.
		for (int i = 0; i < count; i++) {
			final int feedNumber = i + 1;
			try {
				UUID authorId = userIds.get(
					ThreadLocalRandom.current().nextInt(userIds.size()));
				String accessToken = userTokens.get(authorId);
				FeedCreateRequest request = new FeedCreateRequest(
					authorId,
					weatherId,
					getRandomClothes(clothingIds),
					String.format("%d번째 피드입니다", feedNumber)
				);
				restClient.post()
					.uri("/api/feeds")
					.header("Authorization", "Bearer " + accessToken)
					.header(csrf.headerName(), csrf.token())
					.header("Cookie", csrf.cookie())
					.contentType(MediaType.APPLICATION_JSON)
					.body(request)
					.retrieve()
					.toBodilessEntity();
				System.out.printf("  - 피드 #%d 생성 완료 (작성자: ...%s)%n", feedNumber,
					authorId.toString().substring(30));
			} catch (Exception e) {
				System.err.printf("❌ 피드 #%d 생성 실패: %s%n", feedNumber, e.getMessage());
			}
		}
		// latch.await()와 executor.shutdown()도 필요 없으므로 제거합니다.
	}

	private static void createFeedsConcurrently(Map<UUID, String> userTokens, UUID weatherId,
		List<UUID> clothingIds, int count, CsrfResponse csrf) throws InterruptedException {
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch latch = new CountDownLatch(count);
		List<UUID> userIds = new ArrayList<>(userTokens.keySet());

		System.out.printf("--- 피드 %d개 생성을 시작합니다 ---\n", count);

		for (int i = 0; i < count; i++) {
			final int feedNumber = i + 1;
			executor.submit(() -> {
				try {
					UUID authorId = userIds.get(
						ThreadLocalRandom.current().nextInt(userIds.size()));
					String accessToken = userTokens.get(authorId);
					FeedCreateRequest request = new FeedCreateRequest(
						authorId,
						weatherId,
						getRandomClothes(clothingIds),
						String.format("%d번째 피드입니다", feedNumber)
					);
					restClient.post()
						.uri("/api/feeds")
						.header("Authorization", "Bearer " + accessToken)
						.header(csrf.headerName(), csrf.token())
						.header("Cookie", csrf.cookie())
						.contentType(MediaType.APPLICATION_JSON)
						.body(request)
						.retrieve()
						.toBodilessEntity();
					System.out.printf("  - 피드 #%d 생성 완료 (작성자: ...%s)%n", feedNumber,
						authorId.toString().substring(30));
				} catch (Exception e) {
					System.err.printf("❌ 피드 #%d 생성 실패: %s%n", feedNumber, e.getMessage());
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executor.shutdown();
	}

	private static List<UUID> getRandomClothes(List<UUID> source) {
		if (source.isEmpty()) {
			return Collections.emptyList();
		}
		List<UUID> shuffled = new ArrayList<>(source);
		Collections.shuffle(shuffled);
		int count = ThreadLocalRandom.current().nextInt(1, shuffled.size() + 1);
		return shuffled.subList(0, count);
	}

	private static CsrfResponse getCsrfToken() {
		ResponseEntity<CsrfToken> response = restClient.get()
			.uri("/api/auth/csrf-token")
			.retrieve()
			.toEntity(CsrfToken.class);
		CsrfToken body = response.getBody();
		return new CsrfResponse(body.token(), body.headerName(),
			response.getHeaders().getFirst(HttpHeaders.SET_COOKIE));
	}

	private record UserData(UUID id, String name, String email, String password) {

	}

	private record CsrfResponse(String token, String headerName, String cookie) {

	}
}
