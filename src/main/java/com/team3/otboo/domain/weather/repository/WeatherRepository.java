package com.team3.otboo.domain.weather.repository;

import com.team3.otboo.domain.weather.entity.Weather;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherRepository extends JpaRepository<Weather, UUID> {
    @Query("""
      SELECT w 
      FROM Weather w 
      WHERE w.location.latitude  = :latitude
        AND w.location.longitude = :longitude
        AND FUNCTION('date', w.forecastAt) = :forecastDate
      ORDER BY w.forecastAt DESC
    """)
    Optional<Weather> findLatestByLocationAndForecastDate(
            @Param("latitude")     double latitude,
            @Param("longitude")    double longitude,
            @Param("forecastDate") LocalDate forecastDate
    );

    Optional<Weather> findByLocationLatitudeAndLocationLongitudeAndForecastAt(Double latitude, Double longitude, LocalDateTime forecastAt);
}
