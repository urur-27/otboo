package com.team3.otboo.domain.feed.mapper;

import com.team3.otboo.domain.feed.dto.AuthorDto;
import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.dto.OotdDto;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.feed.service.OotdService;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedDtoAssembler {

	// AuthorDto, WeatherDto, List<OotdDto> 만들어야함


	private final FeedCommentCountRepository feedCommentCountRepository;
	private final FeedLikeCountRepository feedLikeCountRepository;

	private final FeedRepository feedRepository;
	private final UserRepository userRepository;
	private final WeatherRepository weatherRepository;
	private final LikeRepository likeRepository;
	private final OotdService ootdService;

	public FeedDto assemble(UUID feedId, UUID userId) {
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feedId:" + feedId)
		);

		UUID authorId = feed.getAuthorId();
		User user = userRepository.findById(authorId).orElseThrow(
			() -> new EntityNotFoundException("user not found. userId: " + userId)
		);

		AuthorDto authorDto = new AuthorDto(
			user.getId(),
			user.getUsername(),
			user.getProfile().getBinaryContent().getImageUrl()
		);

		UUID weatherId = feed.getWeatherId();
		Weather weather = weatherRepository.findById(weatherId).orElseThrow(
			() -> new EntityNotFoundException("weather not found. weatherId: " + weatherId)
		);

		// TODO: weather 쪽 구현(repository) 완료 후 코드 완성하기

		// ootd
		List<OotdDto> ootdDtos = ootdService.getOotdDtos(feed.getId());

		// comment count, like count
		Integer commentCount = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.map(Long::intValue)
			.orElse(0);
		Long likeCount = feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);

		boolean likedByMe = likeRepository.existsByUserIdAndFeedId(userId, feed.getId());

		return new FeedDto(
			feed.getId(),
			feed.getCreatedAt(),
			feed.getUpdatedAt(),
			authorDto,
			null, // weather 구현 완료 후 weatherDto
			ootdDtos, // ootds
			feed.getContent(),
			likeCount,
			commentCount,
			likedByMe // likeByMe .. ->
		);
	}
}
