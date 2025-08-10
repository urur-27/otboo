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
@Component("29cm.co.kr")
@RequiredArgsConstructor
public class TwentyNineCmHtmlParser implements HtmlParser {

    private final ObjectMapper objectMapper;

    @Override
    public ParsedClothingInfo parse(String url) {
        try {
            Document doc = Jsoup.connect(url).get();

            // 스크립트 태그에서 찾을 부분 찾기
            Element script = doc.select("script").stream()
                    .filter(e -> e.html().contains("itemName"))
                    .findFirst()
                    .orElse(null);

            if (script == null) {
                log.warn("29CM HTML 내 itemName 포함된 스크립트를 찾을 수 없습니다. URL: {}", url);
                return null;
            }

            String rawJson = script.html();
            String cleanedJson = fixJsonFromScript(rawJson);
            JsonNode root = objectMapper.readTree(cleanedJson);

            // 기본 정보 추출
            String name = root.has("itemName") ? root.path("itemName").asText() : null;
            String type = root.has("subCopy") ? root.path("subCopy").asText() : null;

            // 이미지 추출 (JSON → fallback: og:image)
            String imageUrl = null;
            JsonNode imagesNode = root.path("itemImages");
            if (imagesNode.isArray() && imagesNode.size() > 0) {
                JsonNode imageNode = imagesNode.get(0);
                if (imageNode != null && imageNode.has("imageUrl")) {
                    imageUrl = "https://img.29cm.co.kr" + imageNode.path("imageUrl").asText();
                }
            }
            if (imageUrl == null) {
                Element ogImage = doc.selectFirst("meta[property=og:image]");
                imageUrl = ogImage != null ? ogImage.attr("content") : null;
            }

            // LLM에 보낼 설명(html 혹은 텍스트 요약)
            StringBuilder sb = new StringBuilder();
            sb.append("이름: ").append(name).append("\n");
            sb.append("카테고리: ").append(type).append("\n");
            sb.append("이미지: ").append(imageUrl).append("\n");

            // 5. 상세 속성 추가 (소재, 제조국 등)
            JsonNode details = root.path("itemDetailsList");
            if (details.isArray()) {
                for (JsonNode detail : details) {
                    String title = detail.path("itemDetailsTitles").asText(); // ex: 소재
                    String value = detail.path("itemDetailsValue").asText(); // ex: 면
                    sb.append(title).append(": ").append(value).append("\n");
                }
            }

            return new ParsedClothingInfo(name, type, imageUrl, sb.toString());

        } catch (Exception e) {
            log.error("29CM HTML 파싱 중 오류 발생 - URL: {}", url, e);
            return null;
        }
    }

    /**
     * self.__next_f.push([1,"..."]) 형식의 문자열을 정제된 JSON으로 변환
     */
    private String fixJsonFromScript(String raw) {
        if (raw == null || raw.isBlank()) return null;

        int firstQuote = raw.indexOf("[1,\"");
        if (firstQuote != -1) {
            raw = raw.substring(firstQuote + 4);
        }

        if (raw.endsWith("\"])\n") || raw.endsWith("\"]")) {
            raw = raw.substring(0, raw.length() - 3);
        }

        raw = raw.replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\n", "")
                .replaceAll("\"\\$[^\"]*\"", "null");

        raw = raw.trim();
        if (!raw.startsWith("{") && raw.contains("{")) {
            raw = raw.substring(raw.indexOf("{"));
        }
        if (!raw.endsWith("}")) {
            raw += "}";
        }

        return raw;
    }
}

/*
{
  "itemDetailsList": [
    {
      "itemDetailsTitles": "소재",
      "itemDetailsValue": "면 100%"
    },
    {
      "itemDetailsTitles": "제조국",
      "itemDetailsValue": "한국"
    }
  ]
}
 */