package com.team3.otboo.domain.weather.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VilageFcstResponse {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }
    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }
    @Data
    public static class Body {
        private Items items;
        private int totalCount;
    }
    @Data
    public static class Items {
        @JsonProperty("item")
        private List<ForecastItem> item;
    }
}