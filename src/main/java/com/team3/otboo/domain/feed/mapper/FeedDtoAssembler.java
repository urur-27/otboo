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
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.weather.dto.PrecipitationDto;
import com.team3.otboo.domain.weather.dto.TemperatureDto;
import com.team3.otboo.domain.weather.dto.WeatherSummaryDto;
import com.team3.otboo.domain.weather.entity.Precipitation;
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import com.team3.otboo.storage.entity.BinaryContent;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
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

		String imageUrl = Optional.ofNullable(user.getProfile())
			.map(Profile::getBinaryContent)
			.map(BinaryContent::getImageUrl)
			.orElse(null);

		AuthorDto authorDto = new AuthorDto(
			user.getId(),
			user.getUsername(),
			imageUrl
		);

		UUID weatherId = feed.getWeatherId();
		Weather weather = weatherRepository.findById(weatherId).orElseThrow(
			() -> new EntityNotFoundException("weather not found. weatherId: " + weatherId)
		);
		WeatherSummaryDto weatherSummaryDto = getWeatherSummaryDto(weather);

		List<OotdDto> ootdDtos = ootdService.getOotdDtos(feedId);

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
			weatherSummaryDto,
			ootdDtos,
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

		WeatherSummaryDto weatherSummaryDto = getWeatherSummaryDto(weather);

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
			weatherSummaryDto,
			ootdDtos,
			feed.getContent(),
			likeCount,
			commentCount,
			likeByMe
		);
	}

	public FeedDto assemble(UUID feedId) {
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

		UUID weatherId = feed.getWeatherId();
		Weather weather = weatherRepository.findById(weatherId).orElseThrow(
			() -> new EntityNotFoundException("weather not found. weatherId: " + weatherId)
		);
		WeatherSummaryDto weatherSummaryDto = getWeatherSummaryDto(weather);

		List<OotdDto> ootdDtos = ootdService.getOotdDtos(feedId);

		Integer commentCount = feedCommentCountRepository.findById(feedId)
			.map(FeedCommentCount::getCommentCount)
			.map(Long::intValue)
			.orElse(0);
		Long likeCount = feedLikeCountRepository.findById(feedId)
			.map(FeedLikeCount::getLikeCount)
			.orElse(0L);

		return new FeedDto(
			feed.getId(),
			feed.getCreatedAt(),
			feed.getUpdatedAt(),
			authorDto,
			weatherSummaryDto,
			ootdDtos,
			feed.getContent(),
			likeCount,
			commentCount,
			null
		);
	}

	private WeatherSummaryDto getWeatherSummaryDto(Weather weather) {
		Precipitation precipitation = weather.getPrecipitation();
		Temperature temperature = weather.getTemperature();

		WeatherSummaryDto weatherSummaryDto = WeatherSummaryDto.builder()
			.weatherId(weather.getId().toString())
			.skyStatus(weather.getSkyStatus().toString())
			.precipitation(PrecipitationDto.builder()
				.type(precipitation.getType())
				.amount(precipitation.getAmount())
				.probability(precipitation.getProbability())
				.build()
			)
			.temperature(TemperatureDto.builder()
				.current(temperature.getTemperatureCurrent())
				.comparedToDayBefore(temperature.getTemperatureComparedToDayBefore())
				.min(temperature.getMin())
				.max(temperature.getMax())
				.build())
			.build();
		return weatherSummaryDto;
	}
}
