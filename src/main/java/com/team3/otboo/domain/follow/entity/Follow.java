package com.team3.otboo.domain.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(
	name = "follows",
	uniqueConstraints = {
		@UniqueConstraint(
			columnNames = {"followeeId", "followerId"}
		)
	}
)
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@CreatedDate
	private Instant createdAt;

	@Column(nullable = false)
	private UUID followeeId;

	@Column(nullable = false)
	private UUID followerId;

	public static Follow create(UUID followeeId, UUID followerId) {
		Follow follow = new Follow();
		follow.followeeId = followeeId;
		follow.followerId = followerId;

		return follow;
	}
}
