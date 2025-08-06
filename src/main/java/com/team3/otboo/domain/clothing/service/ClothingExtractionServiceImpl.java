package com.team3.otboo.domain.clothing.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.parser.HtmlParser;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClothingExtractionServiceImpl implements ClothingExtractionService {

    private final HtmlParser htmlParser; // 파싱 전략
    private final ObjectMapper objectMapper;
    private final AttributeRepository attributeRepository;
    private final OpenAiChatModel openAiChatModel;

    public ClothingExtractionServiceImpl(
            HtmlParser htmlParser,
            ObjectMapper objectMapper,
            AttributeRepository attributeRepository,
            Environment env
    ) {
        this.htmlParser = htmlParser;
        this.objectMapper = objectMapper;
        this.attributeRepository = attributeRepository;

        String apiKey = env.getProperty("openai.api.key", System.getenv("OPENAI_API_KEY"));
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OPENAI_API_KEY is missing!");
        }

        this.openAiChatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.1)         // 창의성 거의 없이 안정적인 응답
                .topP(1.0)                // temperature 사용하므로 topP는 기본값
                .maxTokens(512)           // 의상 정보만 추출하면 충분한 길이
                .frequencyPenalty(0.0)    // 반복 억제할 필요 없음
                .presencePenalty(0.0)     // 새로운 주제 도입 억제
                .logRequests(true)
                .logResponses(true)
                .build();

        log.info("OpenAIChatModel 초기화 완료");
    }

    @Override
    public ClothingDto extractFromUrl(String url, UUID ownerId) {
        // HTML 기반 기본 정보 추출
        ParsedClothingInfo parsed = htmlParser.parse(url);
        log.info("htmlParser로 추출한 정보 : {}", parsed.description());
        String descriptionText = parsed.description();

        // LLM 호출
        String prompt = buildPrompt(descriptionText);
        log.info("LLM 요청 프롬프트:\n{}", prompt);

        String llmResponse = openAiChatModel.generate(prompt);
        log.info("LLM 응답: {}", llmResponse);

        try {
            String json = extractJsonArray(llmResponse);
            JsonNode root = objectMapper.readTree(json);

            String name = root.get("name").asText();
            String type = root.get("type").asText();
            List<Map<String, String>> rawAttributes = objectMapper.convertValue(root.get("attributes"), new TypeReference<>() {});

            List<ClothingAttributeWithDefDto> attributes = mapAttributes(rawAttributes);

            return new ClothingDto(
                    UUID.randomUUID(),  // 아직 저장 안된 상태니까 가짜 ID
                    ownerId,
                    name,
                    parsed.imageUrl(),
                    type,
                    attributes
            );
        } catch (Exception e) {
            log.error("LLM 응답 처리 실패", e);
            // LLM을 제외한 정보 리턴
            return new ClothingDto(UUID.randomUUID(), ownerId, null, parsed.imageUrl(), null, List.of());
        }
    }

    // 프롬프트
    private String buildPrompt(String desc) {
        return """
다음 상품 설명을 바탕으로 의류 정보를 추출해 주세요.

JSON으로만 응답해야 하며, 아래 구조를 따라야 합니다.

요구사항:
- name은 제품의 대표명 또는 핵심 키워드로 지정하세요
- type은 ["TOP", "BOTTOM", "OUTER", "DRESS", "SHOES", "ACCESSORY", "UNDERWEAR", "ETC"] 중 하나로 설정하세요
- 브랜드, 제품번호 등 나머지 정보는 attributes에 포함하세요
- 설명에 브랜드 이름이 포함되어 있다면 브랜드는 별도로 분리해서 attributes에 포함하세요

예시:
{
  "name": "Signature Collar Detail Midi Jacket",
  "type": "OUTER",
  "attributes": [
    {"definition": "브랜드", "value": "구호플러스"},
    {"definition": "제품번호", "value": "KE5839M03D"},
    {"definition": "색상", "value": "브라운"}
  ]
}

설명:
%s
""".formatted(desc);
    }

    // 응답에서 JSON 추출
    private String extractJsonArray(String llmResponse) {
        int start = llmResponse.indexOf("{");
        int end = llmResponse.lastIndexOf("}") + 1;

        if (start == -1 || end == -1) {
            throw new BusinessException(ErrorCode.LLM_JSON_NOT_FOUND);
        }

        return llmResponse.substring(start, end);
    }

    private List<ClothingAttributeWithDefDto> mapAttributes(List<Map<String, String>> parsed) {
        Map<String, Attribute> definitionMap = attributeRepository.findAll().stream()
                .collect(Collectors.toMap(Attribute::getName, Function.identity()));

        List<ClothingAttributeWithDefDto> result = new ArrayList<>();

        for (Map<String, String> entry : parsed) {
            String def = entry.get("definition");
            String value = entry.get("value");

            Attribute attr = definitionMap.get(def);
            if (attr == null) {
                log.warn("정의되지 않은 속성: {}", def);
                continue;
            }

            result.add(new ClothingAttributeWithDefDto(
                    attr.getId(),
                    attr.getName(),
                    attr.getOptions().stream().map(AttributeOption::getValue).toList(),
                    value
            ));
        }

        return result;
    }
}