package com.team3.otboo.domain.follow.entity;

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

@Table(name = "follows")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@CreatedDate
	private LocalDateTime createdAt;

	@Column(nullable =false)
	private UUID followeeId;

	@Column(nullable = false)
	private UUID followerId;

	public static Follow create(UUID followeeId, UUID followerId){
		Follow follow = new Follow();
		follow.followeeId = followeeId;
		follow.followerId = followerId;

		return follow;
	}
}
