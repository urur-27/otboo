package com.team3.otboo.domain.feedread.document;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
//@Document(collection = "feeds_staging") // 데이터를 저장할 MongoDB 컬렉션 이름
public class MongoFeedDocument {

	@Id // MongoDB의 고유 ID 필드
	private String id; // String 타입으로 사용하는 것이 일반적

	private Instant createdAt;
	private String content;
	private UUID authorId;
	private String authorName;
	private String skyStatus;
	private String precipitationType;
	private Integer likeCount;

	// elastic search document 를 MongoDB document 로 변환
	public static MongoFeedDocument from(FeedDocument esDocument) {
		MongoFeedDocument mongoDoc = new MongoFeedDocument();
		mongoDoc.setId(esDocument.getId().toString());
		mongoDoc.setCreatedAt(esDocument.getCreatedAt());
		mongoDoc.setContent(esDocument.getContent());
		mongoDoc.setAuthorId(esDocument.getAuthorId());
		mongoDoc.setSkyStatus(esDocument.getSkyStatus().name());
		mongoDoc.setPrecipitationType(esDocument.getPrecipitationType().name());
		mongoDoc.setLikeCount(esDocument.getLikeCount());
		return mongoDoc;
	}
}
