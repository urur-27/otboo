package com.team3.otboo.domain.clothing.analyzers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.infrastructure.OpenAiVisionClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiVisionAnalyzer implements VisionAnalyzer {

    private final OpenAiVisionClient visionClient;
    private final ObjectMapper objectMapper;

    @Override
    public VisionAnalysisResult analyze(String imageUrl, String title, String description, List<String> definitionNames) {

        String systemPrompt = buildSystemPrompt(definitionNames);
        String userPrompt   = buildUserPrompt(title, description);

        String raw = visionClient.sendImagePrompt(imageUrl, systemPrompt, userPrompt);

        try {
            // JSON만 추출되도록
            String jsonOnly = extractJsonFromResponse(raw);
            return objectMapper.readValue(jsonOnly, VisionAnalysisResult.class);
        } catch (Exception e) {
            throw new RuntimeException("Vision LLM 응답 파싱 실패", e);
        }
    }

    private String extractJsonFromResponse(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        throw new IllegalStateException("JSON 형식 감지 실패");
    }

    private String buildSystemPrompt(List<String> defs) {
        // 출력 가드레일을 system에 명확히
        return """
            너는 온라인 쇼핑몰 상품 페이지에서 의류 속성을 추출하는 전문가다.
            규칙:
            - 반드시 JSON으로만 응답하고, 설명/코드펜스/추가 텍스트를 포함하지 마라.
            - 아래 '지원 속성 목록'에 있는 항목만 attributes에 포함하라. 목록에 없는 속성은 넣지 마라.
            - name은 실제 제품명으로만 작성하고, 광고 문구/후기/상점명/불필요한 접미사는 제거하라.
            - type은 TOP/BOTTOM/OUTER/DRESS/SHOES/ACCESSORY/ETC 중 하나의 카테고리로 작성하라.
            - 값은 한국어로 작성하라.

            지원 속성 목록: %s
            """.formatted(String.join(", ", defs));
    }

    private String buildUserPrompt(String title, String description) {
        return """
            아래 쇼핑몰 페이지 정보를 참고하고, 제공된 이미지도 함께 분석하여 의류 정보를 추출하라.
            'JSON만' 반환할 것.

            제목: %s
            설명: %s

            반환 형식:
            {
              "name": "...",
              "type": "...",
              "attributes": [
                { "definitionName": "색상", "value": "화이트", "selectedValues": ["화이트"] }
              ]
            }
            """.formatted(nvl(title), nvl(description));
    }

    // null 방어
    private String nvl(String s) { return s == null ? "" : s; }
}