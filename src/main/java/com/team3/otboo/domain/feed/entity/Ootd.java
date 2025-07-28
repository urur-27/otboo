package com.team3.otboo.domain.feed.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

@Table(name = "ootds")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ootd {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@CreatedDate
	@Column(updatable = false)
	private Instant createdAt;

	private UUID feedId;

	private UUID clothesId;

	public static Ootd create(UUID feedId, UUID clothesId){
		Ootd ootd = new Ootd();
		ootd.feedId = feedId;
		ootd.clothesId = clothesId;

		return ootd;
	}
}
