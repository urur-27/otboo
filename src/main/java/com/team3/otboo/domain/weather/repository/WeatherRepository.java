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
      WHERE w.location.x  = :x
        AND w.location.y = :y
        AND FUNCTION('date', w.forecastAt) = :forecastDate
      ORDER BY w.forecastAt DESC
    """)
    Optional<Weather> findxByyAndForecastDate(
            @Param("x")     double x,
            @Param("y")    double y,
            @Param("forecastDate") LocalDate forecastDate
    );

    Optional<Weather> findByLocation_XAndLocation_YAndForecastAt(Integer x, Integer y, LocalDateTime forecastAt);

    @Query("""
    SELECT w
    FROM Weather w
    WHERE w.location.x = :x
      AND w.location.y = :y
      AND w.forecastedAt = :forecastedAt
      AND w.forecastAt >= :fromForecastAt
      AND w.forecastAt < :toForecastAt
    """)
    List<Weather> findWeathersWithForecastedAt(
            @Param("x") Integer x,
            @Param("y") Integer y,
            @Param("forecastedAt") LocalDateTime forecastedAt,
            @Param("fromForecastAt") LocalDateTime fromForecastAt,
            @Param("toForecastAt") LocalDateTime toForecastAt
    );
}
