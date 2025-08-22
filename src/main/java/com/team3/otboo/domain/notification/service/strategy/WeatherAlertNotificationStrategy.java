package com.team3.otboo.domain.notification.service.strategy;

import com.team3.otboo.domain.notification.entity.Notification;
import com.team3.otboo.domain.notification.entity.NotificationLevel;
import com.team3.otboo.domain.user.repository.ProfileRepository;
import com.team3.otboo.event.WeatherAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
public class WeatherAlertNotificationStrategy implements NotificationStrategy<WeatherAlert> {

    private final ProfileRepository profileRepository;
    private final MessageSource messageSource;

    @Override
    public boolean supports(Object event) {
        return event instanceof WeatherAlert;
    }

    @Override
    public List<Notification> createNotification(WeatherAlert e) {
        var users = profileRepository.findAllByGrid(e.x(), e.y())
                .stream().map(p -> p.getUser()).distinct().toList();

        String titleKey, contentKey;
        Object[] args;

        switch (e.type()) {
            case RAIN_START -> {
                titleKey = "notification.weather.rainStart.title";
                contentKey = "notification.weather.rainStart.content";
                args = new Object[]{ Math.round(e.rainProb() * 100), e.forecastedAt() };
            }
            case RAIN_PROB_SPIKE -> {
                titleKey = "notification.weather.rainProbSpike.title";
                contentKey = "notification.weather.rainProbSpike.content";
                args = new Object[]{
                        Math.round(e.rainProbDelta() * 100),
                        Math.round(e.rainProb() * 100),
                        e.forecastedAt()
                };
            }
            case TEMP_SPIKE -> {
                titleKey = "notification.weather.tempSpike.title";
                contentKey = "notification.weather.tempSpike.content";
                args = new Object[]{ String.format("%+.1f", e.temperatureDelta()), e.forecastedAt() };
            }
            default -> throw new IllegalStateException("Unknown type: " + e.type());
        }

        String title = messageSource.getMessage(titleKey, null, Locale.KOREAN);
        String content = messageSource.getMessage(contentKey, args, Locale.KOREAN);

        NotificationLevel level = switch (e.type()) {
            case TEMP_SPIKE -> NotificationLevel.WARNING;
            case RAIN_START, RAIN_PROB_SPIKE -> NotificationLevel.INFO;
        };

        return users.stream()
                .map(u -> Notification.builder()
                        .receiver(u)
                        .title(title)
                        .content(content)
                        .level(level)
                        .build())
                .toList();
    }
}