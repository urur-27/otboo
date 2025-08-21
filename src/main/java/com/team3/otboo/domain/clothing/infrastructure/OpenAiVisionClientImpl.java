package com.team3.otboo.domain.clothing.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team3.otboo.config.OpenAiProperties;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiVisionClientImpl implements OpenAiVisionClient {

    private final ObjectMapper objectMapper;
    private final OpenAiProperties openAi;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public String sendImagePrompt(String imageUrl, String systemPrompt, String userPrompt) {
        try {
            // user 메시지 content: text + image_url
            Map<String, Object> textPart = Map.of("type", "text", "text", userPrompt);
            Map<String, Object> imagePart = Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", imageUrl)
            );

            Map<String, Object> sysMsg = Map.of(
                    "role", "system",
                    "content", systemPrompt
            );
            Map<String, Object> userMsg = Map.of(
                    "role", "user",
                    "content", List.of(textPart, imagePart)
            );

            Map<String, Object> body = Map.of(
                    "model", openAi.getModel(),
                    // JSON만 출력하도록 강제 (지원 모델에서 동작)
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(sysMsg, userMsg),
                    "temperature", openAi.getTemperature(),
                    "max_tokens", openAi.getMaxTokens()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAi.getApiKey());

            HttpEntity<String> req = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
            ResponseEntity<String> res = restTemplate.postForEntity(OPENAI_URL, req, String.class);

            return extractContentFromResponse(res.getBody());

        } catch (Exception e) {
            log.error("[LLM] 호출 실패", e);
            throw new BusinessException(ErrorCode.LLM_CALL_FAILED);
        }
    }

    private String extractContentFromResponse(String responseBody) throws Exception {
        Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
        List<?> choices = (List<?>) map.get("choices");
        if (choices == null || choices.isEmpty()) {
            log.warn("[LLM] 응답에 choices 없음.");
            throw new BusinessException(ErrorCode.LLM_JSON_NOT_FOUND, "OpenAI 응답에 choices 없음");
        }

        Map<?, ?> choice = (Map<?, ?>) choices.getFirst();
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        Object content = message.get("content");
        return content == null ? "" : content.toString();
    }
}