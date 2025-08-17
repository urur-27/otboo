package com.team3.otboo.domain.feed.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "feed_view_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedViewCount {

	@Id
	private UUID feedId;
	private Long viewCount;

	public static FeedViewCount init(UUID feedId, Long viewCount) {
		FeedViewCount feedViewCount = new FeedViewCount();
		feedViewCount.feedId = feedId;
		feedViewCount.viewCount = viewCount;
		return feedViewCount;
	}
}
