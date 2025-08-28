package com.team3.otboo.domain.feedread.api;

import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.feedread.service.FeedReadService;
import com.team3.otboo.domain.user.enums.SortDirection;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

@SpringBootTest
public class FeedReadServicePerformanceTest {

	@Autowired
	private FeedReadService feedReadService;

	// 테스트용 사용자 ID (실제 DB에 존재하는 사용자의 UUID로 변경해야 합니다)
	private final UUID TEST_USER_ID = UUID.fromString("");

	private static final int PAGE_SIZE = 20;
	private static final int TOTAL_PAGES_TO_FETCH = 10;

	@Test
	@DisplayName("Redis 최신순 무한 스크롤 성능 측정")
	void performanceTest_RedisInfiniteScroll() {
		System.out.println("============== Redis 성능 테스트 시작 ==============");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String cursor = null;
		UUID idAfter = null;

		for (int i = 0; i < TOTAL_PAGES_TO_FETCH; i++) {
			FeedListRequest request = new FeedListRequest(
				cursor,
				idAfter,
				PAGE_SIZE,
				"createdAt", // Redis는 createdAt 정렬만 지원
				SortDirection.DESCENDING,
				null, null, null, null
			);

			System.out.printf("[%d/%d] 페이지 요청 중...%n", i + 1, TOTAL_PAGES_TO_FETCH);
			FeedDtoCursorResponse response = feedReadService.readAllInfiniteScroll(TEST_USER_ID,
				request);

			if (!response.hasNext()) {
				System.out.println("더 이상 데이터가 없어 테스트를 중단합니다.");
				break;
			}

			// 다음 페이지를 위한 커서 업데이트
			cursor = response.nextCursor();
			idAfter = response.nextIdAfter();
		}

		stopWatch.stop();
		System.out.printf(">> Redis 총 실행 시간: %d ms%n", stopWatch.getTotalTimeMillis());
		System.out.println("==================================================");
	}

	@Test
	@DisplayName("Elasticsearch 좋아요순 무한 스크롤 성능 측정")
	void performanceTest_ElasticsearchInfiniteScroll() {
		System.out.println("=========== Elasticsearch 성능 테스트 시작 ===========");
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String cursor = null;
		UUID idAfter = null;

		for (int i = 0; i < TOTAL_PAGES_TO_FETCH; i++) {
			FeedListRequest request = new FeedListRequest(
				cursor,
				idAfter,
				PAGE_SIZE,
				"likeCount", // ES는 다양한 정렬 지원 (좋아요순으로 테스트)
				SortDirection.DESCENDING,
				null, null, null, null
			);

			System.out.printf("[%d/%d] 페이지 요청 중...%n", i + 1, TOTAL_PAGES_TO_FETCH);
			FeedDtoCursorResponse response = feedReadService.readAllInfiniteScrollByEs(TEST_USER_ID,
				request);

			if (!response.hasNext()) {
				System.out.println("더 이상 데이터가 없어 테스트를 중단합니다.");
				break;
			}

			// 다음 페이지를 위한 커서 업데이트
			cursor = response.nextCursor();
			idAfter = response.nextIdAfter();
		}

		stopWatch.stop();
		System.out.printf(">> Elasticsearch 총 실행 시간: %d ms%n", stopWatch.getTotalTimeMillis());
		System.out.println("==================================================");
	}
}
