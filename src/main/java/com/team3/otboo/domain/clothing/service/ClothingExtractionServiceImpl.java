package com.team3.otboo.domain.clothing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.domain.clothing.dto.ClothingAttributeWithDefDto;
import com.team3.otboo.domain.clothing.dto.ClothingDto;
import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;
import com.team3.otboo.domain.clothing.dto.response.ClothingLlmResponse;
import com.team3.otboo.domain.clothing.entity.Attribute;
import com.team3.otboo.domain.clothing.entity.AttributeOption;
import com.team3.otboo.domain.clothing.parser.HtmlParser;
import com.team3.otboo.domain.clothing.parser.HtmlParserResolver;
import com.team3.otboo.domain.clothing.repository.AttributeRepository;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
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

    private final HtmlParserResolver htmlParserResolver;
    private final ObjectMapper objectMapper;
    private final AttributeRepository attributeRepository;
    private final OpenAiChatModel openAiChatModel;

    public ClothingExtractionServiceImpl(
            ObjectMapper objectMapper,
            AttributeRepository attributeRepository,
            Environment env,
            HtmlParserResolver htmlParserResolver
    ) {
        this.objectMapper = objectMapper;
        this.attributeRepository = attributeRepository;
        this.htmlParserResolver = htmlParserResolver;

        String apiKey = env.getProperty("openai.api.key", System.getenv("OPENAI_API_KEY"));
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("OPENAI_API_KEY is missing!");
        }

        this.openAiChatModel = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-4o-mini")
                .temperature(0.1)         // 창의성 거의 없이 안정적인 응답
                .topP(1.0)                // temperature 사용하므로 topP는 기본값
                .maxTokens(512)           // 의상 정보만 추출하기 위해 적게 설정
                .frequencyPenalty(0.0)    // 반복 억제할 필요 없음
                .presencePenalty(0.0)     // 새로운 주제 도입 억제
                .logRequests(true)
                .logResponses(true)
                .build();

        log.info("OpenAIChatModel 초기화 완료");
    }

    @Override
    public ClothingDto extractFromUrl(String url, UUID ownerId) {
        HtmlParser parser = htmlParserResolver.resolve(url);
        ParsedClothingInfo parsed = parser.parse(url);

        if (parsed == null) {
            throw new BusinessException(ErrorCode.HTML_PARSE_FAILED);
        }

        log.info("htmlParser로 추출한 정보 : {}", parsed.fullHtml());

        // LLM 메시지 구성
        List<ChatMessage> messages = buildMessages(parsed.fullHtml());

        // LLM 호출
        Response<AiMessage> response = openAiChatModel.generate(messages);
        String content = response.content().text();
        log.info("LLM 응답: {}", content);

        // JSON -> DTO 파싱
        ClothingLlmResponse llm;
        try {
            llm = objectMapper.readValue(content, ClothingLlmResponse.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.LLM_JSON_PARSE_ERROR);
        }

        List<ClothingAttributeWithDefDto> attributes = mapAttributes(llm.attributes());

        return new ClothingDto(
                UUID.randomUUID(),
                ownerId,
                llm.name(),
                parsed.imageUrl(),
                llm.type(),
                attributes
        );
    }

    private List<ChatMessage> buildMessages(String descriptionText) {
        String systemPrompt = """
웹사이트 HTML 코드에서 의류 정보(예: 이름, 타입 등)를 체계적으로 추출하는 전문가로서 역할을 수행하세요. 주어진 HTML에서 의류의 이름, 타입(상의, 하의 등)과 같이 의미 있는 정보를 어떤 태그/속성을 통해 제공하고 있는지 분석하고,
다음 지침에 따라 JSON 형식으로 정보를 추출하시오.

요구사항:
- name: 제품의 대표명 또는 핵심 키워드
- type: ["TOP", "BOTTOM", "OUTER", "DRESS", "SHOES", "ACCESSORY", "UNDERWEAR", "ETC"] 중 하나
- attributes: 브랜드, 제품번호, 색상 등 기타 정보 포함
- 응답은 반드시 JSON 하나만 포함해야 하며, 다른 문장 없이 제공하세요
- 항상 먼저 충분한 분석 및 추론을 거친 후 결론(추출 결과 및 태그 매핑)을 제시하세요

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
""";

        String userPrompt = "다음 설명에서 의류 정보를 추출해줘:\n" + descriptionText;

        return List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        );
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