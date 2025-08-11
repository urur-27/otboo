package com.team3.otboo.domain.clothing.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component("musinsa.com")
@RequiredArgsConstructor
public class MussinsaHtmlParser implements HtmlParser {

    private final ObjectMapper objectMapper;

    @Override
    public ParsedClothingInfo parse(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // script#__NEXT_DATA__에서 JSON 추출
            Element script = doc.selectFirst("script#__NEXT_DATA__");

            if (script == null) {
                log.warn("무신사 HTML 내 NEXT_DATA 스크립트를 찾을 수 없습니다.");
                return null;
            }

            String json = script.html();
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("props").path("pageProps").path("meta").path("data");

            // 기본 정보 추출
            String imageUrl = "https://image.msscdn.net" + data.path("thumbnailImageUrl").asText();
            String name = data.path("goodsNm").asText();
            String type = data.path("category").path("categoryDepth3Name").asText();

            // LLM에 보낼 설명(html 혹은 텍스트 요약)
            StringBuilder sb = new StringBuilder();
            sb.append("이름: ").append(name).append("\n");
            sb.append("카테고리: ").append(type).append("\n");
            sb.append("이미지: ").append(imageUrl).append("\n");

            // name/type/imageUrl을 포함한 전체 HTML 생성 (fullHtml은 LLM 입력용)
            JsonNode materials = data.path("goodsMaterial").path("materials");
            for (JsonNode material : materials) {
                String def = material.path("name").asText(); // ex: 소재
                for (JsonNode item : material.path("items")) {
                    if (item.path("isSelected").asBoolean()) {
                        sb.append(def).append(": ").append(item.path("name").asText()).append("\n"); // ex: 면
                    }
                }
            }

            return new ParsedClothingInfo(name, type, imageUrl, sb.toString());

        } catch (Exception e) {
            log.error("무신사 HTML 파싱 중 오류 발생", e);
            return null;
        }
    }
}
/*
{
  "goodsMaterial": {
    "materials": [
      {
        "name": "소재",
        "items": [
          { "name": "면 100%", "isSelected": true },
          { "name": "폴리우레탄", "isSelected": false }
        ]
      }
    ]
  }
}
 */