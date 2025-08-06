package com.team3.otboo.domain.feed.entity;

import com.team3.otboo.domain.base.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "ootds")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ootd extends BaseEntity {

	private UUID feedId;

	private UUID clothesId;

	public static Ootd create(UUID feedId, UUID clothesId) {
		Ootd ootd = new Ootd();
		ootd.feedId = feedId;
		ootd.clothesId = clothesId;

		return ootd;
	}
}
