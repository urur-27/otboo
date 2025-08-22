package com.team3.otboo.domain.weather.service;

import com.team3.otboo.domain.weather.dto.KakaoGeoResponse;
import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
public class WeatherServiceTest {

    @Autowired
    private WeatherService weatherService;

    @MockBean
    @Qualifier("locationRestTemplate")
    private RestTemplate locationRestTemplate;

    @DisplayName("좌표를 받아 행정구역 정보를 반환해준다.")
    @Test
    void getLocationForUser() {
        // given
        LocationRequest locationRequest = new LocationRequest(127.034992, 37.582604);

        // 목 응답 객체를 하나 만듭니다
        KakaoGeoResponse.Document doc = new KakaoGeoResponse.Document();
        doc.setRegion1depthName("서울특별시");
        doc.setRegion2depthName("동대문구");
        doc.setRegion3depthName("제기동");
        doc.setRegion4depthName("");
        doc.setX(1270378184);
        doc.setY(375831154);

        KakaoGeoResponse mockResponse = new KakaoGeoResponse();
        mockResponse.setDocuments(List.of(doc));

        when(locationRestTemplate.getForObject(anyString(), any(Class.class)))
                .thenReturn(mockResponse);

        // when
        LocationResponse locationResponse = weatherService.getLocationForUser(locationRequest);

        // then
        assertThat(locationResponse).isNotNull();
        assertThat(locationResponse.getLatitude()).isEqualTo(37.582604);
        assertThat(locationResponse.getLongitude()).isEqualTo(127.034992);
        assertThat(locationResponse.getLocationNames())
                .contains("서울특별시", "동대문구", "제기동");
    }
}
