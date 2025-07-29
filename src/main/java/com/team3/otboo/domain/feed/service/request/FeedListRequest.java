package com.team3.otboo.domain.feed.service.request;

import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.util.UUID;
import org.hibernate.query.SortDirection;

public record FeedListRequest(
	String cursor,
	UUID idAfter,
	int limit,
	String sortBy,
	SortDirection sortDirection,
	String keywordLike,
	SkyStatus skyStatusEqual,
	PrecipitationType precipitationTypeEqual,
	UUID authorIdEqual
) {

}
