package com.team3.otboo.domain.feed.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "feed_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeedCount {

	@Id
	private Long id;
	private Long feedCount;

	public static final Long SINGLETON_ID = 1L;

	public static FeedCount init(Long feedCount) {
		FeedCount count = new FeedCount();
		count.id = SINGLETON_ID;
		count.feedCount = feedCount;
		return count;
	}
}
