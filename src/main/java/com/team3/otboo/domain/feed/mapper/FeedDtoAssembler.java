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
			() -> new EntityNotFoundException("user not found. user id: " + authorId)
		);

		AuthorDto authorDto = new AuthorDto(
			user.getId(),
			user.getUsername(),
			user.getProfile().getBinaryContent().getImageUrl()
		);

		// TODO: weather 쪽 구현(repository) 완료 후 코드 완성하기
//		UUID weatherId = feed.getWeatherId();
//		Weather weather = weatherRepository.findById(weatherId).orElseThrow(
//			() -> new EntityNotFoundException("weather not found. weatherId: " + weatherId)
//		);

		List<OotdDto> ootdDtos = ootdService.getOotdDtos(feedId);

		// comment count, like count
		Integer commentCount = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.map(Long::intValue)
			.orElse(0);
		Long likeCount = feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);

		// 내가 좋아요를 눌렀는가 하나 때문에 userId 를 넣어줘야함 .
		boolean likedByMe = likeRepository.existsByUserIdAndFeedId(userId, feedId);

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
			likedByMe
		);
	}

	// like api 에 대한 response 를 만드는 메서드, DB 조회를 1회 줄일 수 있음 .
	public FeedDto assemble(UUID feedId, UUID userId, Long likeCount) {
		Feed feed = feedRepository.findById(feedId).orElseThrow(
			() -> new EntityNotFoundException("feed not found. feed id: " + feedId)
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
			() -> new EntityNotFoundException("weather not found. weather id: " + weatherId)
		);

		// TODO weather 쪽 구현 이후 weather dto 만들기 .

		List<OotdDto> ootdDtos = ootdService.getOotdDtos(feedId);

		Integer commentCount = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.map(Long::intValue)
			.orElse(0);

		boolean likeByMe = likeRepository.existsByUserIdAndFeedId(userId, feedId);

		return new FeedDto(
			feedId,
			feed.getCreatedAt(),
			feed.getUpdatedAt(),
			authorDto,
			null, // weatherDto
			ootdDtos,
			feed.getContent(),
			likeCount,
			commentCount,
			likeByMe
		);
	}
}
