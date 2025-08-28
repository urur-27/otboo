package com.team3.otboo.domain.feedread.service;

import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feedread.document.FeedDocument;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedIndexBatchService {

	private static final int CHUNK_SIZE = 1000; // 한 번에 처리할 데이터 양
	private static final String FEED_ALIAS = "feeds"; // 애플리케이션이 사용할 고정 별칭

	private final FeedRepository feedRepository;
	private final WeatherRepository weatherRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;
	private final ElasticsearchOperations elasticsearchOperations;
	private final UserRepository userRepository; // 사용자 정보 조회를 위해 UserRepository 추가

	/**
	 * 매일 새벽 1시에 실행되는 전체 색인 스케줄러 (Alias를 활용한 무중단 방식).
	 */
	@Scheduled(cron = "0 0 1 * * *")
	@Transactional(readOnly = true)
	public void runFullIndexingWithAlias() {
		log.info(">>>>> [BATCH] 무중단 전체 색인 작업을 시작합니다.");

		String newIndexName = FEED_ALIAS + "_" + LocalDateTime.now()
			.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
		IndexCoordinates newIndexCoordinates = IndexCoordinates.of(newIndexName);

		IndexOperations indexOperations = elasticsearchOperations.indexOps(newIndexCoordinates);
		indexOperations.create();
		indexOperations.putMapping(FeedDocument.class);
		log.info(">>>>> [BATCH] 새로운 인덱스 '{}'를 생성했습니다.", newIndexName);

		Pageable pageable = PageRequest.of(0, CHUNK_SIZE);
		long totalFeedsProcessed = 0;

		while (true) {
			Page<Feed> feedPage = feedRepository.findAll(pageable);
			List<Feed> feeds = feedPage.getContent();
			if (feeds.isEmpty()) {
				break;
			}

			processChunk(feeds, newIndexCoordinates);

			totalFeedsProcessed += feeds.size();
			log.info(">>>>> [BATCH] {}개의 피드를 '{}' 인덱스에 색인 완료. (총 {}개 처리)", feeds.size(),
				newIndexName, totalFeedsProcessed);

			if (!feedPage.hasNext()) {
				break;
			}
			pageable = feedPage.nextPageable();
		}

		AliasActions aliasActions = new AliasActions();

		Set<String> oldIndexNames = elasticsearchOperations.indexOps(
			IndexCoordinates.of(FEED_ALIAS)).getAliasesForIndex("*").keySet();
		oldIndexNames.forEach(oldIndex -> {
			AliasActionParameters removeParams = AliasActionParameters.builder()
				.withAliases(FEED_ALIAS)
				.withIndices(oldIndex)
				.build();
			aliasActions.add(new AliasAction.Remove(removeParams));
		});

		AliasActionParameters addParams = AliasActionParameters.builder()
			.withAliases(FEED_ALIAS)
			.withIndices(newIndexName)
			.build();
		aliasActions.add(new AliasAction.Add(addParams));

		if (!aliasActions.getActions().isEmpty()) {
			elasticsearchOperations.indexOps(newIndexCoordinates).alias(aliasActions);
		}
		log.info(">>>>> [BATCH] 별칭 '{}'가 새로운 인덱스 '{}'를 가리키도록 교체했습니다.", FEED_ALIAS, newIndexName);

		oldIndexNames.forEach(oldIndex -> {
			elasticsearchOperations.indexOps(IndexCoordinates.of(oldIndex)).delete();
			log.info(">>>>> [BATCH] 옛날 인덱스 '{}'를 삭제했습니다.", oldIndex);
		});

		log.info(">>>>> [BATCH] 총 {}개의 피드 전체 색인 완료. 작업을 종료합니다.", totalFeedsProcessed);
	}

	private void processChunk(List<Feed> feeds, IndexCoordinates indexCoordinates) {
		List<UUID> feedIds = feeds.stream().map(Feed::getId).collect(Collectors.toList());
		List<UUID> weatherIds = feeds.stream().map(Feed::getWeatherId).collect(Collectors.toList());
		List<UUID> authorIds = feeds.stream().map(Feed::getAuthorId)
			.collect(Collectors.toList()); // 작성자 ID 목록 추출

		Map<UUID, Weather> weatherMap = weatherRepository.findAllByIdIn(weatherIds).stream()
			.collect(Collectors.toMap(Weather::getId, Function.identity()));

		Map<UUID, FeedLikeCount> likeCountMap = feedLikeCountRepository.findAllByIdIn(feedIds)
			.stream()
			.collect(Collectors.toMap(FeedLikeCount::getFeedId, Function.identity()));

		// 작성자 ID 목록으로 사용자 정보 조회
		Map<UUID, User> userMap = userRepository.findAllByIdIn(authorIds).stream()
			.collect(Collectors.toMap(User::getId, Function.identity()));

		// buildIndexQuery 호출 시 userMap 전달
		List<IndexQuery> indexQueries = feeds.stream()
			.map(feed -> buildIndexQuery(feed, weatherMap, likeCountMap, userMap))
			.collect(Collectors.toList());

		if (!indexQueries.isEmpty()) {
			elasticsearchOperations.bulkIndex(indexQueries, indexCoordinates);
		}
	}

	// buildIndexQuery 메소드에 userMap 파라미터 추가 및 로직 수정
	private IndexQuery buildIndexQuery(Feed feed, Map<UUID, Weather> weatherMap,
		Map<UUID, FeedLikeCount> likeCountMap, Map<UUID, User> userMap) {
		FeedDocument document = new FeedDocument();
		document.setId(feed.getId().toString());
		document.setFeedId(feed.getId());
		document.setCreatedAt(feed.getCreatedAt());
		document.setContent(feed.getContent());
		document.setAuthorId(feed.getAuthorId());

		User author = userMap.get(feed.getAuthorId());
		if (author != null) {
			document.setAuthorName(author.getUsername()); // User 엔티티의 이름 필드(getUsername)를 사용
		}

		Weather weather = weatherMap.get(feed.getWeatherId());
		if (weather != null) {
			document.setSkyStatus(weather.getSkyStatus());
			document.setPrecipitationType(weather.getPrecipitation().getType());
		}

		int likeCount = likeCountMap.containsKey(feed.getId())
			? likeCountMap.get(feed.getId()).getLikeCount().intValue()
			: 0;
		document.setLikeCount(likeCount);

		return new IndexQueryBuilder()
			.withId(document.getId().toString())
			.withObject(document)
			.build();
	}
}
