package com.team3.otboo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.team3.otboo.domain.feedread.repository")
public class ElasticSearchConfig {

}
