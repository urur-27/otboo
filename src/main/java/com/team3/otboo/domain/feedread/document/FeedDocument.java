package com.team3.otboo.domain.feedread.document;

import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Getter
@Setter
@Document(indexName = "feeds")
public class FeedDocument {

	@Id
	private UUID id; // feed_id

	@Field(type = FieldType.Date)
	private Instant createdAt;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "nori"),
		otherFields = {
			@InnerField(suffix = "raw", type = FieldType.Keyword, ignoreAbove = 256)
		}
	)
	private String content;

	@Field(type = FieldType.Keyword)
	private UUID authorId;

	@MultiField(
		mainField = @Field(type = FieldType.Text, analyzer = "nori"),
		otherFields = {
			@InnerField(suffix = "raw", type = FieldType.Keyword, ignoreAbove = 256)
		}
	)
	private String authorName;

	@Field(type = FieldType.Keyword)
	private SkyStatus skyStatus; // 날씨

	@Field(type = FieldType.Keyword)
	private PrecipitationType precipitationType; // 강수

	@Field(type = FieldType.Integer)
	private Integer likeCount;
}
