package com.team3.otboo.domain.feedread.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.user.dto.CsrfToken;
import com.team3.otboo.domain.user.dto.Request.SignInRequest;
import com.team3.otboo.domain.user.dto.Request.UserCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;


public class UserFlowIntegrationTest {

	@LocalServerPort
	private int port;

	private RestClient restClient;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		this.restClient = RestClient.builder()
			.baseUrl("http://localhost:8080")
			.build();
	}

	@Test
	void test() throws Exception {
		// 1. CSRF 토큰 발급
		ResponseEntity<CsrfToken> csrfResponse = restClient.get()
			.uri("/api/auth/csrf-token")
			.retrieve()
			.toEntity(CsrfToken.class);

		String csrfToken = csrfResponse.getBody().token();
		String csrfHeaderName = csrfResponse.getBody().headerName();
		String csrfCookie = csrfResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

		// 2. 회원가입
		UserCreateRequest signUpRequest = new UserCreateRequest(
			"user-" + System.currentTimeMillis(),
			"user-" + System.currentTimeMillis() + "@test.com",
			"password123!"
		);

		restClient.post()
			.uri("/api/users")
			.contentType(MediaType.APPLICATION_JSON)
			.header(csrfHeaderName, csrfToken)
			.header("Cookie", csrfCookie)
			.body(signUpRequest)
			.retrieve()
			.toBodilessEntity();

		System.out.println("\n>> 회원가입 성공: " + signUpRequest.email());

		// 3. 로그인
		SignInRequest signInRequest = new SignInRequest(signUpRequest.email(),
			signUpRequest.password());

		ResponseEntity<String> signInResponse = restClient.post()
			.uri("/api/auth/sign-in")
			.contentType(MediaType.APPLICATION_JSON)
			.header(csrfHeaderName, csrfToken)
			.header("Cookie", csrfCookie)
			.body(signInRequest)
			.retrieve()
			.toEntity(String.class);

		String accessToken = objectMapper.readValue(signInResponse.getBody(), String.class);
		String refreshTokenCookie = signInResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

		// 4. 피드 조회
		// 첫번째 페이지 조회
		FeedDtoCursorResponse firstPageResponse = restClient.get()
			.uri("/api/feeds?limit=10")
			.header("Authorization", "Bearer " + accessToken)
			.header("Cookie", refreshTokenCookie)
			.retrieve()
			.body(FeedDtoCursorResponse.class);

		System.out.println("[First Page]");
		firstPageResponse.data().forEach(feed -> System.out.println("feed id: " + feed.id()));
		System.out.println("조회된 개수: " + firstPageResponse.data().size());
		System.out.println("hasNext: " + firstPageResponse.hasNext());

		if (firstPageResponse.hasNext()) {
			String nextCursor = firstPageResponse.nextCursor();
			String nextIdAfter = firstPageResponse.nextIdAfter().toString();

			FeedDtoCursorResponse nextPageResponse = restClient.get()
				.uri("/api/feeds?limit=10&cursor={cursor}&idAfter={idAfter}", nextCursor,
					nextIdAfter)
				.header("Authorization", "Bearer " + accessToken)
				.header("Cookie", refreshTokenCookie)
				.retrieve()
				.body(FeedDtoCursorResponse.class);

			System.out.println("\n[다음 페이지 피드 목록]");
			nextPageResponse.data().forEach(feed -> System.out.println("피드 ID: " + feed.id()));
			System.out.println("조회된 개수: " + nextPageResponse.data().size());
		}
	}
}
