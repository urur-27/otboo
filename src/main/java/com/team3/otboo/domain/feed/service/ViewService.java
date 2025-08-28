package com.team3.otboo.domain.feed.service;

import com.team3.otboo.domain.feed.repository.FeedViewCountRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewService {

	private final FeedViewCountRepository feedViewCountRepository;
	private final ViewCountBackUpProcessor viewCountBackUpProcessor;
	private static final int BACK_UP_BACH_SIZE = 100;

	public Long increase(UUID feedId) {
		Long count = feedViewCountRepository.increase(feedId);
		if (count % BACK_UP_BACH_SIZE == 0) { // 100 단위마다 데이터베이스에 백업
			viewCountBackUpProcessor.backUp(feedId, count);
		}
		return count;
	}

	public Long count(UUID feedId) {
		return feedViewCountRepository.read(feedId);
	}
}
