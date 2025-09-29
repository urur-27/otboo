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

    // 기존: 원격 URL 가져와서 파싱
    public HtmlExtractionResult extract(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000) // 5s → 30s 권장
                    .referrer("https://www.google.com")
                    .get();

            return buildResult(doc);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.HTML_PARSE_FAILED);
        }
    }

    // 추가: 이미 가진 HTML 문자열을 바로 파싱 (fixture용)
    public HtmlExtractionResult extractFromHtml(String html) {
        try {
            Document doc = Jsoup.parse(html);
            return buildResult(doc);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.HTML_PARSE_FAILED);
        }
    }

    private HtmlExtractionResult buildResult(Document doc) {
        String title = extractOgProperty(doc, "og:title");
        String description = extractOgProperty(doc, "og:description");
        String imageUrl = extractOgProperty(doc, "og:image");
        return new HtmlExtractionResult(title, description, imageUrl);
    }

    private String extractOgProperty(Document doc, String property) {
        return Optional.ofNullable(doc.selectFirst("meta[property=" + property + "]"))
                .map(e -> e.attr("content"))
                .orElse(null);
    }
}
