package com.team3.otboo.domain.feed.entity;


import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "likes", uniqueConstraints = {
	@UniqueConstraint(
		name = "idx_feed_id_user_id",
		columnNames = {"feed_id", "user_id"}
	)
})
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Like extends BaseEntity {

	@Column(nullable = false)
	private UUID feedId;

	@Column(nullable = false)
	private UUID userId;

	public static Like create(UUID feedId, UUID userId) {
		Like like = new Like();
		like.feedId = feedId;
		like.userId = userId;

		return like;
	}
}
