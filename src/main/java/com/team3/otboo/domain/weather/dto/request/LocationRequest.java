package com.team3.otboo.domain.weather.dto.request;

import lombok.NonNull;

public record LocationRequest(
    @NonNull
    Double longitude,

    @NonNull
    Double latitude
){}
