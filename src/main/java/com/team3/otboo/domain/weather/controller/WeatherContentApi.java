package com.team3.otboo.domain.weather.controller;


import com.team3.otboo.domain.weather.dto.request.LocationRequest;
import com.team3.otboo.domain.weather.dto.response.LocationErrorResponse;
import com.team3.otboo.domain.weather.dto.response.LocationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Weather-Controller", description = "날씨 위치 조회 API")
public interface WeatherContentApi {

    @Operation(
            summary = "날씨 위치 정보 조회",
            description = "위도/경도로부터 행정구역 정보(동, 구, 시 등)를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "날씨 위치 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LocationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (위도/경도 누락 등)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LocationErrorResponse.class)
                            )
                    )
            }
    )
    ResponseEntity<LocationResponse> getLocation(
            @Validated @ModelAttribute LocationRequest locationRequestDto
    );

}

