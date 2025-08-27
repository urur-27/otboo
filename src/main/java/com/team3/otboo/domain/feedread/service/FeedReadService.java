package com.team3.otboo.domain.feedread.service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryStringQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.feedread.client.FeedClient;
import com.team3.otboo.domain.feedread.document.FeedDocument;
import com.team3.otboo.domain.feedread.repository.FeedIdListRepository;
import com.team3.otboo.domain.feedread.repository.FeedQueryModel;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import com.team3.otboo.domain.feedread.service.event.handler.EventHandler;
import com.team3.otboo.domain.feedread.service.response.FeedReadResponse;
import com.team3.otboo.domain.user.enums.SortDirection;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Slf4j
public class FeedReadService {

	private final FeedClient feedClient;

	private final FeedRepository feedRepository;
	private final FeedQueryModelRepository feedQueryModelRepository;
	private final FeedIdListRepository feedIdListRepository;
	private final FeedCountRepository feedCountRepository;

	private final FeedDtoAssembler feedDtoAssembler;

	private final LikeRepository likeRepository; // likeByMe 를 위한 likeRepository
	private final List<EventHandler> eventHandlers;
	private final FeedCommentCountRepository feedCommentCountRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;

	private final ElasticsearchOperations elasticsearchOperations;

	public void handleEvent(Event<EventPayload> event) {
		for (EventHandler eventHandler : eventHandlers) {
			if (eventHandler.supports(event)) {
				eventHandler.handle(event);
			}
		}
	}

	public FeedReadResponse read(UUID feedId, UUID userId) {
		// redis 에 있으면 redis 에서 가져오고, 만약 없으면 RDBMS 에서 가져오기 .
		FeedQueryModel feedQueryModel = feedQueryModelRepository.read(feedId)
			.or(() -> fetch(feedId, userId))
			.orElseThrow(() -> new EntityNotFoundException("feed not found feedId: " + feedId));

		return FeedReadResponse.from(
			feedQueryModel,
			likeRepository.existsByUserIdAndFeedId(userId, feedId)
		);
	}

	private Optional<FeedQueryModel> fetch(UUID feedId, UUID userId) {
		Optional<FeedQueryModel> feedQueryModelOptional = feedClient.read(feedId, userId)
			.map(FeedQueryModel::create);// RDBMS 에서 가져오기 .

		// repository 에 저장하기 .
		feedQueryModelOptional
			.ifPresent(feedQueryModel -> feedQueryModelRepository.create(
				feedQueryModel, Duration.ofDays(1) // 일단 하루만 저장 .
			));
		return feedQueryModelOptional;
	}

	public FeedDtoCursorResponse readAllInfiniteScroll(UUID userId, FeedListRequest request) {

		int requestLimit = request.limit();
		List<UUID> ids = readAllInfiniteScrollFeedIds(userId, request, requestLimit + 1);
		List<FeedDto> data = readAll(ids, userId);

		boolean hasNext = data.size() > requestLimit;
		if (hasNext) {
			data = data.subList(0, requestLimit);
		}

		if (data.isEmpty()) {
			return new FeedDtoCursorResponse(
				Collections.emptyList(),
				null,
				null,
				false,
				0,
				request.sortBy(), request.sortDirection()
			);
		}

		String nextCursor = data.getLast().createdAt().toString();
		UUID nextIdAfter = data.getLast().id();

		long totalCount = feedCountRepository.findById(FeedCount.SINGLETON_ID)
			.map(FeedCount::getFeedCount)
			.orElse(0L);

		return new FeedDtoCursorResponse(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			(int) totalCount,
			request.sortBy(),
			request.sortDirection()
		);
	}

	private List<FeedDto> readAll(List<UUID> feedIds, UUID userId) {
		List<FeedQueryModel> feedQueryModels = feedIds.stream()
			.map(feedId -> feedQueryModelRepository.read(feedId)
				.orElseGet(() -> fetch(feedId, userId).orElse(null)))
			.filter(Objects::nonNull)
			.toList();

		if (feedQueryModels.isEmpty()) {
			return Collections.emptyList();
		}

		List<UUID> fetchedFeedIds = feedQueryModels.stream()
			.map(FeedQueryModel::getId)
			.toList();

		Set<UUID> likedFeedIds = likeRepository
			.findLikedFeedIdsByUserAndFeedIn(userId, fetchedFeedIds);

		return feedQueryModels.stream()
			.map(feedQueryModel ->
				FeedDto.from(
					feedQueryModel,
					feedLikeCountRepository.findById(feedQueryModel.getId())
						.map(FeedLikeCount::getLikeCount).orElse(0L),
					feedCommentCountRepository.findById(feedQueryModel.getId())
						.map(FeedCommentCount::getCommentCount).orElse(0L),
					likedFeedIds.contains(feedQueryModel.getId())) // likedByMe
			)
			.toList();
	}

	private List<UUID> readAllInfiniteScrollFeedIds(UUID userId, FeedListRequest request,
		long limit) {
		Instant cursor = null;
		if (request.cursor() != null && !request.cursor().isBlank()) {
			cursor = Instant.parse(request.cursor());
		}

		List<UUID> feedIds = feedIdListRepository.readAllInfiniteScroll(cursor, limit);

		/////////////
		System.out.println("limit: " + limit);
		System.out.println("feedIds.size(): " + feedIds.size());
		/////////////

		if (limit == feedIds.size()) {
			log.info("[FeedReadService.readAllInfiniteScrollFeedIds] return redis data");
			return feedIds;
		}
		// hasNext 때문에 limit + 1 해서 요청해야함 .
		FeedListRequest newRequest = new FeedListRequest(
			request.cursor(),
			request.idAfter(),
			request.limit() + 1,
			request.sortBy(),
			request.sortDirection(),
			request.keywordLike(),
			request.skyStatusEqual(),
			request.precipitationTypeEqual(),
			request.authorIdEqual()
		);

		log.info("[FeedReadService.readAllInfiniteScrollFeedIds] return origin data");
		return feedClient.readAllInfiniteScroll(userId, newRequest).stream()
			.flatMap(cursorResponse -> cursorResponse.data().stream())
			.map(FeedDto::id)
			.toList();
	}

	public FeedDtoCursorResponse readAllInfiniteScrollByEs(UUID userId, FeedListRequest request) {
		String indexName = "feeds";
		boolean desc = request.sortDirection() == SortDirection.DESCENDING;
		int pageSize = request.limit() + 1;

		List<Query> must = new ArrayList<>();
		List<Query> filters = new ArrayList<>();

		if (request.keywordLike() != null && !request.keywordLike().isBlank()) {
			must.add(new QueryStringQuery.Builder()
				.fields("content", "authorName")
				.query(request.keywordLike())
				.fuzziness("AUTO")
				.build()
				._toQuery());
		} else {
			must.add(MatchAllQuery.of(m -> m)._toQuery());
		}
		if (request.authorIdEqual() != null) {
			filters.add(new TermQuery.Builder()
				.field("authorId")
				.value(request.authorIdEqual().toString())
				.build()
				._toQuery());
		}
		if (request.skyStatusEqual() != null) {
			filters.add(new TermQuery.Builder()
				.field("skyStatus")
				.value(request.skyStatusEqual().name())
				.build()
				._toQuery());
		}
		if (request.precipitationTypeEqual() != null) {
			filters.add(new TermQuery.Builder()
				.field("precipitationType")
				.value(request.precipitationTypeEqual().name())
				.build()
				._toQuery());
		}

		BoolQuery boolQuery = new BoolQuery.Builder().must(must).filter(filters).build();

		String sortByField = request.sortBy() != null ? request.sortBy() : "createdAt";
		List<SortOptions> sorts = List.of(
			SortOptions.of(s -> s.field(
				f -> f.field(sortByField).order(desc ? SortOrder.Desc : SortOrder.Asc)
			))
		);

		List<Object> searchAfter = null;
		if (request.cursor() != null && !request.cursor().isBlank()) {
			searchAfter = List.of(request.cursor());
		}

		NativeQueryBuilder queryBuilder = new NativeQueryBuilder()
			.withQuery(boolQuery._toQuery())
			.withSort(sorts)
			.withPageable(org.springframework.data.domain.PageRequest.of(0, pageSize))
			.withTrackTotalHits(true);

		if (searchAfter != null) {
			queryBuilder.withSearchAfter(searchAfter);
		}

		NativeQuery query = queryBuilder.build();

		SearchHits<FeedDocument> searchResult = elasticsearchOperations.search(
			query, FeedDocument.class, IndexCoordinates.of(indexName));

		List<org.springframework.data.elasticsearch.core.SearchHit<FeedDocument>> searchHits = searchResult.getSearchHits();

		boolean hasNext = searchHits.size() > request.limit();
		List<org.springframework.data.elasticsearch.core.SearchHit<FeedDocument>> pageHits =
			hasNext ? searchHits.subList(0, request.limit()) : searchHits;

		List<UUID> pageIds = pageHits.stream()
			.map(hit -> hit.getContent().getId())
			.toList();

		List<FeedDto> data;
		if (pageIds.isEmpty()) {
			data = Collections.emptyList();
		} else {
			Map<UUID, Feed> feedsById = feedRepository.findAllByIdIn(pageIds).stream()
				.collect(Collectors.toMap(Feed::getId, java.util.function.Function.identity()));

			data = pageIds.stream()
				.map(feedsById::get)
				.filter(Objects::nonNull)
				.map(feed -> feedDtoAssembler.assemble(feed.getId(),
					userId))
				.toList();
		}

		String nextCursor = null;
		UUID nextIdAfter = null;

		if (hasNext) {
			var lastHit = pageHits.getLast();
			List<Object> sortValues = lastHit.getSortValues();
			if (!sortValues.isEmpty()) {
				nextCursor = sortValues.get(0).toString();
			}
		}

		long totalCount = searchResult.getTotalHits();

		return new FeedDtoCursorResponse(
			data,
			nextCursor,
			nextIdAfter,
			hasNext,
			(int) totalCount,
			request.sortBy(),
			request.sortDirection()
		);
	}
}
