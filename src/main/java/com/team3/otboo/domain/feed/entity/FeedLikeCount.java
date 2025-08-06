package com.team3.otboo.domain.feed.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "feed_like_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedLikeCount {

	@Id
	private UUID feedId;
	private Long likeCount;

	public static FeedLikeCount init(UUID feedId, Long likeCount) {
		FeedLikeCount feedLikeCount = new FeedLikeCount();
		feedLikeCount.feedId = feedId;
		feedLikeCount.likeCount = likeCount;
		return feedLikeCount;
	}
}
