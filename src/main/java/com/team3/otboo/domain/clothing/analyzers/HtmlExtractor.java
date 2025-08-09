package com.team3.otboo.domain.clothing.analyzers;

import com.team3.otboo.domain.clothing.dto.response.HtmlExtractionResult;
import com.team3.otboo.global.exception.BusinessException;
import com.team3.otboo.global.exception.ErrorCode;
import java.io.IOException;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HtmlExtractor {

    public HtmlExtractionResult extract(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            // 간단한 정보 및 이미지 url 추출
            String title = extractOgProperty(doc, "og:title");
            String description = extractOgProperty(doc, "og:description");
            String imageUrl = extractOgProperty(doc, "og:image");

            return new HtmlExtractionResult(title, description, imageUrl);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.HTML_PARSE_FAILED);
        }
    }

    private String extractOgProperty(Document doc, String property) {
        return Optional.ofNullable(doc.selectFirst("meta[property=" + property + "]"))
                .map(e -> e.attr("content"))
                .orElse(null);
    }
}