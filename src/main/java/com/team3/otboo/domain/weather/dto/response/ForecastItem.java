package com.team3.otboo.domain.weather.dto.response;

import lombok.Data;

@Data
public class ForecastItem {
    private String baseDate;    // yyyyMMdd
    private String baseTime;    // HHmm
    private String category;    // TMP, SKY, PTY, …
    private String fcstDate;    // yyyyMMdd
    private String fcstTime;    // HHmm
    private String fcstValue;   // 예보값
    private int nx, ny;
}
