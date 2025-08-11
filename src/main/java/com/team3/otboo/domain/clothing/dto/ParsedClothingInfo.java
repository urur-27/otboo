package com.team3.otboo.domain.clothing.dto;

public record ParsedClothingInfo(
        String name,
        String type,
        String imageUrl,
        String fullHtml
) {}