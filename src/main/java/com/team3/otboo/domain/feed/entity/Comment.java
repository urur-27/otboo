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

@Table(name = "comments")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@Column(nullable = false)
	private UUID feedId;

	@Column(nullable = false)
	private UUID authorId;

	@Column(nullable = false)
	private String content;

	public static Comment create(UUID feedId, UUID authorId, String content){
		Comment comment = new Comment();
		comment.feedId = feedId;
		comment.authorId = authorId;
		comment.content = content;

		return comment;
	}
}
