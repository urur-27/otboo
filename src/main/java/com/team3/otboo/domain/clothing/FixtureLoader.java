package com.team3.otboo.domain.clothing;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

@Component
public class FixtureLoader {
    private final ResourceLoader resourceLoader;

    public FixtureLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public String loadHtml(String name) {
        try {
            Resource r = resourceLoader.getResource("classpath:/fixtures/" + name + ".html");
            try (InputStream in = r.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Fixture not found: " + name, e);
        }
    }
}