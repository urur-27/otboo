package com.team3.otboo.domain.clothing.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component("zigzag.kr")
@RequiredArgsConstructor
public class ZigzagHtmlParser implements HtmlParser {

    private final ObjectMapper objectMapper;

    @Override
    public ParsedClothingInfo parse(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // JSON-LD 구조화된 데이터 가져오기
            Element scriptTag = doc.selectFirst("script[type=application/ld+json]");
            if (scriptTag == null) {
                log.warn("Zigzag JSON-LD 를 찾을 수 없습니다.");
                return null;
            }

            // JSON 파싱
            String json = scriptTag.html();
            JsonNode root = objectMapper.readTree(json);

            String name = root.path("name").asText();
            String type = "UNKNOWN"; // 지그재그는 type 별도 없음 → 기본값
            JsonNode imageNode = root.path("image");
            String imageUrl = null;
            if (imageNode.isArray() && imageNode.size() > 0) {
                imageUrl = imageNode.get(0).asText(); // 첫 이미지 사용
            }

            return new ParsedClothingInfo(name, type, imageUrl, doc.outerHtml());

        } catch (Exception e) {
            log.error("Zigzag 파싱 중 오류 발생", e);
            return null;
        }
    }
}
