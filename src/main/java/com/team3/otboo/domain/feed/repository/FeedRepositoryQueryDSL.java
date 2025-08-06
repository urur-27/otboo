package com.team3.otboo.domain.feed.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.QFeed;
import com.team3.otboo.domain.feed.entity.QFeedLikeCount;
import com.team3.otboo.domain.feed.entity.QOotd;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.user.enums.SortDirection;
import com.team3.otboo.domain.weather.entity.QWeather;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryQueryDSL {

	private final JPAQueryFactory queryFactory;

	private final QFeed feed = QFeed.feed;
	private final QOotd ootd = QOotd.ootd;
	private final QWeather weather = QWeather.weather;
	private final QFeedLikeCount feedLikeCount = QFeedLikeCount.feedLikeCount;

	public List<Feed> getFeeds(FeedListRequest req) {

		return queryFactory
			.selectFrom(feed)
			.join(ootd).on(feed.id.eq(ootd.feedId))
			.join(weather).on(feed.weatherId.eq(weather.id))
			.leftJoin(feedLikeCount).on(feed.id.eq(feedLikeCount.feedId))
			.where(
				keywordLike(req.keywordLike()),
				skyStatusEq(req.skyStatusEqual()),
				precipitationEq(req.precipitationTypeEqual()),
				authorEq(req.authorIdEqual()),
				cursor(req.cursor(), req.idAfter(),
					req.sortBy(), req.sortDirection())
			)
			.orderBy(sortOrder(req.sortBy(), req.sortDirection()))
			.limit(req.limit() + 1)
			.fetch();
	}

	public int countFeeds(FeedListRequest req) {

		Long cnt = queryFactory
			.select(feed.id.countDistinct())
			.from(feed)
			.join(ootd).on(feed.id.eq(ootd.feedId))
			.join(weather).on(feed.weatherId.eq(weather.id))
			// precipitation 조인 제거
			.leftJoin(feedLikeCount).on(feed.id.eq(feedLikeCount.feedId))
			.where(
				keywordLike(req.keywordLike()),
				skyStatusEq(req.skyStatusEqual()),
				precipitationEq(req.precipitationTypeEqual()),
				authorEq(req.authorIdEqual())
			)
			.fetchOne();

		return cnt == null ? 0 : Math.toIntExact(cnt);
	}

	private BooleanExpression keywordLike(String keyword) {
		return (keyword == null || keyword.isBlank())
			? null
			: feed.content.likeIgnoreCase("%" + keyword + "%");
	}

	private BooleanExpression skyStatusEq(SkyStatus status) {
		return status == null ? null : weather.skyStatus.eq(status);
	}

	private BooleanExpression precipitationEq(PrecipitationType type) {
		return type == null ? null : weather.precipitation.type.eq(type);
	}

	private BooleanExpression authorEq(UUID authorId) {
		return authorId == null ? null : feed.authorId.eq(authorId);
	}

	private BooleanExpression cursor(String cursor,
		UUID idAfter,
		String sortBy,
		SortDirection dir) {

		if (cursor == null || cursor.isBlank()) {
			return null;
		}

		if ("createdAt".equals(sortBy)) {
			Instant time = Instant.parse(cursor);

			return dir == SortDirection.DESCENDING
				? feed.createdAt.lt(time)
				.or(feed.createdAt.eq(time)
					.and(idAfter != null ? feed.id.lt(idAfter) : null))
				: feed.createdAt.gt(time)
					.or(feed.createdAt.eq(time)
						.and(idAfter != null ? feed.id.gt(idAfter) : null));
		}

		if ("likeCount".equals(sortBy)) {
			Long likeCnt = Long.parseLong(cursor);

			return dir == SortDirection.DESCENDING
				? feedLikeCount.likeCount.lt(likeCnt)
				.or(feedLikeCount.likeCount.eq(likeCnt)
					.and(idAfter != null ? feed.id.lt(idAfter) : null))
				: feedLikeCount.likeCount.gt(likeCnt)
					.or(feedLikeCount.likeCount.eq(likeCnt)
						.and(idAfter != null ? feed.id.gt(idAfter) : null));
		}

		return null;
	}

	private OrderSpecifier<?> sortOrder(String sortBy, SortDirection dir) {

		boolean desc = dir == SortDirection.DESCENDING;

		if ("createdAt".equals(sortBy)) {
			return desc ? feed.createdAt.desc()
				: feed.createdAt.asc();
		}

		if ("likeCount".equals(sortBy)) {
			return desc ? feedLikeCount.likeCount.coalesce(0L).desc()
				: feedLikeCount.likeCount.coalesce(0L).asc();
		}

		return feed.createdAt.desc();
	}
}
