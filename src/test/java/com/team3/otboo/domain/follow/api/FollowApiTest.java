package com.team3.otboo.domain.follow.api;


import com.team3.otboo.domain.follow.dto.FollowDto;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

public class FollowApiTest {

	RestClient restClient = RestClient.create("http://localhost:8080");

	@Test
	void getFollowersInfiniteScroll() {
		// 실제 데이터베이스에 있는 userId 세팅해야함 .
		String userId = "c3bea995-e8de-49db-91b8-7239934c2c82";

		// 첫번째 페이지 ..
		FollowListResponse response = restClient.get()
			.uri("/api/follows/followers?followeeId=%s&limit=10"
				.formatted(userId))
			.retrieve()
			.body(FollowListResponse.class);

		System.out.println("[First Page]");
		List<FollowDto> data = response.getData();
		for (FollowDto followDto : data) {
			System.out.println("follower: " + followDto.follower().userId());
		}

		System.out.println("response: " + response);

		String nextCursor = response.getNextCursor();
		UUID nextIdAfter = response.getNextIdAfter();

		System.out.println("[Next Page]");
		FollowListResponse nextPage = restClient.get()
			.uri("/api/follows/followers?followeeId=%s&limit=10&cursor=%s&idAfter=%s"
				.formatted(userId, nextCursor, nextIdAfter))
			.retrieve()
			.body(FollowListResponse.class);

		List<FollowDto> data2 = nextPage.getData();
		for (FollowDto followDto : data2) {
			System.out.println("follower: " + followDto.follower().userId());
		}
	}

	@Test
	void getFollowingsInfiniteScroll() {
		String followerId = "e0027086-4823-4a25-9721-6483cfc89456";

		FollowListResponse response = restClient.get()
			.uri("/api/follows/followings?followerId=%s&limit=10".formatted(followerId))
			.retrieve()
			.body(FollowListResponse.class);

		List<FollowDto> data = response.getData();
		System.out.println("[First Page]");
		for (FollowDto followDto : data) {
			System.out.println("following id: " + followDto.followee().userId());
		}

		System.out.println("next cursor: " + response.getNextCursor());
		System.out.println("next idAfter: " + response.getNextIdAfter());
		String nextCursor = response.getNextCursor();
		UUID nextIdAfter = response.getNextIdAfter();

		FollowListResponse nextPage = restClient.get()
			.uri("/api/follows/followings?followerId=%s&limit=10&cursor=%s&idAfter=%s"
				.formatted(followerId, nextCursor, nextIdAfter))
			.retrieve()
			.body(FollowListResponse.class);

		List<FollowDto> data2 = nextPage.getData();
		System.out.println("[Next Page]");
		for (FollowDto followDto : data2) {
			System.out.println("following id: " + followDto.followee().userId());
		}
	}

	@Data
	public static class FollowListResponse {

		List<FollowDto> data; // follower 검색, following 검색 모두 그냥 follow dto 를 보냄 .
		String nextCursor;
		UUID nextIdAfter;
		boolean hasNext;
		int totalCount;
		String sortBy;
		SortDirection sortDirection;
	}
}
