package com.team3.otboo.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	@Bean
	public NewTopic feedTopic() {
		return TopicBuilder.name("otboo-feed")
			.partitions(3)
			.replicas(1)
			.build();
	}

	@Bean
	public NewTopic commentTopic() {
		return TopicBuilder.name("otboo-feed-comment")
			.partitions(3)
			.replicas(1)
			.build();
	}

	@Bean
	public NewTopic likeTopic() {
		return TopicBuilder.name("otboo-feed-like")
			.partitions(3)
			.replicas(1)
			.build();
	}

	@Bean
	public NewTopic viewTopic() {
		return TopicBuilder.name("otboo-feed-view")
			.partitions(3)
			.replicas(1)
			.build();
	}
}
