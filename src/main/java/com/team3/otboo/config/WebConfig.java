package com.team3.otboo.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${otboo.storage.local.root-path}")
    private String rootProp;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 절대 경로
        Path root = Paths.get(rootProp);
        if (!root.isAbsolute()) {
            root = Paths.get(System.getProperty("user.dir")).resolve(root).toAbsolutePath();
        }

        // 핸들러에 등록할 물리 경로
        String location = "file:" + (root.toString().endsWith(File.separator)
                ? root.toString()
                : root.toString() + File.separator);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}

