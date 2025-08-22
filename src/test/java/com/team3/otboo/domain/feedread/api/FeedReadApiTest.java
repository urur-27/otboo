package com.team3.otboo.domain.feedread.api;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.feedread.service.response.FeedReadResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class FeedReadApiTest {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Test
	void readTest() {
		FeedReadResponse response = restClient.get()
			.uri("/api/feeds/{feedId}")
			.retrieve()
			.body(FeedReadResponse.class);

		System.out.println("response = " + response);
	}

	@Test
	void readAllTest() {
		// 첫페이지 조회 .
		FeedDtoCursorResponse response = restClient.get()
			.uri("/api/feeds?limit=%s".formatted(10))
			.retrieve()
			.body(FeedDtoCursorResponse.class);

		List<FeedDto> dtos1 = response.data();
		System.out.println("[FirstPage]");
		for (FeedDto feedDto : dtos1) {
			System.out.println("id: " + feedDto.id());
		}

		FeedDto feedDto = dtos1.getLast();
		String cursor = feedDto.createdAt().toString();

		FeedDtoCursorResponse response2 = restClient.get()
			.uri("/api/feeds?limit=%s&cursor=%s".formatted(10, cursor))
			.retrieve()
			.body(FeedDtoCursorResponse.class);

		List<FeedDto> dtos2 = response2.data();
		System.out.println("[NextPage]");
		for (FeedDto dto : dtos2) {
			System.out.println("id: " + dto.id());
		}
	}
}
