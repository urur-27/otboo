package com.team3.otboo.domain.feed.comment.api;

import com.team3.otboo.domain.feed.dto.CommentDto;
import java.util.List;
import java.util.UUID;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class CommentApiTest {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Test
	void getCommentsInfiniteScroll() {
		String feedId = "488846cf-c5c6-4ec4-938a-746a9572d5a1"; // feed id 꼭 넣어줘야함 .

		CommentDtoCursorResponse response = restClient.get()
			.uri("/api/feeds/{feedId}/comments?limit=30", feedId)
			.retrieve()
			.body(CommentDtoCursorResponse.class);

		System.out.println("[First Page]");
		List<CommentDto> firstPage = response.getData();
		for (CommentDto commentDto : firstPage) {
			System.out.println("content: " + commentDto.content());
		}
		CommentDto lastCommentDto = firstPage.getLast();
		String lastCreatedAt = lastCommentDto.createdAt().toString();
		String lastCommentId = lastCommentDto.id().toString();

		// 두번째 페이지 가져오기
		System.out.println("[Next Page]");
		CommentDtoCursorResponse response2 = restClient.get()
			.uri("/api/feeds/{feedId}/comments?cursor=%s&idAfter=%s&limit=30"
				.formatted(lastCreatedAt, lastCommentId), feedId)
			.retrieve()
			.body(CommentDtoCursorResponse.class);

		List<CommentDto> nextPage = response2.getData();
		for (CommentDto commentDto : nextPage) {
			System.out.println("content: " + commentDto.content());
		}
	}

	@Data
	public static class CommentDtoCursorResponse {

		private List<CommentDto> data;
		private String nextCursor;
		private UUID nextIdAfter;
		private boolean hasNext;
		private int totalCount;
		private String sortBy;
		private SortDirection sortDirection;
	}
}
