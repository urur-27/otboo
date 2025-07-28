package com.team3.otboo.domain.feed.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

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

	@Column(nullable = false)
	private int likeCount;

	@Column(nullable = false)
	private int commentCount;

	public static Feed create(UUID authorId, UUID weatherId, String content) {
		Feed feed = new Feed();
		feed.authorId = authorId;
		feed.weatherId = weatherId;
		feed.content = content;

		return feed;
	}
}
