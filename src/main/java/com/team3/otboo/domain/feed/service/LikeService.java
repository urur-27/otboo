package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.entity.Like;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

	private final LikeRepository likeRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;

	private final FeedRepository feedRepository;
	private final UserRepository userRepository;

	public FeedDto like(UUID userId, UUID feedId) {

		Like like = likeRepository.save(
			Like.create(
				feedId,
				userId)
		);

		int result = feedLikeCountRepository.increase(feedId);
		if (result == 0) { // 여기서 스레드 두개가 if 문 안으로 들어가서 동시성 문제 생길 수 있음 .
			feedLikeCountRepository.save(FeedLikeCount.init(feedId, 1L));
		}

		// Weather 쪽 완성되면 .. feed dto assembler 로 FeedDto 만들기 .
		return null;
	}

	public void unlike(UUID userId, UUID feedId) {
		likeRepository.findByUserIdAndFeedId(userId, feedId);
		feedLikeCountRepository.decrease(feedId);
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

	public void deleteAllByFeedId(UUID feedId) {
		likeRepository.deleteAllByFeedId(feedId);
	}
}
