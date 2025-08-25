package com.team3.otboo.domain.feedread.service;

import com.team3.otboo.common.event.Event;
import com.team3.otboo.common.event.EventPayload;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.feed.service.request.FeedListRequest;
import com.team3.otboo.domain.feed.service.response.FeedDtoCursorResponse;
import com.team3.otboo.domain.feedread.client.FeedClient;
import com.team3.otboo.domain.feedread.repository.FeedIdListRepository;
import com.team3.otboo.domain.feedread.repository.FeedQueryModel;
import com.team3.otboo.domain.feedread.repository.FeedQueryModelRepository;
import com.team3.otboo.domain.feedread.service.event.handler.EventHandler;
import com.team3.otboo.domain.feedread.service.response.FeedReadResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedReadService {

	private final FeedClient feedClient;

	private final FeedQueryModelRepository feedQueryModelRepository;
	private final FeedIdListRepository feedIdListRepository;
	private final FeedCountRepository feedCountRepository;

	private final LikeRepository likeRepository; // likeByMe 를 위한 likeRepository
	private final List<EventHandler> eventHandlers;
	private final FeedCommentCountRepository feedCommentCountRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;

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
		log.info("[FeedReadService.fetch] fetch data. feedId={}", feedId);
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
}
