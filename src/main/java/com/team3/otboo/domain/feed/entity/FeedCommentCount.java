package com.team3.otboo.domain.feed.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "feed_comment_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedCommentCount {

	@Id
	private UUID feedId;
	private Long commentCount;

	public static FeedCommentCount init(UUID feedId, Long commentCount) {
		FeedCommentCount feedCommentCount = new FeedCommentCount();
		feedCommentCount.feedId = feedId;
		feedCommentCount.commentCount = commentCount;
		return feedCommentCount;
	}
}
