package com.team3.otboo.domain.feed.comment.api;

import com.team3.otboo.domain.feed.dto.CommentDto;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class CommentApiTest {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Test
	void getCommentsInfiniteScroll() {
		String feedId = "c2ea31cf-c89a-4782-9e85-0401da7e6043"; // feed id 꼭 넣어줘야함 .

		CommentDtoCursorResponse response = restClient.get()
			.uri("/api/feeds/{feedId}/comments?limit=30", feedId)
			.retrieve()
			.body(CommentDtoCursorResponse.class);

		System.out.println("[First Page]");
		List<CommentDto> firstPage = response.getData();
		for (CommentDto commentDto : firstPage) {
			System.out.println("content: " + commentDto.content());
		}

		if (firstPage.isEmpty()) {
			System.out.println("댓글이 없습니다.");
			return;
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
