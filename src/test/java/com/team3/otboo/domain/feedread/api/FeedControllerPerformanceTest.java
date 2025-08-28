package com.team3.otboo.domain.feedread.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;

@SpringBootTest
@AutoConfigureMockMvc
public class FeedControllerPerformanceTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	private static final int PAGE_SIZE = 20;
	private static final int TOTAL_PAGES_TO_FETCH = 10;

	@Test
	@DisplayName("API 호출 - Redis 최신순 무한 스크롤 성능 측정")
		// 미리 만들어둔 테스트용 사용자 계정으로 로그인한 상태를 만듦
	void performanceTest_RedisApiInfiniteScroll() throws Exception {
		System.out.println("============== API(Redis) 성능 테스트 시작 ==============");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String cursor = null;
		String idAfter = null;

		for (int i = 0; i < TOTAL_PAGES_TO_FETCH; i++) {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("limit", String.valueOf(PAGE_SIZE));
			if (cursor != null) {
				params.add("cursor", cursor);
			}
			if (idAfter != null) {
				params.add("idAfter", idAfter);
			}

			MvcResult result = mockMvc.perform(get("/api/feeds/redis").params(params))
				.andExpect(status().isOk())
				.andReturn();

			String jsonResponse = result.getResponse().getContentAsString();
			FeedDtoCursorResponse response = objectMapper.readValue(jsonResponse,
				FeedDtoCursorResponse.class);

			if (!response.hasNext()) {
				System.out.println("더 이상 데이터가 없어 테스트를 중단합니다.");
				break;
			}
			cursor = response.nextCursor();
			idAfter = response.nextIdAfter() != null ? response.nextIdAfter().toString() : null;
		}

		stopWatch.stop();
		System.out.printf(">> API(Redis) 총 실행 시간: %d ms%n", stopWatch.getTotalTimeMillis());
		System.out.println("======================================================");
	}

	@Test
	@DisplayName("API 호출 - Elasticsearch 좋아요순 무한 스크롤 성능 측정")
	@WithUserDetails("testuser@example.com")
		// 로그인한 상태로 테스트
	void performanceTest_ElasticsearchApiInfiniteScroll() throws Exception {
		System.out.println("=========== API(Elasticsearch) 성능 테스트 시작 ===========");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String cursor = null;
		String idAfter = null;

		for (int i = 0; i < TOTAL_PAGES_TO_FETCH; i++) {
			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("limit", String.valueOf(PAGE_SIZE));
			params.add("sortBy", "likeCount"); // 좋아요순 정렬
			if (cursor != null) {
				params.add("cursor", cursor);
			}
			if (idAfter != null) {
				params.add("idAfter", idAfter);
			}

			MvcResult result = mockMvc.perform(get("/api/feeds/elasticsearch").params(params))
				.andExpect(status().isOk())
				.andReturn();

			String jsonResponse = result.getResponse().getContentAsString();
			FeedDtoCursorResponse response = objectMapper.readValue(jsonResponse,
				FeedDtoCursorResponse.class);

			if (!response.hasNext()) {
				System.out.println("더 이상 데이터가 없어 테스트를 중단합니다.");
				break;
			}
			cursor = response.nextCursor();
			idAfter = response.nextIdAfter() != null ? response.nextIdAfter().toString() : null;
		}

		stopWatch.stop();
		System.out.printf(">> API(Elasticsearch) 총 실행 시간: %d ms%n", stopWatch.getTotalTimeMillis());
		System.out.println("=======================================================");
	}
}
