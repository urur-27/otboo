package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.entity.Like;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;

	private final FeedDtoAssembler feedDtoAssembler;

	@Transactional
	public FeedDto like(UUID userId, UUID feedId) {
		likeRepository.save(
			Like.create(
				feedId,
				userId)
		);

		int result = feedLikeCountRepository.increase(feedId);
		if (result == 0) { // 여기서 스레드 두개가 if 문 안으로 들어가서 동시성 문제 생길 수 있음 .
			feedLikeCountRepository.save(FeedLikeCount.init(feedId, 1L));
		}

		// likeCount 를 바로 사용하지 못하고 DB에 저장했다가 저장한걸 꺼내는 식으로 구현 되어 있음.
		// assemble 메서드의 파라미터로 likeCount 를 넣어주면 진짜 조금이라도 더 성능이 좋아지지 않을까 .
		// 좋아요 누르면 FeedDto 를 반환 .. FeedDto 를 만드는게 생각보다 무거운 작업인데 FeedDto 를 반환해야할까 ? 이 부분 개선해도 좋을 것 같음 .
		return feedDtoAssembler.assemble(feedId, userId);
	}

	@Transactional
	public void unlike(UUID userId, UUID feedId) {
		likeRepository.deleteByUserIdAndFeedId(userId, feedId);
		feedLikeCountRepository.decrease(feedId);
	}

	@Transactional
	public void deleteAllByFeedId(UUID feedId) {
		likeRepository.deleteAllByFeedId(feedId);
	}

	// 테스트 데이터 삽입용 메서드
	@Transactional
	public Like createBulk(UUID userId, UUID feedId) {
		Like like = likeRepository.save(
			Like.create(feedId, userId)
		);

		int result = feedLikeCountRepository.increase(feedId);
		if (result == 0) {
			// 트래픽이 몰리면 이 구문 안에 두개의 스레드가 들어와 데이터가 유실될 수도 있음 . todo: (미리 초기화 해놓기)
			feedLikeCountRepository.save(FeedLikeCount.init(feedId, 1L));
		}

		return like;
	}

	public Long count(UUID feedId) {
		return feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);
	}
}
