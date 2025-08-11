package com.team3.otboo.domain.clothing.parser;

import com.team3.otboo.global.exception.BusinessException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.team3.otboo.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;


@Component
public class HtmlParserResolver {

    private final Map<String, HtmlParser> htmlParsers;
    private final HtmlParser fallbackParser;

    public HtmlParserResolver(
            Map<String, HtmlParser> htmlParsers,
            @Qualifier("fallback") HtmlParser fallbackParser
    ) {
        this.htmlParsers = htmlParsers;
        this.fallbackParser = fallbackParser;
    }

    public HtmlParser resolve(String url) {
        try {
            String host = new URI(url).getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4); // www. 제거
            }
            HtmlParser parser = htmlParsers.getOrDefault(host, fallbackParser);
            return parser;
        } catch (URISyntaxException e) {
            throw new BusinessException(ErrorCode.INVALID_URL);
        }
    }
}