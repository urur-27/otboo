package com.team3.otboo.domain.weather.dto.response;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationErrorResponse{

    private String exceptionName;

    private String message;

    private List<String> details;

    @Builder
    private LocationErrorResponse(String exceptionName, String message, List<String> details) {
        this.exceptionName = exceptionName;
        this.message = message;
        this.details = details;
    }

    public static LocationErrorResponse of(String exceptionName, String message) {
        return LocationErrorResponse.builder()
                .exceptionName(exceptionName)
                .message(message)
                .details(List.of())
                .build();
    }
}