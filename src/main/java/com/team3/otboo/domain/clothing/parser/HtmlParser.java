package com.team3.otboo.domain.clothing.parser;

import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;

public interface HtmlParser {
    ParsedClothingInfo parse(String url);
}