package com.team3.otboo.domain.clothing.infrastructure;

public interface OpenAiVisionClient {
    String sendImagePrompt(String imageUrl, String systemPrompt, String userPrompt);
}