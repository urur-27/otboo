package com.team3.otboo.domain.feed.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feed extends BaseEntity {

	@Column(nullable = false)
	private UUID authorId;

	@Column(nullable = false)
	private UUID weatherId;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	// 엔티티 카운트 필드에 메모리에서 증가시키는 방식을 말고, 데이터 베이스 수준 lock 을 사용해야함 .
//	@Column(nullable = false)
//	private int likeCount;
//
//	@Column(nullable = false)
//	private int commentCount;

	public static Feed create(UUID authorId, UUID weatherId, String content) {
		Feed feed = new Feed();
		feed.authorId = authorId;
		feed.weatherId = weatherId;
		feed.content = content;

		return feed;
	}
}
