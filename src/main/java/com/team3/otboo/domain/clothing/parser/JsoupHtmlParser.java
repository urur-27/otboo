package com.team3.otboo.domain.clothing.parser;

import com.team3.otboo.domain.clothing.dto.ParsedClothingInfo;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import java.io.IOException;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class JsoupHtmlParser implements HtmlParser {

    @Override
    public ParsedClothingInfo parse(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            String imageUrl = Optional.ofNullable(doc.selectFirst("meta[property=og:image]"))
                    .map(e -> e.attr("content")).orElse(null);

            String description = Optional.ofNullable(doc.selectFirst("meta[property=og:description]"))
                    .map(e -> e.attr("content")).orElse(doc.body().text());

            return new ParsedClothingInfo(imageUrl, null, description);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.CLOTHING_EXTACTION_EXCEPTION);
        }
    }
}