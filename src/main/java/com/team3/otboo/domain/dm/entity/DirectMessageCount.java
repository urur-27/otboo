package com.team3.otboo.domain.dm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "direct_message_count")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DirectMessageCount {

	@Id
	private String dmKey;
	private Long directMessageCount;

	public static DirectMessageCount init(String dmKey, Long count) {
		DirectMessageCount dmCount = new DirectMessageCount();
		dmCount.dmKey = dmKey;
		dmCount.directMessageCount = count;
		return dmCount;
	}
}
