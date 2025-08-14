package com.team3.otboo.domain.clothing.analyzers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.response.VisionAnalysisResult;
import com.team3.otboo.domain.clothing.infrastructure.OpenAiVisionClient;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public VisionAnalysisResult analyze(String imageUrl, String title, String description, List<String> definitionNames, Map<String, List<String>> optionsByDef) {

        String systemPrompt = buildSystemPrompt(definitionNames, optionsByDef);
        String userPrompt   = buildUserPrompt(title, description);

        String raw = visionClient.sendImagePrompt(imageUrl, systemPrompt, userPrompt);
        log.info("LLM에서 반환된 raw: {}", raw);

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

    private String buildSystemPrompt(List<String> defs, Map<String, List<String>> optionsByDef) {
        String optionsJson = toPrettyJson(optionsByDef);
        // 출력 가드레일을 system에 명확히
        return """
            너는 온라인 쇼핑몰 상품 페이지에서 의류 속성을 추출하는 전문가다.


        출력 규칙:
        - 반드시 JSON만 반환한다. 코드펜스, 설명, 추가 텍스트 금지.
        - 키는 아래 스키마만 사용한다. 누락/추가/오타 금지.
        - 모든 텍스트(값 포함)는 한국어로 작성한다.
        - type은 {TOP,BOTTOM,OUTER,DRESS,SHOES,ACCESSORY,ETC} 중 하나.
        - 각 속성의 값은 "해당 속성의 허용 값 목록" 중에서만 선택한다.
        - 허용 값 목록에 없는 경우 임의 생성 금지(선택 불가 시 value=null 허용).
        
        지원 속성 목록(필수 규칙):
        - 의류에 대해 추출 가능한 속성의 전체 목록이다.
        - attributes 배열에는 반드시 이 목록에 있는 속성만 포함한다.
        - 목록에 없는 속성은 attributes에 추가하지 않는다.
        
        속성별 허용 값 목록(JSON):
        - 각 지원 속성에 대해 선택 가능한 값들의 목록이다.
        - value는 반드시 해당 속성의 허용 값 목록 안에서 선택해야 한다.
        - 허용 값 목록이 빈 배열이면 value=null로 둔다.

        이름 정제:
        - 상점명/광고문구/해시태그/이모지/사이즈·색상 접미사/괄호 표현 제거.
        - 예) "무신사 [공식] 데미지 워시드 데님 팬츠(미디엄 블루) - 1+1 특가"
          → "데미지 워시드 데님 팬츠"

        타입 힌트:
        - TOP(티셔츠/셔츠/니트/후디), BOTTOM(팬츠/데님/스커트), OUTER(자켓/코트/패딩),
          DRESS(원피스/점프수트), SHOES(스니커즈/부츠), ACCESSORY(모자/가방/벨트), 기타는 ETC.

        지원 속성 목록: %s
        속성별 허용 값 목록(JSON): %s

        반환 스키마:
        {
          "name": string | null,
          "type": "TOP" | "BOTTOM" | "OUTER" | "DRESS" | "SHOES" | "ACCESSORY" | "ETC",
          "attributes": [
            { "definitionName": string, "value": string | null }
          ]
        }
        """.formatted(String.join(", ", defs), optionsJson);
    }

    private String buildUserPrompt(String title, String description) {
        return """
            아래 쇼핑몰 페이지 정보를 참고하고, 제공된 이미지도 함께 분석하여 의류 정보를 추출하라.
            'JSON만' 반환할 것.
            
            제목: %s
            설명: %s

            주의:
            - 지원 목록에 없는 속성/값은 생성하지 말 것.
            - 확실하지 않으면 null/[]로 유지할 것.
            """.formatted(nvl(title), nvl(description));
    }

    // null 방어
    private String nvl(String s) { return s == null ? "" : s; }

    // Json으로
    private static String toPrettyJson(Map<String, List<String>> optionsByDef) {
        StringBuilder sb = new StringBuilder("{\n");
        var it = optionsByDef.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            String values = e.getValue().stream()
                    .map(v -> "\"" + escape(v) + "\"")
                    .collect(Collectors.joining(", "));
            sb.append("  \"").append(escape(e.getKey())).append("\": [").append(values).append("]");
            if (it.hasNext()) sb.append(",");
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escape(String s) { return s.replace("\\","\\\\").replace("\"","\\\""); }
}