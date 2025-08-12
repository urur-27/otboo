package com.team3.otboo.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team3.otboo.domain.feed.dto.FeedDto;
import com.team3.otboo.domain.feed.entity.Feed;
import com.team3.otboo.domain.feed.entity.FeedCommentCount;
import com.team3.otboo.domain.feed.entity.FeedLikeCount;
import com.team3.otboo.domain.feed.mapper.FeedDtoAssembler;
import com.team3.otboo.domain.feed.repository.FeedCommentCountRepository;
import com.team3.otboo.domain.feed.repository.FeedLikeCountRepository;
import com.team3.otboo.domain.feed.repository.FeedRepository;
import com.team3.otboo.domain.feed.repository.LikeRepository;
import com.team3.otboo.domain.user.entity.Profile;
import com.team3.otboo.domain.user.entity.User;
import com.team3.otboo.domain.user.enums.Role;
import com.team3.otboo.domain.user.repository.UserRepository;
import com.team3.otboo.domain.weather.entity.Humidity;
import com.team3.otboo.domain.weather.entity.Precipitation;
import com.team3.otboo.domain.weather.entity.Temperature;
import com.team3.otboo.domain.weather.entity.Weather;
import com.team3.otboo.domain.weather.entity.WeatherLocation;
import com.team3.otboo.domain.weather.entity.WindSpeed;
import com.team3.otboo.domain.weather.enums.PrecipitationType;
import com.team3.otboo.domain.weather.enums.SkyStatus;
import com.team3.otboo.domain.weather.enums.WindSpeedLevel;
import com.team3.otboo.domain.weather.repository.WeatherRepository;
import com.team3.otboo.storage.entity.BinaryContent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class FeedAssembleServiceTest {

	@InjectMocks
	private FeedDtoAssembler feedDtoAssembler;

	@Mock
	private FeedRepository feedRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private WeatherRepository weatherRepository;
	@Mock
	private OotdService ootdService;
	@Mock
	private FeedCommentCountRepository feedCommentCountRepository;
	@Mock
	private FeedLikeCountRepository feedLikeCountRepository;
	@Mock
	private LikeRepository likeRepository;
	@Mock
	private Profile fakeProfile;
	@Mock
	private BinaryContent fakeBinaryContent;

	@Test
	void assembleTest() {
		UUID feedId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		UUID authorId = UUID.randomUUID();
		UUID weatherId = UUID.randomUUID();

		String content = "feed content !!";
		String profileImageUrl = "http://example.com/profile.jpg";

		Feed fakeFeed = Feed.create(authorId, weatherId, content);
		User fakeAuthor = User.builder()
			.username("김태우")
			.email("test@email.com")
			.password("password123")
			.role(Role.USER)
			.build();
		Weather fakeWeather = Weather.of(
			LocalDateTime.now(),
			LocalDateTime.now(),
			SkyStatus.CLEAR,
			new WeatherLocation(1D, 1D, 0, 0, List.of()),
			new Precipitation(PrecipitationType.NONE, 0D, 0D),
			new Humidity(0D, 0D),
			new Temperature(0D, 0D, 0D, 0D),
			new WindSpeed(0D, WindSpeedLevel.WEAK)
		);
		ReflectionTestUtils.setField(fakeWeather, "id", weatherId);
		FeedCommentCount fakeCommentCount = FeedCommentCount.init(feedId, 5L);
		FeedLikeCount fakeLikeCount = FeedLikeCount.init(feedId, 10L);

		when(feedRepository.findById(feedId)).thenReturn(Optional.of(fakeFeed));
		when(userRepository.findById(authorId)).thenReturn(Optional.of(fakeAuthor));
		when(weatherRepository.findById(weatherId)).thenReturn(Optional.of(fakeWeather));
		when(ootdService.getOotdDtos(feedId)).thenReturn(Collections.emptyList());
		when(feedCommentCountRepository.findById(feedId)).thenReturn(Optional.of(fakeCommentCount));
		when(feedLikeCountRepository.findById(feedId)).thenReturn(Optional.of(fakeLikeCount));
		when(likeRepository.existsByUserIdAndFeedId(userId, feedId)).thenReturn(true);

		fakeAuthor.setProfile(fakeProfile);

		when(fakeProfile.getBinaryContent()).thenReturn(fakeBinaryContent);
		when(fakeBinaryContent.getImageUrl()).thenReturn(profileImageUrl);

		FeedDto resultDto = feedDtoAssembler.assemble(feedId, userId);

		assertThat(resultDto).isNotNull();
		assertThat(resultDto.commentCount()).isEqualTo(5);
		assertThat(resultDto.likeCount()).isEqualTo(10L);
		assertThat(resultDto.likedByMe()).isTrue();

		verify(feedRepository).findById(feedId);
		verify(userRepository).findById(authorId);
		verify(likeRepository).existsByUserIdAndFeedId(userId, feedId);
	}
}
